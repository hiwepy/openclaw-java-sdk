package io.github.hiwepy.openclaw.ws;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.hiwepy.openclaw.OpenClawClientConfig;
import io.github.hiwepy.openclaw.util.OpenClawStrings;
import io.github.hiwepy.openclaw.ws.protocol.*;
import io.github.hiwepy.openclaw.ws.protocol.params.*;
import io.github.hiwepy.openclaw.ws.protocol.result.*;
import lombok.extern.slf4j.Slf4j;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantLock;

/**
 * OpenClaw Gateway 原生 WebSocket 协议客户端。
 *
 * <h3>协议概要</h3>
 * <ul>
 *     <li>帧类型：{@code req}（RPC 请求）、{@code res}（RPC 响应）、{@code event}（推送事件）</li>
 *     <li>握手：连接后发送 {@code method: "connect"}，收到 {@code hello-ok} 后可开始 RPC</li>
 *     <li>流式对话：{@code chat.send} → 多个 {@code event: "chat", delta: true} → {@code event: "chat", done: true}</li>
 * </ul>
 *
 * <h3>使用示例</h3>
 * <pre>{@code
 * OpenClawClientConfig config = new OpenClawClientConfig();
 * config.setGatewayBaseUrl("http://localhost:18789");
 * config.setGatewayAuthToken("my-gateway-token");
 *
 * OpenClawGatewayWsClient ws = new OpenClawGatewayWsClient(config);
 * ws.addListener(new OpenClawWsListener() { ... });
 * ws.connectBlocking();
 *
 * // 流式对话
 * ws.chatSend(ChatSendParams.builder()
 *     .sessionKey("main")
 *     .message("你好")
 *     .build(),
 *     new ChatStreamHandler() {
 *         public void onDelta(String text) { System.out.print(text); }
 *         public void onComplete(String fullText) { System.out.println(); }
 *         public void onError(String error) { System.err.println(error); }
 *     });
 *
 * ws.close();
 * }</pre>
 */
@Slf4j
public class OpenClawGatewayWsClient extends WebSocketClient {

    private static final int PROTOCOL_VERSION = 1;

    /** 默认 RPC 超时（毫秒） */
    private static final long DEFAULT_RPC_TIMEOUT_MS = 120_000L;

    private final OpenClawClientConfig config;
    private final ObjectMapper objectMapper;
    private final List<OpenClawWsListener> listeners = new CopyOnWriteArrayList<>();

    /** RPC 请求 ID → PendingRpc */
    private final Map<String, PendingRpc> pendingRpcs = new ConcurrentHashMap<>();

    /** chat.send 请求 ID → ChatStreamCollector */
    private final Map<String, ChatStreamCollector> activeChatStreams = new ConcurrentHashMap<>();

    /** 串行化连接/握手/重连，避免多线程重复 connect 或复用已完成的 connectFuture */
    private final ReentrantLock connectLock = new ReentrantLock();

    /** 串行化 WebSocket 写帧（Java-WebSocket 的 send 非线程安全） */
    private final ReentrantLock writeLock = new ReentrantLock();

    private final AtomicReference<HelloOk> helloOkRef = new AtomicReference<>();

    /** 每次连接尝试单独一个 Future，重连时替换 */
    private volatile CompletableFuture<HelloOk> connectFuture = new CompletableFuture<>();

    // ============================================================
    // 构造
    // ============================================================

    public OpenClawGatewayWsClient(OpenClawClientConfig config) {
        this(config, buildWsUri(config));
    }

    public OpenClawGatewayWsClient(OpenClawClientConfig config, URI serverUri) {
        super(serverUri);
        this.config = config;
        this.objectMapper = new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        this.setConnectionLostTimeout(30);
    }

    private static URI buildWsUri(OpenClawClientConfig config) {
        String base = config.getGatewayBaseUrl();
        if (OpenClawStrings.isBlank(base)) {
            throw new IllegalArgumentException("gatewayBaseUrl is required for WebSocket connection");
        }
        String wsUrl = base
                .replaceFirst("^https://", "wss://")
                .replaceFirst("^http://", "ws://")
                .replaceAll("/+$", "");
        return URI.create(wsUrl);
    }

