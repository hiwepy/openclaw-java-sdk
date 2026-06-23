package io.github.hiwepy.openclaw;

import io.github.hiwepy.openclaw.api.*;
import io.github.hiwepy.openclaw.api.sse.SseStreamReader;
import io.github.hiwepy.openclaw.api.sse.StreamingChatResponse;
import io.github.hiwepy.openclaw.cli.OpenClawCli;
import io.github.hiwepy.openclaw.cli.OpenClawCliExecutor;
import io.github.hiwepy.openclaw.api.model.*;
import io.github.hiwepy.openclaw.api.model.ResponseRequest;
import io.github.hiwepy.openclaw.api.model.ResponseResult;
import io.github.hiwepy.openclaw.api.model.ToolInvokeRequest;
import io.github.hiwepy.openclaw.api.model.ToolInvokeResult;
import io.github.hiwepy.openclaw.ws.ChatStreamHandler;
import io.github.hiwepy.openclaw.ws.OpenClawGatewayWsClient;
import io.github.hiwepy.openclaw.ws.OpenClawWsListener;
import io.github.hiwepy.openclaw.ws.protocol.ChatSendParams;
import io.github.hiwepy.openclaw.ws.protocol.HelloOk;
import io.github.hiwepy.openclaw.ws.protocol.SessionsSendParams;
import io.github.hiwepy.openclaw.ws.protocol.result.SessionsSendResult;

import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

/**
 * 门面：<b>HTTP Webhooks</b>（{@code /hooks/*}）+ <b>OpenAI 兼容 API</b>（{@code /v1/*}）+ <b>Tools Invoke</b>（{@code /tools/invoke}）+ <b>WebSocket 控制面</b> + 通用本地 CLI（{@link #cli()}）。
 * <p>
 * 五条通信通道相互独立：
 * </p>
 * <ul>
 *     <li><b>Webhook HTTP</b>：{@link #agent} / {@link #wake} / {@link #hook} — 无状态触发</li>
 *     <li><b>OpenAI 兼容 HTTP</b>：{@link #chatCompletion} / {@link #listModels} / {@link #createEmbeddings} — OpenAI 标准 API</li>
 *     <li><b>OpenResponses HTTP</b>：{@link #createResponse} — Item-based 输入，客户端工具调用</li>
 *     <li><b>Tools Invoke HTTP</b>：{@link #toolInvoke} — 直接调用单个工具</li>
 *     <li><b>WebSocket</b>：{@link #ws()} — 双向实时通信，流式对话，完整 RPC</li>
 *     <li><b>CLI</b>：{@link #cli()} — 本地 {@code openclaw} 命令封装</li>
 * </ul>
 *
 * @see OpenClawGatewayWsClient
 * @see OpenClawOpenAiHttpClient
 * @see OpenClawToolsInvokeClient
 */
@Slf4j
public class OpenClawClient implements AutoCloseable {

    private final OpenClawHttpClientConfig httpConfig;
    private final OpenClawCliConfig cliConfig;
    private final OpenClawGatewayHttpClient gatewayHttpClient;
    private final OpenClawOpenAiHttpClient openAiHttpClient;
    private final OpenClawToolsInvokeClient toolsInvokeClient;
    private final OpenClawCli cli;
    private final OpenClawGatewayWsClient wsClient;

    /**
     * 标准构造（自动创建 HTTP、CLI、WS 客户端）。
     */
    public OpenClawClient(OpenClawHttpClientConfig httpConfig, OpenClawCliConfig cliConfig) {
        this(httpConfig, cliConfig, null, null);
    }

    /**
     * 带 ObjectMapper 和 OkHttpClient 的构造（三个 HTTP 客户端共享同一个 OkHttpClient）。
     */
    public OpenClawClient(OpenClawHttpClientConfig httpConfig, OpenClawCliConfig cliConfig, com.fasterxml.jackson.databind.ObjectMapper objectMapper, okhttp3.OkHttpClient httpClient) {
        this.httpConfig = Objects.requireNonNull(httpConfig, "httpConfig");
        this.cliConfig = Objects.requireNonNull(cliConfig, "cliConfig");
        OpenClawCliExecutor exec = new OpenClawCliExecutor(cliConfig);
        this.gatewayHttpClient = new OpenClawGatewayHttpClient(httpConfig, objectMapper, httpClient);
        this.openAiHttpClient = new OpenClawOpenAiHttpClient(httpConfig, objectMapper, httpClient);
        this.toolsInvokeClient = new OpenClawToolsInvokeClient(httpConfig, objectMapper, httpClient);
        this.cli = new OpenClawCli(exec);
        this.wsClient = new OpenClawGatewayWsClient(httpConfig);
    }

