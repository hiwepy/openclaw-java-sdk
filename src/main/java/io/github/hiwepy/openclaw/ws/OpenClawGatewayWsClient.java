package io.github.hiwepy.openclaw.ws;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.hiwepy.openclaw.OpenClawClientConfig;
import io.github.hiwepy.openclaw.ws.protocol.*;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
public class OpenClawGatewayWsClient extends WebSocketClient {

    private static final Logger log = LoggerFactory.getLogger(OpenClawGatewayWsClient.class);
    private static final int PROTOCOL_VERSION = 1;

    private final OpenClawClientConfig config;
    private final ObjectMapper objectMapper;
    private final List<OpenClawWsListener> listeners = new CopyOnWriteArrayList<>();

    /** RPC 请求 ID → PendingRpc */
    private final Map<String, PendingRpc> pendingRpcs = new ConcurrentHashMap<>();

    /** chat.send 请求 ID → ChatStreamCollector */
    private final Map<String, ChatStreamCollector> activeChatStreams = new ConcurrentHashMap<>();

    private final ReentrantLock connectLock = new ReentrantLock();
    private final AtomicReference<HelloOk> helloOkRef = new AtomicReference<>();
    private final CompletableFuture<HelloOk> connectFuture = new CompletableFuture<>();

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
        if (base == null || base.isBlank()) {
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
                case "res" -> handleResponse(root);
                case "event" -> handleEvent(root);
                case "req" -> log.debug("Received unexpected req frame from Gateway: {}", root);
                default -> log.warn("Unknown frame type: {}", type);
            }
        } catch (Exception e) {
            log.error("Failed to parse WebSocket message: {}", message, e);
        }
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        log.info("WebSocket closed: code={}, reason={}, remote={}", code, reason, remote);
        // 取消所有 pending RPC
        PendingRpc pending;
        for (var entry : pendingRpcs.entrySet()) {
            entry.getValue().future.completeExceptionally(
                    new RuntimeException("WebSocket closed: " + reason));
        }
        pendingRpcs.clear();
        // 清理 chat streams
        for (var entry : activeChatStreams.entrySet()) {
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

    private void sendConnectHandshake() {
        ConnectParams.AuthInfo auth = null;
        String token = config.getGatewayAuthToken();
        String password = config.getGatewayAuthPassword();
        if (token != null && !token.isBlank()) {
            auth = ConnectParams.AuthInfo.token(token);
        } else if (password != null && !password.isBlank()) {
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

        // 注册 connect 的 pending RPC
        PendingRpc pending = new PendingRpc(reqId, "connect", System.currentTimeMillis());
        pendingRpcs.put(reqId, pending);

        try {
            String json = objectMapper.writeValueAsString(req);
            log.debug("Sending connect handshake: {}", json);
            send(json);
        } catch (JsonProcessingException e) {
            connectFuture.completeExceptionally(e);
        }
    }

    /**
     * 连接 Gateway WebSocket 并阻塞等待握手完成。
     * <p>内部调用 {@code super.connectBlocking()} 建立 TCP 连接，然后等待 {@code connect} RPC 握手响应。</p>
     *
     * @return 握手结果（hello-ok）
     * @throws InterruptedException 线程被中断
     */
    public HelloOk connectHandshake() throws InterruptedException {
        super.connectBlocking();
        try {
            return connectFuture.get(30, TimeUnit.SECONDS);
        } catch (ExecutionException | TimeoutException e) {
            throw new RuntimeException("Gateway WS connect handshake failed", e);
        }
    }

    /**
     * 异步连接并等待握手完成。
     */
    public CompletableFuture<HelloOk> connectHandshakeAsync() {
        super.connect();
        return connectFuture;
    }

    /**
     * 获取握手信息（仅握手完成后非空）。
     */
    public HelloOk getHelloOk() {
        return helloOkRef.get();
    }

    // ============================================================
    // RPC 调用
    // ============================================================

    /**
     * 发送 RPC 请求并等待响应。
     *
     * @param method RPC 方法名
     * @param params 请求参数
     * @param timeoutMs 超时毫秒
     * @return 响应帧
     */
    public ResponseFrame rpc(String method, Map<String, Object> params, long timeoutMs) {
        String reqId = generateId();
        PendingRpc pending = new PendingRpc(reqId, method, System.currentTimeMillis());
        pendingRpcs.put(reqId, pending);

        try {
            RequestFrame req = new RequestFrame(reqId, method, params);
            String json = objectMapper.writeValueAsString(req);
            send(json);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize RPC request", e);
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

    /**
     * 发送 RPC 请求（默认超时 120s）。
     */
    public ResponseFrame rpc(String method, Map<String, Object> params) {
        return rpc(method, params, 120_000);
    }

    /**
     * 发送 RPC 请求（无参数）。
     */
    public ResponseFrame rpc(String method) {
        return rpc(method, null, 120_000);
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
            send(json);
        } catch (JsonProcessingException e) {
            activeChatStreams.remove(reqId);
            handler.onError("Failed to serialize chat.send request: " + e.getMessage());
        }
    }

    // ============================================================
    // sessions.send
    // ============================================================

    /**
     * 通过 {@code sessions.send} 向指定会话发消息（非流式，返回 RPC 响应）。
     *
     * @param params 会话发送参数
     * @return RPC 响应
     */
    public ResponseFrame sessionsSend(SessionsSendParams params) {
        return rpc("sessions.send", params.toParamsMap());
    }

    // ============================================================
    // 常用便捷方法
    // ============================================================

    /**
     * 获取会话列表。
     */
    public ResponseFrame sessionsList() {
        return rpc("sessions.list", Map.of());
    }

    /**
     * 获取聊天历史。
     *
     * @param sessionKey 会话键
     * @param limit      历史条数限制
     */
    public ResponseFrame chatHistory(String sessionKey, int limit) {
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("sessionKey", sessionKey);
        params.put("limit", limit);
        return rpc("chat.history", params);
    }

    /**
     * 中止当前聊天。
     *
     * @param sessionKey 会话键
     */
    public ResponseFrame chatAbort(String sessionKey) {
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("sessionKey", sessionKey);
        return rpc("chat.abort", params);
    }

    /**
     * 获取智能体信息。
     */
    public ResponseFrame agent() {
        return rpc("agent");
    }

    /**
     * 获取智能体身份。
     */
    public ResponseFrame agentIdentityGet() {
        return rpc("agent.identity.get");
    }

    /**
     * 列出 cron 任务。
     */
    public ResponseFrame cronList() {
        return rpc("cron.list", Map.of());
    }

    /**
     * 获取配置。
     */
    public ResponseFrame configGet() {
        return rpc("config.get");
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
                    helloOkRef.set(helloOk);
                    connectFuture.complete(helloOk);
                    listeners.forEach(l -> l.onConnected(helloOk));
                } catch (Exception e) {
                    connectFuture.completeExceptionally(e);
                }
            } else if ("connect".equals(connectPending.method)) {
                connectFuture.completeExceptionally(
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