    // ============================================================
    // 生命周期
    // ============================================================

    @Override
    public void onOpen(ServerHandshake handshake) {
        log.info("WebSocket connected to {}, sending connect handshake", getURI());
        sendConnectHandshake();
    }

    @Override
    public void onMessage(String message) {
        try {
            JsonNode root = objectMapper.readTree(message);
            String type = root.path("type").asText("");

            switch (type) {
                case "res":
                    handleResponse(root);
                    break;
                case "event":
                    handleEvent(root);
                    break;
                case "req":
                    log.debug("Received unexpected req frame from Gateway: {}", root);
                    break;
                default:
                    log.warn("Unknown frame type: {}", type);
                    break;
            }
        } catch (Exception e) {
            log.error("Failed to parse WebSocket message: {}", message, e);
        }
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        log.info("WebSocket closed: code={}, reason={}, remote={}", code, reason, remote);
        failConnectFuture(new RuntimeException("WebSocket closed: " + reason));
        helloOkRef.set(null);
        // 取消所有 pending RPC
        for (Map.Entry<String, PendingRpc> entry : pendingRpcs.entrySet()) {
            entry.getValue().future.completeExceptionally(
                    new RuntimeException("WebSocket closed: " + reason));
        }
        pendingRpcs.clear();
        // 清理 chat streams
        for (Map.Entry<String, ChatStreamCollector> entry : activeChatStreams.entrySet()) {
            entry.getValue().handler.onError("WebSocket closed: " + reason);
        }
        activeChatStreams.clear();
        // 通知监听器
        listeners.forEach(l -> l.onDisconnected(code, reason, remote));
    }

    @Override
    public void onError(Exception ex) {
        log.error("WebSocket error", ex);
        listeners.forEach(l -> l.onError(ex));
    }

    // ============================================================
    // 握手
    // ============================================================

    /**
     * 在 {@link #connectLock} 下发送 connect 握手；若当前握手已完成则跳过，避免 onOpen 重复发送。
     */
    private void sendConnectHandshake() {
        connectLock.lock();
        try {
            if (connectFuture.isDone()) {
                log.debug("Skipping duplicate connect handshake");
                return;
            }

            ConnectParams.AuthInfo auth = null;
            String token = config.getGatewayAuthToken();
            String password = config.getGatewayAuthPassword();
            if (OpenClawStrings.isNotBlank(token)) {
                auth = ConnectParams.AuthInfo.token(token);
            } else if (OpenClawStrings.isNotBlank(password)) {
                auth = ConnectParams.AuthInfo.password(password);
            }

            ConnectParams params = new ConnectParams(
                    PROTOCOL_VERSION, PROTOCOL_VERSION,
                    new ConnectParams.ClientInfo(
                            "openclaw-java-sdk",
                            "OpenClaw Java SDK",
                            "1.0.0",
                            "java",
                            "operator"
                    ),
                    auth
            );

            String reqId = generateId();
            RequestFrame req = new RequestFrame(reqId, "connect", params.toParamsMap());

            PendingRpc pending = new PendingRpc(reqId, "connect", System.currentTimeMillis());
            pendingRpcs.put(reqId, pending);

            try {
                String json = objectMapper.writeValueAsString(req);
                log.debug("Sending connect handshake: {}", json);
                sendFrame(json);
            } catch (JsonProcessingException e) {
                failConnectFuture(e);
            }
        } finally {
            connectLock.unlock();
        }
    }