    /**
     * 兼容性构造（用于测试等简易场景，只传入 config 和 gatewayHttpClient）。
     * <p>其他组件使用默认实现。</p>
     */
    public OpenClawClient(OpenClawHttpClientConfig httpConfig, OpenClawCliConfig cliConfig, OpenClawGatewayHttpClient gatewayHttpClient) {
        this.httpConfig = Objects.requireNonNull(httpConfig, "httpConfig");
        this.cliConfig = Objects.requireNonNull(cliConfig, "cliConfig");
        this.gatewayHttpClient = Objects.requireNonNull(gatewayHttpClient, "gatewayHttpClient");
        this.openAiHttpClient = new OpenClawOpenAiHttpClient(httpConfig);
        this.toolsInvokeClient = new OpenClawToolsInvokeClient(httpConfig);
        this.cli = new OpenClawCli(new OpenClawCliExecutor(cliConfig));
        this.wsClient = new OpenClawGatewayWsClient(httpConfig);
    }

    /**
     * 完整依赖注入（用于测试或自定义组件）。
     */
    public OpenClawClient(OpenClawHttpClientConfig httpConfig,
                          OpenClawCliConfig cliConfig,
                          OpenClawGatewayHttpClient gatewayHttpClient,
                          OpenClawOpenAiHttpClient openAiHttpClient,
                          OpenClawToolsInvokeClient toolsInvokeClient,
                          OpenClawCli cli,
                          OpenClawGatewayWsClient wsClient) {
        this.httpConfig = Objects.requireNonNull(httpConfig, "httpConfig");
        this.cliConfig = Objects.requireNonNull(cliConfig, "cliConfig");
        this.gatewayHttpClient = Objects.requireNonNull(gatewayHttpClient, "gatewayHttpClient");
        this.openAiHttpClient = Objects.requireNonNull(openAiHttpClient, "openAiHttpClient");
        this.toolsInvokeClient = Objects.requireNonNull(toolsInvokeClient, "toolsInvokeClient");
        this.cli = Objects.requireNonNull(cli, "cli");
        this.wsClient = Objects.requireNonNull(wsClient, "wsClient");
    }

    // ============================================================
    // HTTP Webhook
    // ============================================================

    /**
     * 通过 Gateway HTTP Webhook（{@code POST /hooks/agent}）触发智能体。
     */
    public HookResponse agent(HookRequest request) {
        return gatewayHttpClient.postHooksAgent(request);
    }


    /**
     * 一次性调用（无会话保持）。
     */
    public HookResponse agentOneShot(HookRequest request) {
        return agent(request);
    }

    /**
     * 带 peer 的一次性调用。
     */
    public HookResponse agentOneShotForPeer(String peerId, HookRequest request) {
        return agent(request);
    }

    /**
     * 带 peer 和 correlationId 的一次性调用。
     */
    public HookResponse agentOneShotForPeer(String peerId, String correlationId, HookRequest request) {
        return agent(request);
    }

    /**
     * 稳定会话调用。
     */
    public HookResponse agentWithStableSession(String agentId, String peerId, HookRequest request) {
        return agent(request);
    }

    /**
     * @deprecated 请改用 {@link #agent(HookRequest)}
     */
    @Deprecated
    public HookResponse invokeViaGateway(HookRequest request) {
        return agent(request);
    }

    /**
     * 调用 {@code POST /hooks/wake} 注入系统事件。
     */
    public String wake(String text, String mode) {
        return gatewayHttpClient.postHooksWake(text, mode);
    }

    /**
     * @deprecated 请改用 {@link #wake(String, String)}
     */
    @Deprecated
    public String wakeViaWebhook(String text, String mode) {
        return wake(text, mode);
    }

    /**
     * 调用映射 webhook {@code POST /hooks/<name>}。
     */
    public String hook(String hookName, Map<String, Object> payload) {
        return gatewayHttpClient.postMappedHook(hookName, payload);
    }

    /**
     * @deprecated 请改用 {@link #hook(String, Map)}
     */
    @Deprecated
    public String invokeMappedWebhook(String hookName, Map<String, Object> payload) {
        return hook(hookName, payload);
    }

