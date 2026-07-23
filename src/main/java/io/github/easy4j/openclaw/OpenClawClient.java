package io.github.easy4j.openclaw;

import io.github.easy4j.openclaw.api.*;
import io.github.easy4j.openclaw.api.sse.StreamingChatResponse;
import io.github.easy4j.openclaw.cli.OpenClawCli;
import io.github.easy4j.openclaw.cli.OpenClawCliExecutor;
import io.github.easy4j.openclaw.api.model.*;
import io.github.easy4j.openclaw.api.model.ResponseRequest;
import io.github.easy4j.openclaw.api.model.ResponseResult;
import io.github.easy4j.openclaw.api.model.ToolInvokeRequest;
import io.github.easy4j.openclaw.api.model.ToolInvokeResult;
import io.github.easy4j.openclaw.exception.OpenClawException;
import io.github.easy4j.openclaw.ws.ChatStreamHandler;
import io.github.easy4j.openclaw.ws.OpenClawGatewayWsClient;
import io.github.easy4j.openclaw.ws.OpenClawWsListener;
import io.github.easy4j.openclaw.ws.protocol.ChatSendParams;
import io.github.easy4j.openclaw.ws.protocol.HelloOk;
import io.github.easy4j.openclaw.ws.protocol.SessionsSendParams;
import io.github.easy4j.openclaw.ws.protocol.result.SessionsSendResult;

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
 *     <li><b>Webhook HTTP</b>：{@link #hook} / {@link #wake} — 触发 agent、注入事件、自定义 webhook</li>
 *     <li><b>OpenAI 兼容 HTTP</b>：{@link #chatCompletion} / {@link #listModels} / {@link #createEmbeddings} — OpenAI 标准 API</li>
 *     <li><b>OpenResponses HTTP</b>：{@link #createResponse} — Item-based 输入，客户端工具调用</li>
 *     <li><b>Tools Invoke HTTP</b>：{@link #toolInvoke} — 直接调用单个工具</li>
 *     <li><b>WebSocket</b>：{@link #ws()} — 双向实时通信，流式对话，完整 RPC</li>
 *     <li><b>CLI</b>：{@link #cli()} — 本地 {@code openclaw} 命令封装</li>
 * </ul>
 *
 * @see OpenClawGatewayWsClient
 * @see OpenClawChatClient
 * @see OpenClawEmbeddingsClient
 * @see OpenClawResponsesClient
 * @see OpenClawToolInvokeClient
 */
@Slf4j
public class OpenClawClient implements AutoCloseable {

    private final OpenClawWebhookClient gatewayHttpClient;
    private final OpenClawChatClient chatClient;
    private final OpenClawEmbeddingsClient embeddingsClient;
    private final OpenClawResponsesClient responsesClient;
    private final OpenClawToolInvokeClient toolsInvokeClient;
    private final OpenClawCli cli;
    private final OpenClawGatewayWsClient wsClient;

    /**
     * 标准构造（自动创建 HTTP、CLI、WS 客户端）。
     */
    public OpenClawClient(OpenClawHttpClientConfig httpConfig, OpenClawCliConfig cliConfig) {
        this(httpConfig, cliConfig, null, null);
    }

    /**
     * 带 ObjectMapper 和 OkHttpClient 的构造（HTTP 客户端共享同一个 OkHttpClient）。
     */
    public OpenClawClient(OpenClawHttpClientConfig httpConfig, OpenClawCliConfig cliConfig, com.fasterxml.jackson.databind.ObjectMapper objectMapper, okhttp3.OkHttpClient httpClient) {
        Objects.requireNonNull(httpConfig, "httpConfig");
        Objects.requireNonNull(cliConfig, "cliConfig");
        OpenClawCliExecutor exec = new OpenClawCliExecutor(cliConfig);
        this.gatewayHttpClient = new OpenClawWebhookClient(httpConfig, objectMapper, httpClient);
        this.chatClient = new OpenClawChatClient(httpConfig, objectMapper, httpClient);
        this.embeddingsClient = new OpenClawEmbeddingsClient(httpConfig, objectMapper, httpClient);
        this.responsesClient = new OpenClawResponsesClient(httpConfig, objectMapper, httpClient);
        this.toolsInvokeClient = new OpenClawToolInvokeClient(httpConfig, objectMapper, httpClient);
        this.cli = new OpenClawCli(exec);
        this.wsClient = new OpenClawGatewayWsClient(httpConfig);
    }

    /**
     * 兼容性构造（用于测试等简易场景，只传入 config 和 gatewayHttpClient）。
     */
    public OpenClawClient(OpenClawHttpClientConfig httpConfig, OpenClawCliConfig cliConfig, OpenClawWebhookClient gatewayHttpClient) {
        Objects.requireNonNull(httpConfig, "httpConfig");
        Objects.requireNonNull(cliConfig, "cliConfig");
        this.gatewayHttpClient = Objects.requireNonNull(gatewayHttpClient, "gatewayHttpClient");
        this.chatClient = new OpenClawChatClient(httpConfig);
        this.embeddingsClient = new OpenClawEmbeddingsClient(httpConfig);
        this.responsesClient = new OpenClawResponsesClient(httpConfig);
        this.toolsInvokeClient = new OpenClawToolInvokeClient(httpConfig);
        this.cli = new OpenClawCli(new OpenClawCliExecutor(cliConfig));
        this.wsClient = new OpenClawGatewayWsClient(httpConfig);
    }

    /**
     * 完整依赖注入（用于测试或自定义组件）。
     */
    public OpenClawClient(OpenClawHttpClientConfig httpConfig,
                          OpenClawCliConfig cliConfig,
                          OpenClawWebhookClient gatewayHttpClient,
                          OpenClawChatClient chatClient,
                          OpenClawEmbeddingsClient embeddingsClient,
                          OpenClawResponsesClient responsesClient,
                          OpenClawToolInvokeClient toolsInvokeClient,
                          OpenClawCli cli,
                          OpenClawGatewayWsClient wsClient) {
        Objects.requireNonNull(httpConfig, "httpConfig");
        Objects.requireNonNull(cliConfig, "cliConfig");
        this.gatewayHttpClient = Objects.requireNonNull(gatewayHttpClient, "gatewayHttpClient");
        this.chatClient = Objects.requireNonNull(chatClient, "chatClient");
        this.embeddingsClient = Objects.requireNonNull(embeddingsClient, "embeddingsClient");
        this.responsesClient = Objects.requireNonNull(responsesClient, "responsesClient");
        this.toolsInvokeClient = Objects.requireNonNull(toolsInvokeClient, "toolsInvokeClient");
        this.cli = Objects.requireNonNull(cli, "cli");
        this.wsClient = Objects.requireNonNull(wsClient, "wsClient");
    }

    // ============================================================
    // HTTP Webhook
    // ============================================================


    /**
     * 注入系统事件。
     * <p>对应 {@code POST /hooks/wake}。</p>
     *
     * @param text 事件文本
     * @param mode 唤醒模式（如 {@code "now"}）
     * @return 响应结果
     */
    public String wake(String text, String mode) {
        return gatewayHttpClient.postHooksWake(text, mode);
    }

    /**
     * 调用映射 webhook。
     * <p>对应 {@code POST /hooks/<name>}。</p>
     *
     * @param hookName webhook 名称
     * @param payload 请求数据
     * @return 响应结果
     */
    public String hook(String hookName, Map<String, Object> payload) {
        return gatewayHttpClient.postMappedHook(hookName, payload);
    }

    /**
     * 触发智能体。
     * <p>对应 {@code POST /hooks/agent}。</p>
     *
     * @param request 请求体
     * @return 智能体响应
     */
    public HookResponse hook(HookRequest request) {
        return gatewayHttpClient.postHooksAgent(request);
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
     * 连接 WebSocket 并阻塞等待握手完成。
     *
     * @return 握手结果
     */
    public HelloOk connect() {
        try {
            return wsClient.connectHandshake();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new OpenClawException("WS connect interrupted", e);
        }
    }

    /**
     * 异步连接 WebSocket。
     */
    public CompletableFuture<HelloOk> connectAsync() {
        return wsClient.connectHandshakeAsync();
    }

    /**
     * 通过 WebSocket 发送流式聊天。
     *
     * @param message 消息文本
     * @param handler 流式处理器
     */
    public void chatSend(String message, ChatStreamHandler handler) {
        wsClient.chatSend(ChatSendParams.builder().message(message).build(), handler);
    }

    /**
     * 通过 WebSocket 发送流式聊天（指定会话）。
     *
     * @param sessionKey 会话键
     * @param message    消息文本
     * @param handler    流式处理器
     */
    public void chatSend(String sessionKey, String message, ChatStreamHandler handler) {
        wsClient.chatSend(
                ChatSendParams.builder().sessionKey(sessionKey).message(message).build(),
                handler);
    }

    /**
     * 通过 WebSocket 向指定会话发消息（非流式 RPC）。
     */
    public SessionsSendResult sessionsSend(String sessionKey, String message) {
        return wsClient.sessionsSend(
                SessionsSendParams.builder().key(sessionKey).message(message).build());
    }

    /**
     * 添加 WebSocket 事件监听器。
     */
    public OpenClawClient addWsListener(OpenClawWsListener listener) {
        wsClient.addListener(listener);
        return this;
    }

    // ============================================================
    // OpenAI 兼容 HTTP API（/v1/*）
    // ============================================================

    /**
     * 获取 Chat Completions 客户端。
     */
    public OpenClawChatClient chat() {
        return chatClient;
    }

    /**
     * 获取 Embeddings 客户端。
     */
    public OpenClawEmbeddingsClient embeddings() {
        return embeddingsClient;
    }

    /**
     * 获取 Responses 客户端。
     */
    public OpenClawResponsesClient responses() {
        return responsesClient;
    }

    // ----------------------------------------------------------------
    // Chat Completions 快捷方法
    // ----------------------------------------------------------------

    /**
     * 发送 Chat Completions 请求（非流式）。
     */
    public ChatResponse chatCompletion(ChatRequest request) {
        return chatClient.chatCompletion(request);
    }

    /**
     * 发送 Chat Completions 请求，携带自定义请求头。
     */
    public ChatResponse chatCompletion(ChatRequest request, OpenClawHeaders.Builder headersBuilder) {
        Map<String, String> headers = headersBuilder != null ? headersBuilder.build() : null;
        return chatClient.chatCompletion(request, headers);
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
     * <pre>{@code
     * client.chatCompletionStream(request)
     *     .onDelta(delta -> System.out.print(delta))
     *     .onComplete(text -> System.out.println("\n完成"))
     *     .onError(error -> error.printStackTrace());
     * }</pre>
     *
     * @param request 请求体（自动设 stream=true）
     * @return 流式响应，支持链式回调
     */
    public StreamingChatResponse chatCompletionStream(ChatRequest request) {
        return chatClient.chatCompletionStream(request);
    }

    /**
     * 流式 chat completion，使用 Builder 模式注册回调。
     */
    public StreamingChatResponse chatCompletionStream(ChatRequest request, StreamingChatResponse.Builder callbackBuilder) {
        return chatClient.chatCompletionStream(request, callbackBuilder);
    }

    /**
     * 流式 chat completion，按 sessionKey 路由。
     */
    public StreamingChatResponse chatCompletionStreamWithSession(ChatRequest request, String sessionKey) {
        Map<String, String> headers = OpenClawHeaders.builder().sessionKey(sessionKey).build();
        return chatClient.chatCompletionStream(request, headers);
    }

    // ----------------------------------------------------------------
    // Models
    // ----------------------------------------------------------------

    /**
     * 获取可用模型/agent 目标列表。
     */
    public ModelsResponse listModels() {
        return chatClient.listModels();
    }

    // ----------------------------------------------------------------
    // Embeddings
    // ----------------------------------------------------------------

    /**
     * 创建嵌入向量。
     */
    public EmbeddingsResponse createEmbeddings(EmbeddingsRequest request) {
        return embeddingsClient.createEmbeddings(request);
    }

    // ----------------------------------------------------------------
    // Responses
    // ----------------------------------------------------------------

    /**
     * 发送 OpenResponses 请求。
     */
    public ResponseResult createResponse(ResponseRequest request) {
        return responsesClient.createResponse(request);
    }

    // ============================================================
    // Tools Invoke（/tools/invoke）
    // ============================================================

    /**
     * 获取 Tools Invoke 客户端实例。
     */
    public OpenClawToolInvokeClient toolsInvoke() {
        return toolsInvokeClient;
    }

    /**
     * 调用单个工具。
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
        closeQuietly(chatClient);
        closeQuietly(embeddingsClient);
        closeQuietly(responsesClient);
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
}