    /**
     * 准备一次新的连接尝试：已握手成功则复用；否则关闭旧连接并重置 connectFuture。
     *
     * @return 本次握手对应的 Future（在 connectLock 外等待，避免与 onOpen 死锁）
     */
    private CompletableFuture<HelloOk> beginConnectAttempt() {
        connectLock.lock();
        try {
            HelloOk existing = helloOkRef.get();
            CompletableFuture<HelloOk> current = connectFuture;
            if (isOpen() && existing != null && current.isDone() && !current.isCompletedExceptionally()) {
                return current;
            }
            if (isOpen()) {
                log.debug("Closing existing WebSocket before reconnect");
                super.close();
            }
            resetConnectStateUnderLock();
            return connectFuture;
        } finally {
            connectLock.unlock();
        }
    }

    /**
     * 废弃进行中的握手 Future 并创建新的（调用方需已持有 {@link #connectLock}）。
     */
    private void resetConnectStateUnderLock() {
        CompletableFuture<HelloOk> previous = connectFuture;
        if (!previous.isDone()) {
            previous.completeExceptionally(new CancellationException("Superseded by new connect attempt"));
        }
        helloOkRef.set(null);
        connectFuture = new CompletableFuture<>();
    }

    private void completeConnectFuture(HelloOk helloOk) {
        connectLock.lock();
        try {
            helloOkRef.set(helloOk);
            CompletableFuture<HelloOk> handshakeFuture = connectFuture;
            if (!handshakeFuture.isDone()) {
                handshakeFuture.complete(helloOk);
            }
        } finally {
            connectLock.unlock();
        }
    }

    private void failConnectFuture(Throwable cause) {
        connectLock.lock();
        try {
            CompletableFuture<HelloOk> handshakeFuture = connectFuture;
            if (!handshakeFuture.isDone()) {
                handshakeFuture.completeExceptionally(cause);
            }
        } finally {
            connectLock.unlock();
        }
    }