    // ============================================================
    // WebSocket 控制面
    // ============================================================

    /**
     * 获取 WebSocket 客户端实例。
     * <p>通过 WS 客户端可实现：流式对话（{@code chat.send}）、会话管理、cron 管理、配置管理等。</p>
     *
     * <pre>{@code
     * OpenClawGatewayWsClient ws = client.ws();
     * ws.addListener(new OpenClawWsListener() { ... });
     * ws.connectBlocking();
     * ws.chatSend(ChatSendParams.builder().message("你好").build(), handler);
     * }</pre>
     *
     * @return WebSocket 客户端
     */
    public OpenClawGatewayWsClient ws() {
        return wsClient;
    }

    /**
     * 快捷方式：连接 WS 并阻塞等待握手完成。
     *
     * @return 握手结果
     */
    public HelloOk wsConnect() {
        try {
            return wsClient.connectHandshake();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new io.github.hiwepy.openclaw.exception.OpenClawException("WS connect interrupted", e);
        }
    }

    /**
     * 快捷方式：异步连接 WS。
     */
    public CompletableFuture<HelloOk> wsConnectAsync() {
        return wsClient.connectHandshakeAsync();
    }

    /**
     * 快捷方式：通过 WS 发送流式聊天。
     *
     * @param message 消息文本
     * @param handler 流式处理器
     */
    public void wsChatSend(String message, ChatStreamHandler handler) {
        wsClient.chatSend(ChatSendParams.builder().message(message).build(), handler);
    }

    /**
     * 快捷方式：通过 WS 发送流式聊天（指定会话）。
     *
     * @param sessionKey 会话键
     * @param message    消息文本
     * @param handler    流式处理器
     */
    public void wsChatSend(String sessionKey, String message, ChatStreamHandler handler) {
        wsClient.chatSend(
                ChatSendParams.builder().sessionKey(sessionKey).message(message).build(),
                handler);
    }

    /**
     * 快捷方式：通过 WS 向指定会话发消息（非流式 RPC）。
     */
    public SessionsSendResult wsSessionsSend(String sessionKey, String message) {
        return wsClient.sessionsSend(
                SessionsSendParams.builder().key(sessionKey).message(message).build());
    }

    /**
     * 快捷方式：添加 WS 事件监听器。
     */
    public OpenClawClient addWsListener(OpenClawWsListener listener) {
        wsClient.addListener(listener);
        return this;
    }

    // ============================================================
    // OpenAI 兼容 HTTP API（/v1/*）
    // ============================================================

    /**
     * 获取 OpenAI 兼容 HTTP 客户端实例。
     * <p>
     * 通过此客户端可直接访问 OpenAI 标准 API：
     * <ul>
     *   <li>{@code POST /v1/chat/completions} - Chat Completions</li>
     *   <li>{@code GET /v1/models} - 模型列表</li>
     *   <li>{@code POST /v1/embeddings} - 嵌入向量</li>
     *   <li>{@code POST /v1/responses} - OpenResponses</li>
     * </ul>
     * </p>
     *
     * @return OpenAI 兼容 HTTP 客户端
     */
    public OpenClawOpenAiHttpClient openai() {
        return openAiHttpClient;
    }

    /**
     * 发送 Chat Completions 请求（非流式）。
     * <p>
     * 快捷方式，等价于 {@code openai().chatCompletion(request)}。
     * </p>
     *
     * @param request 请求体
     * @return Chat Completions 响应
     */
    public ChatResponse chatCompletion(ChatRequest request) {
        return openAiHttpClient.chatCompletion(request);
    }

    /**
     * 发送 chat completion 请求，携带自定义请求头。
     *
     * @param request 请求体
     * @param headers 自定义请求头
     * @return Chat Completions 响应
     */
    public ChatResponse chatCompletion(ChatRequest request, OpenClawHeaders.Builder headers) {
        return openAiHttpClient.chatCompletion(request, headers);
    }

    /**
     * 发送 chat completion 请求，按 sessionKey 路由会话。
     *
     * @param request    请求体
     * @param sessionKey 会话路由 key（如 {@code hook:<agentId>:<peerId>}）
     * @return Chat Completions 响应
     */
    public ChatResponse chatCompletionWithSession(ChatRequest request, String sessionKey) {
        return chatCompletion(request, OpenClawHeaders.builder().sessionKey(sessionKey));
    }

    // ----------------------------------------------------------------
    // Convenience methods（便捷方法）
    // ----------------------------------------------------------------

    /**
     * 发送 chat completion 请求（简洁写法）。
     * <p>
     * 示例：
     * <pre>{@code
     * client.chatCompletion("openclaw/default", List.of(ChatMessage.ofUser("Hello")));
     * }</pre>
     * </p>
     *
     * @param agent    Agent 目标（如 {@code "openclaw/default"}）
     * @param messages 消息列表
     * @return Chat Completions 响应
     */
    public ChatResponse chatCompletion(String agent, List<ChatMessage> messages) {
        return this.chatCompletion(ChatRequest.builder().agent(agent).messages(messages).build());
    }

    /**
     * 发送 chat completion 请求（指定 Agent 和后端模型）。
     * <p>
     * 示例：
     * <pre>{@code
     * client.chatCompletion("openclaw/default", "gpt-4o", List.of(ChatMessage.ofUser("Hello")));
     * }</pre>
     * </p>
     *
     * @param agent    Agent 目标（如 {@code "openclaw/default"}）
     * @param model    后端 LLM 模型（如 {@code "gpt-4o"}）
     * @param messages 消息列表
     * @return Chat Completions 响应
     */
    public ChatResponse chatCompletion(String agent, String model, List<ChatMessage> messages) {
        return this.chatCompletion(ChatRequest.builder().agent(agent).model(model).messages(messages).build());
    }

    /**
     * 发送 chat completion 请求（指定 Agent、模型和用户标识）。
     *
     * @param agent    Agent 目标
     * @param model    后端 LLM 模型
     * @param user     用户标识（用于派生稳定 session）
     * @param messages 消息列表
     * @return Chat Completions 响应
     */
    public ChatResponse chatCompletion(String agent, String model, String user, List<ChatMessage> messages) {
        return this.chatCompletion(ChatRequest.builder().agent(agent).model(model).user(user).messages(messages).build());
    }

    /**
     * 发送流式 chat completion 请求（指定 Agent）。
     *
     * @param agent    Agent 目标
     * @param messages 消息列表
     * @return 流式响应
     */
    public StreamingChatResponse chatCompletionStream(String agent, List<ChatMessage> messages) {
        return this.chatCompletionStream(ChatRequest.builder().agent(agent).messages(messages).build());
    }

    /**
     * 发送流式 chat completion 请求（指定 Agent 和后端模型）。
     *
     * @param agent    Agent 目标
     * @param model    后端 LLM 模型
     * @param messages 消息列表
     * @return 流式响应
     */
    public StreamingChatResponse chatCompletionStream(String agent, String model, List<ChatMessage> messages) {
        return this.chatCompletionStream(ChatRequest.builder().agent(agent).model(model).messages(messages).build());
    }

    /**
     * 发送流式 chat completion 请求（指定 Agent、模型和用户标识）。
     *
     * @param agent    Agent 目标
     * @param model    后端 LLM 模型
     * @param user     用户标识（用于派生稳定 session）
     * @param messages 消息列表
     * @return 流式响应
     */
    public StreamingChatResponse chatCompletionStream(String agent, String model, String user, List<ChatMessage> messages) {
        return this.chatCompletionStream(ChatRequest.builder().agent(agent).model(model).user(user).messages(messages).build());
    }

    // ----------------------------------------------------------------
    // Streaming（对齐 Hermes chatCompletionStream）
    // ----------------------------------------------------------------

    /**
     * 流式 chat completion，返回 {@link StreamingChatResponse}。
     *
     * @param request 请求体（自动设 stream=true）
     * @return 流式响应
     */
    public StreamingChatResponse chatCompletionStream(ChatRequest request) {
        okhttp3.Response response = openAiHttpClient.chatCompletionStream(request);
        return consumeStream(response);
    }

    public StreamingChatResponse chatCompletionStreamWithSession(ChatRequest request, String sessionKey) {
        Map<String, String> headers = OpenClawHeaders.builder().sessionKey(sessionKey).build();
        okhttp3.Response response = openAiHttpClient.chatCompletionStream(request, headers);
        return consumeStream(response);
    }

    private StreamingChatResponse consumeStream(okhttp3.Response response) {
        StreamingChatResponse stream = new StreamingChatResponse();
        SseStreamReader reader = new SseStreamReader();
        CompletableFuture.runAsync(() -> {
            try {
                if (response.body() != null) {
                    reader.readChatCompletionStream(response.body().byteStream(), stream);
                }
                response.close();
            } catch (Exception e) { stream.completeExceptionally(e); }
        });
        return stream;
    }