    /**
     * 线程安全地向 Gateway 发送 JSON 帧。
     */
    private void sendFrame(String json) {
        writeLock.lock();
        try {
            send(json);
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * 连接 Gateway WebSocket 并阻塞等待握手完成。
     * <p>内部调用 {@code super.connectBlocking()} 建立 TCP 连接，然后等待 {@code connect} RPC 握手响应。</p>
     * <p>多线程并发调用时由 {@link #connectLock} 串行化；已连接且握手成功时直接返回缓存的 {@link HelloOk}。</p>
     *
     * @return 握手结果（hello-ok）
     * @throws InterruptedException 线程被中断
     */
    public HelloOk connectHandshake() throws InterruptedException {
        CompletableFuture<HelloOk> handshakeFuture = beginConnectAttempt();
        if (handshakeFuture.isDone() && !handshakeFuture.isCompletedExceptionally()) {
            HelloOk cached = handshakeFuture.getNow(null);
            if (cached != null) {
                return cached;
            }
        }
        if (!isOpen()) {
            super.connectBlocking();
        }
        try {
            return handshakeFuture.get(30, TimeUnit.SECONDS);
        } catch (ExecutionException | TimeoutException e) {
            throw new RuntimeException("Gateway WS connect handshake failed", e);
        }
    }

    /**
     * 异步连接并等待握手完成。
     * <p>多线程安全：与 {@link #connectHandshake()} 共用同一套连接状态与锁。</p>
     */
    public CompletableFuture<HelloOk> connectHandshakeAsync() {
        CompletableFuture<HelloOk> handshakeFuture = beginConnectAttempt();
        if (handshakeFuture.isDone() && !handshakeFuture.isCompletedExceptionally()) {
            return handshakeFuture;
        }
        if (!isOpen()) {
            super.connect();
        }
        return handshakeFuture;
    }

    @Override
    public void close() {
        failConnectFuture(new CancellationException("Closed by client"));
        helloOkRef.set(null);
        super.close();
    }

    /**
     * 获取握手信息（仅握手完成后非空）。
     */
    public HelloOk getHelloOk() {
        return helloOkRef.get();
    }

    // ============================================================
    // RPC 调用（类型化 API）
    // ============================================================

    /**
     * 获取会话列表（{@code sessions.list}）。
     */
    public SessionsListResult sessionsList(SessionsListParams params) {
        return invokeRpc("sessions.list", params, SessionsListResult.class, DEFAULT_RPC_TIMEOUT_MS);
    }

    /**
     * 获取会话列表（默认参数）。
     */
    public SessionsListResult sessionsList() {
        return sessionsList(SessionsListParams.defaults());
    }

    /**
     * 获取聊天历史（{@code chat.history}）。
     */
    public ChatHistoryResult chatHistory(ChatHistoryParams params) {
        Objects.requireNonNull(params, "params");
        return invokeRpc("chat.history", params, ChatHistoryResult.class, DEFAULT_RPC_TIMEOUT_MS);
    }

    /**
     * 获取聊天历史。
     *
     * @param sessionKey 会话键
     * @param limit      条数上限（可选）
     */
    public ChatHistoryResult chatHistory(String sessionKey, Integer limit) {
        return chatHistory(ChatHistoryParams.of(sessionKey, limit));
    }

    /**
     * 中止聊天 run（{@code chat.abort}）。
     */
    public ChatAbortResult chatAbort(ChatAbortParams params) {
        Objects.requireNonNull(params, "params");
        return invokeRpc("chat.abort", params, ChatAbortResult.class, DEFAULT_RPC_TIMEOUT_MS);
    }

    /**
     * 中止指定会话上的所有进行中的 run。
     */
    public ChatAbortResult chatAbort(String sessionKey) {
        return chatAbort(ChatAbortParams.abortSession(sessionKey));
    }

    /**
     * 获取智能体身份（{@code agent.identity.get}）。
     */
    public AgentIdentityGetResult agentIdentityGet(AgentIdentityGetParams params) {
        Objects.requireNonNull(params, "params");
        return invokeRpc("agent.identity.get", params, AgentIdentityGetResult.class, DEFAULT_RPC_TIMEOUT_MS);
    }

    /**
     * 获取当前默认智能体身份。
     */
    public AgentIdentityGetResult agentIdentityGet() {
        return agentIdentityGet(AgentIdentityGetParams.empty());
    }

    /**
     * 列出 cron 任务（{@code cron.list}）。
     */
    public CronListResult cronList(CronListParams params) {
        Objects.requireNonNull(params, "params");
        return invokeRpc("cron.list", params, CronListResult.class, DEFAULT_RPC_TIMEOUT_MS);
    }

    /**
     * 列出 cron 任务（默认参数）。
     */
    public CronListResult cronList() {
        return cronList(CronListParams.defaults());
    }

    /**
     * 读取配置快照（{@code config.get}）。
     */
    public ConfigGetResult configGet() {
        return invokeRpc("config.get", new Object(), ConfigGetResult.class, DEFAULT_RPC_TIMEOUT_MS);
    }

    /**
     * 发送 RPC 并解析为指定类型；{@code ok: false} 时抛出 {@link OpenClawWsRpcException}。
     */
    private <T> T invokeRpc(String method, Object params, Class<T> responseType, long timeoutMs) {
        Objects.requireNonNull(method, "method");
        Objects.requireNonNull(responseType, "responseType");
        ResponseFrame frame = executeRpc(method, params, timeoutMs);
        if (!frame.isOk()) {
            throw new OpenClawWsRpcException(method, frame.getError());
        }
        Object payload = frame.getPayload();
        if (payload == null) {
            return null;
        }
        return objectMapper.convertValue(payload, responseType);
    }

    /**
     * 底层 RPC：发送请求帧并等待 {@link ResponseFrame}（不解析 payload）。
     */
    private ResponseFrame executeRpc(String method, Object params, long timeoutMs) {
        requireHandshakeComplete(method);
        String reqId = generateId();
        PendingRpc pending = new PendingRpc(reqId, method, System.currentTimeMillis());
        pendingRpcs.put(reqId, pending);

        try {
            Map<String, Object> paramsMap = serializeRpcParams(params);
            RequestFrame req = new RequestFrame(reqId, method, paramsMap);
            String json = objectMapper.writeValueAsString(req);
            sendFrame(json);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize RPC request: " + method, e);
        }

        try {
            return pending.future.get(timeoutMs, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("RPC interrupted: " + method, e);
        } catch (ExecutionException e) {
            throw new RuntimeException("RPC failed: " + method, e.getCause());
        } catch (TimeoutException e) {
            pendingRpcs.remove(reqId);
            throw new RuntimeException("RPC timeout: " + method + " (" + timeoutMs + "ms)");
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> serializeRpcParams(Object params) throws JsonProcessingException {
        if (params == null) {
            return Collections.emptyMap();
        }
        if (params instanceof ChatSendParams) {
            return ((ChatSendParams) params).toParamsMap();
        }
        if (params instanceof SessionsSendParams) {
            return ((SessionsSendParams) params).toParamsMap();
        }
        if (params instanceof ConnectParams) {
            return ((ConnectParams) params).toParamsMap();
        }
        if (params instanceof Map) {
            return (Map<String, Object>) params;
        }
        return objectMapper.convertValue(params, Map.class);
    }

    private void requireHandshakeComplete(String method) {
        if (helloOkRef.get() == null) {
            throw new IllegalStateException(
                    "Gateway WS handshake not complete; call connectHandshake() before " + method);
        }
    }

    // ============================================================
    // chat.send（流式对话）
    // ============================================================

    /**
     * 通过 {@code chat.send} 发送消息给智能体，流式接收回复。
     *
     * @param params  聊天参数（必填 message）
     * @param handler 流式回复处理器
     */
    public void chatSend(ChatSendParams params, ChatStreamHandler handler) {
        String reqId = generateId();
        ChatStreamCollector collector = new ChatStreamCollector(reqId, handler);
        activeChatStreams.put(reqId, collector);

        Map<String, Object> paramsMap = params.toParamsMap();
        // idempotencyKey: Gateway chat.send 要求
        paramsMap.put("idempotencyKey", reqId);

        try {
            RequestFrame req = new RequestFrame(reqId, "chat.send", paramsMap);
            String json = objectMapper.writeValueAsString(req);
            sendFrame(json);
        } catch (JsonProcessingException e) {
            activeChatStreams.remove(reqId);
            handler.onError("Failed to serialize chat.send request: " + e.getMessage());
        }
    }

    // ============================================================
    // sessions.send
    // ============================================================

    /**
     * 通过 {@code sessions.send} 向指定会话发消息（非流式）。
     *
     * @param params 会话发送参数（{@code key}、{@code message} 必填）
     * @return 发送确认（含 {@code runId} 等）
     */
    public SessionsSendResult sessionsSend(SessionsSendParams params) {
        Objects.requireNonNull(params, "params");
        return invokeRpc("sessions.send", params, SessionsSendResult.class, DEFAULT_RPC_TIMEOUT_MS);
    }

    // ============================================================
    // 监听器管理
    // ============================================================

    public void addListener(OpenClawWsListener listener) {
        listeners.add(listener);
    }

    public void removeListener(OpenClawWsListener listener) {
        listeners.remove(listener);
    }

    // ============================================================
    // 内部帧处理
    // ============================================================

    private void handleResponse(JsonNode root) {
        String id = root.path("id").asText("");
        boolean ok = root.path("ok").asBoolean(false);
        Object payload = null;
        ErrorShape error = null;

        if (root.has("payload") && !root.path("payload").isNull()) {
            payload = objectMapper.convertValue(root.path("payload"), Object.class);
        }
        if (root.has("error") && !root.path("error").isNull()) {
            try {
                error = objectMapper.treeToValue(root.path("error"), ErrorShape.class);
            } catch (JsonProcessingException e) {
                log.warn("Failed to parse error shape", e);
            }
        }

        ResponseFrame frame = new ResponseFrame("res", id, ok, payload, error);

        // 1. 检查是否为 connect 握手响应
        PendingRpc connectPending = pendingRpcs.remove(id);
        if (connectPending != null) {
            if ("connect".equals(connectPending.method) && ok && payload != null) {
                try {
                    JsonNode payloadNode = objectMapper.valueToTree(payload);
                    HelloOk helloOk = objectMapper.treeToValue(payloadNode, HelloOk.class);
                    completeConnectFuture(helloOk);
                    listeners.forEach(l -> l.onConnected(helloOk));
                } catch (Exception e) {
                    failConnectFuture(e);
                }
            } else if ("connect".equals(connectPending.method)) {
                failConnectFuture(
                        new RuntimeException("Connect failed: " + (error != null ? error : "unknown")));
            } else {
                // 非 connect 的普通 RPC
                connectPending.future.complete(frame);
            }
            return;
        }

        // 2. 检查是否为 chat.send 的 RPC 确认
        // chat.send 的实际回复通过 event 帧推送，但 RPC 响应也需消费
        if (activeChatStreams.containsKey(id)) {
            if (!ok && error != null) {
                ChatStreamCollector collector = activeChatStreams.remove(id);
                if (collector != null) {
                    collector.handler.onError(error.getMessage());
                }
            }
            // ok=true 时 chat.send 的内容通过 event 帧推送，无需在此处理
            return;
        }

        // 3. 普通监听器
        listeners.forEach(l -> l.onResponse(frame));
    }

    @SuppressWarnings("unchecked")
    private void handleEvent(JsonNode root) {
        String event = root.path("event").asText("");
        Object payload = null;
        Integer seq = root.has("seq") && !root.path("seq").isNull() ? root.path("seq").asInt() : null;

        if (root.has("payload") && !root.path("payload").isNull()) {
            payload = objectMapper.convertValue(root.path("payload"), Object.class);
        }

        EventFrame frame = new EventFrame("event", event, payload, seq);

        // chat 事件：处理流式回复
        if ("chat".equals(event) && payload != null) {
            handleChatEvent(payload);
        }

        // 通知监听器
        listeners.forEach(l -> l.onEvent(frame));
    }

    @SuppressWarnings("unchecked")
    private void handleChatEvent(Object payload) {
        Map<String, Object> p;
        if (payload instanceof Map) {
            p = (Map<String, Object>) payload;
        } else {
            try {
                p = objectMapper.convertValue(payload, Map.class);
            } catch (Exception e) {
                return;
            }
        }

        String runId = (String) p.get("runId");
        boolean done = Boolean.TRUE.equals(p.get("done"));
        String delta = (String) p.get("delta");

        // 匹配到活跃的 chat stream
        ChatStreamCollector collector = null;
        if (runId != null) {
            collector = activeChatStreams.get(runId);
        }
        // runId 匹配不到时，尝试匹配最后一个（兼容）
        if (collector == null && !activeChatStreams.isEmpty()) {
            collector = activeChatStreams.values().stream()
                    .reduce((first, second) -> second)
                    .orElse(null);
        }

        if (collector == null) return;

        if (delta != null) {
            collector.textBuilder.append(delta);
            collector.handler.onDelta(delta);
        }

        if (done) {
            String fullText = collector.textBuilder.toString();
            activeChatStreams.remove(collector.reqId);
            collector.handler.onComplete(fullText);
        }
    }

    // ============================================================
    // 内部类
    // ============================================================

    private static class PendingRpc {
        final String id;
        final String method;
        final long timestamp;
        final CompletableFuture<ResponseFrame> future = new CompletableFuture<>();

        PendingRpc(String id, String method, long timestamp) {
            this.id = id;
            this.method = method;
            this.timestamp = timestamp;
        }
    }

    private static class ChatStreamCollector {
        final String reqId;
        final ChatStreamHandler handler;
        final StringBuilder textBuilder = new StringBuilder();

        ChatStreamCollector(String reqId, ChatStreamHandler handler) {
            this.reqId = reqId;
            this.handler = handler;
        }
    }

    private static String generateId() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 16);
    }
}