    /**
     * 获取可用模型/agent 目标列表。
     * <p>
     * 快捷方式，等价于 {@code openai().listModels()}。
     * 返回 OpenClaw agent 目标（如 {@code openclaw}、{@code openclaw/default}）。
     * </p>
     *
     * @return 模型列表
     */
    public ModelsResponse listModels() {
        return openAiHttpClient.listModels();
    }

    /**
     * 创建嵌入向量。
     * <p>
     * 快捷方式，等价于 {@code openai().createEmbeddings(request)}。
     * </p>
     *
     * @param request 请求体
     * @return 嵌入向量响应
     */
    public EmbeddingsResponse createEmbeddings(EmbeddingsRequest request) {
        return openAiHttpClient.createEmbeddings(request);
    }

    /**
     * 发送 OpenResponses 请求（非流式）。
     * <p>
     * 快捷方式，等价于 {@code openai().createResponse(request)}。
     * OpenResponses 支持 Item-based 输入、客户端工具调用、图片和文件输入。
     * </p>
     *
     * @param request 请求体
     * @return OpenResponses 响应
     */
    public ResponseResult createResponse(ResponseRequest request) {
        return openAiHttpClient.createResponse(request);
    }

    /**
     * 发送流式 OpenResponses 请求。
     * <p>
     * 流式事件类型：{@code response.created}、{@code response.in_progress}、{@code response.output_item.added} 等。
     * 流以 {@code data: [DONE]} 结束。
     * </p>
     *
     * @param request 请求体
     * @return OkHttp 响应
     */
    public okhttp3.Response createResponseStream(ResponseRequest request) {
        return openAiHttpClient.createResponseStream(request);
    }

    // ============================================================
    // Tools Invoke（/tools/invoke）
    // ============================================================

    /**
     * 获取 Tools Invoke 客户端实例。
     * <p>
     * 通过此客户端可直接调用单个工具而无需运行完整 agent 回合。
     * </p>
     *
     * @return Tools Invoke 客户端
     */
    public OpenClawToolsInvokeClient toolsInvoke() {
        return toolsInvokeClient;
    }

    /**
     * 调用单个工具。
     * <p>
     * 快捷方式，等价于 {@code toolsInvoke().invoke(request)}。
     * 工具通过 Gateway 鉴权 + 工具策略过滤。
     * </p>
     *
     * @param request 工具调用请求
     * @return 工具调用结果
     */
    public ToolInvokeResult toolInvoke(ToolInvokeRequest request) {
        return toolsInvokeClient.invoke(request);
    }

    // ============================================================
    // CLI
    // ============================================================

    /**
     * 官方 CLI 顶层命令封装。
     */
    public OpenClawCli cli() {
        return cli;
    }

    // ============================================================
    // 生命周期
    // ============================================================

    @Override
    public void close() {
        // 逐一释放所有子客户端资源，任一失败不影响其他
        closeQuietly(gatewayHttpClient);
        closeQuietly(openAiHttpClient);
        closeQuietly(toolsInvokeClient);
        closeQuietly(wsClient);
    }

    /**
     * 安全关闭资源（失败不抛异常、不影响后续释放）。
     */
    private static void closeQuietly(AutoCloseable resource) {
        if (resource == null) {
            return;
        }
        try {
            resource.close();
        } catch (Exception e) {
            log.warn("Failed to close {}: {}", resource.getClass().getSimpleName(), e.getMessage());
        }
    }
    // Session Keys
    // ============================================================

    /**
     * 复制 {@link HookRequest} 并设置 session key。
     * <p>用于 {@link OpenClawSessionKeys} 的辅助方法。</p>
     */
    public static HookRequest copyRequest(HookRequest request) {
        HookRequest copy = new HookRequest();
        copy.setMessage(request.getMessage());
        copy.setAgentId(request.getAgentId());
        copy.setName(request.getName());
        copy.setWakeMode(request.getWakeMode());
        copy.setTimeoutSeconds(request.getTimeoutSeconds());
        copy.setSessionKey(request.getSessionKey());
        copy.setDeliver(request.getDeliver());
        copy.setChannel(request.getChannel());
        copy.setTo(request.getTo());
        copy.setModel(request.getModel());
        copy.setThinking(request.getThinking());
        return copy;
    }
}
