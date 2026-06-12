package io.github.hiwepy.openclaw.api;

import io.github.hiwepy.openclaw.cli.OpenClawCli;
import io.github.hiwepy.openclaw.cli.OpenClawCliExecutor;
import io.github.hiwepy.openclaw.api.http.OpenClawHttpClient;
import io.github.hiwepy.openclaw.api.OpenClawOpenAiHttpClient;
import io.github.hiwepy.openclaw.api.model.*;
import io.github.hiwepy.openclaw.api.sse.SseEventHandler;
import io.github.hiwepy.openclaw.api.sse.StreamingChatResponse;
import io.github.hiwepy.openclaw.api.model.ResponseRequest;
import io.github.hiwepy.openclaw.api.model.ResponseResult;
import io.github.hiwepy.openclaw.api.OpenClawToolsInvokeClient;
import io.github.hiwepy.openclaw.api.model.ToolInvokeRequest;
import io.github.hiwepy.openclaw.api.model.ToolInvokeResult;
import io.github.hiwepy.openclaw.ws.ChatStreamHandler;
import io.github.hiwepy.openclaw.ws.OpenClawGatewayWsClient;
import io.github.hiwepy.openclaw.ws.OpenClawWsListener;
import io.github.hiwepy.openclaw.ws.protocol.ChatSendParams;
import io.github.hiwepy.openclaw.ws.protocol.HelloOk;
import io.github.hiwepy.openclaw.ws.protocol.SessionsSendParams;
import io.github.hiwepy.openclaw.ws.protocol.result.SessionsSendResult;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

/**
 * 门面客户端：HTTP Webhooks + OpenAI 兼容 API + Tools Invoke + WebSocket 控制面 + CLI。
 * 五条通信通道相互独立。
 */
public class OpenClawClient implements AutoCloseable {

    private final OpenClawClientConfig config;
    private final OpenClawGatewayHttpClient gatewayHttpClient;
    private final OpenClawOpenAiHttpClient openAiHttpClient;
    private final OpenClawToolsInvokeClient toolsInvokeClient;
    private final OpenClawCli cli;
    private final OpenClawGatewayWsClient wsClient;
    private final OpenClawHttpClient hooksHttpClient;
    private final OpenClawHttpClient gatewayHttpClient2;

    public OpenClawClient(OpenClawClientConfig config) {
        this.config = Objects.requireNonNull(config, "config");

        HttpClient sharedHttp = HttpClient.newBuilder()
                .connectTimeout(Duration.ofMillis(config.getConnectTimeoutMillis()))
                .version(HttpClient.Version.HTTP_1_1)
                .build();

        this.hooksHttpClient = OpenClawHttpClient.builder()
                .baseUrl(config.getGatewayBaseUrl())
                .bearerTokenProvider(config::resolveHooksBearerToken)
                .httpClient(sharedHttp)
                .connectTimeoutMillis(config.getConnectTimeoutMillis())
                .readTimeoutMillis(config.getReadTimeoutMillis())
                .verifySsl(config.isVerifySsl())
                .build();

        this.gatewayHttpClient2 = OpenClawHttpClient.builder()
                .baseUrl(config.getGatewayBaseUrl())
                .bearerTokenProvider(config::resolveGatewayBearerToken)
                .httpClient(sharedHttp)
                .connectTimeoutMillis(config.getConnectTimeoutMillis())
                .readTimeoutMillis(config.getReadTimeoutMillis())
                .verifySsl(config.isVerifySsl())
                .build();

        OpenClawCliExecutor exec = new OpenClawCliExecutor(config);
        this.gatewayHttpClient = new OpenClawGatewayHttpClient(config, hooksHttpClient);
        this.openAiHttpClient = new OpenClawOpenAiHttpClient(config, gatewayHttpClient2);
        this.toolsInvokeClient = new OpenClawToolsInvokeClient(config, gatewayHttpClient2);
        this.cli = new OpenClawCli(exec);
        this.wsClient = new OpenClawGatewayWsClient(config);
    }

    public OpenClawClient(OpenClawClientConfig config,
                          OpenClawGatewayHttpClient gatewayHttpClient) {
        this.config = Objects.requireNonNull(config, "config");
        this.gatewayHttpClient = Objects.requireNonNull(gatewayHttpClient, "gatewayHttpClient");
        this.hooksHttpClient = null;
        this.gatewayHttpClient2 = null;
        this.openAiHttpClient = new OpenClawOpenAiHttpClient(config,
                OpenClawHttpClient.builder()
                        .baseUrl(config.getGatewayBaseUrl())
                        .bearerTokenProvider(config::resolveGatewayBearerToken)
                        .build());
        this.toolsInvokeClient = new OpenClawToolsInvokeClient(config,
                OpenClawHttpClient.builder()
                        .baseUrl(config.getGatewayBaseUrl())
                        .bearerTokenProvider(config::resolveGatewayBearerToken)
                        .build());
        this.cli = new OpenClawCli(new OpenClawCliExecutor(config));
        this.wsClient = new OpenClawGatewayWsClient(config);
    }

    public OpenClawClient(OpenClawClientConfig config,
                          OpenClawGatewayHttpClient gatewayHttpClient,
                          OpenClawOpenAiHttpClient openAiHttpClient,
                          OpenClawToolsInvokeClient toolsInvokeClient,
                          OpenClawCli cli,
                          OpenClawGatewayWsClient wsClient) {
        this.config = Objects.requireNonNull(config, "config");
        this.gatewayHttpClient = Objects.requireNonNull(gatewayHttpClient, "gatewayHttpClient");
        this.openAiHttpClient = Objects.requireNonNull(openAiHttpClient, "openAiHttpClient");
        this.toolsInvokeClient = Objects.requireNonNull(toolsInvokeClient, "toolsInvokeClient");
        this.cli = Objects.requireNonNull(cli, "cli");
        this.wsClient = Objects.requireNonNull(wsClient, "wsClient");
        this.hooksHttpClient = null;
        this.gatewayHttpClient2 = null;
    }

    // ============================================================
    // HTTP Webhook
    // ============================================================

    public InvokeAgentResult agent(InvokeAgentRequest request) {
        return gatewayHttpClient.postHooksAgent(request);
    }

    public InvokeAgentResult agentOneShot(InvokeAgentRequest request) {
        return agent(request);
    }

    public InvokeAgentResult agentOneShotForPeer(String peerId, InvokeAgentRequest request) {
        return agent(request.withSessionKey(OpenClawSessionKeys.forEphemeralPeer(peerId)));
    }

    public InvokeAgentResult agentOneShotForPeer(String peerId, String correlationId,
                                                  InvokeAgentRequest request) {
        return agent(request.withSessionKey(
                OpenClawSessionKeys.forEphemeralPeer(peerId, correlationId)));
    }

    public InvokeAgentResult agentWithStableSession(String agentId, String peerId,
                                                     InvokeAgentRequest request) {
        return agent(request.withSessionKey(
                OpenClawSessionKeys.forStableSession(agentId, peerId)));
    }

    public String wake(String text, String mode) {
        return gatewayHttpClient.postHooksWake(text, mode);
    }

    public String hook(String hookName, Map<String, Object> payload) {
        return gatewayHttpClient.postMappedHook(hookName, payload);
    }

    // ============================================================
    // WebSocket
    // ============================================================

    public OpenClawGatewayWsClient ws() {
        return wsClient;
    }

    public HelloOk wsConnect() {
        try {
            return wsClient.connectHandshake();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("WS connect interrupted", e);
        }
    }

    public CompletableFuture<HelloOk> wsConnectAsync() {
        return wsClient.connectHandshakeAsync();
    }

    public void wsChatSend(String message, ChatStreamHandler handler) {
        wsClient.chatSend(ChatSendParams.builder().message(message).build(), handler);
    }

    public void wsChatSend(String sessionKey, String message, ChatStreamHandler handler) {
        wsClient.chatSend(
                ChatSendParams.builder().sessionKey(sessionKey).message(message).build(),
                handler);
    }

    public SessionsSendResult wsSessionsSend(String sessionKey, String message) {
        return wsClient.sessionsSend(
                SessionsSendParams.builder().key(sessionKey).message(message).build());
    }

    public OpenClawClient addWsListener(OpenClawWsListener listener) {
        wsClient.addListener(listener);
        return this;
    }

    // ============================================================
    // OpenAI HTTP
    // ============================================================

    public OpenClawOpenAiHttpClient openai() {
        return openAiHttpClient;
    }

    public ChatCompletionResponse chatCompletion(ChatCompletionRequest request) {
        return openAiHttpClient.chatCompletion(request);
    }

    public ModelsResponse listModels() {
        return openAiHttpClient.listModels();
    }

    public EmbeddingsResponse createEmbeddings(EmbeddingsRequest request) {
        return openAiHttpClient.createEmbeddings(request);
    }

    public ResponseResult createResponse(ResponseRequest request) {
        return openAiHttpClient.createResponse(request);
    }

    // ============================================================
    // Streaming (SSE)
    // ============================================================

    /**
     * Streaming chat completion returning a {@link StreamingChatResponse}
     * that accumulates delta chunks and completes when the stream ends.
     *
     * <pre>{@code
     * ChatCompletionChunk full = client.chatCompletionStream(req)
     *     .onDelta(text -> System.out.print(text))
     *     .get(); // blocking
     * System.out.println(full.getAccumulatedContent());
     * }</pre>
     */
    public StreamingChatResponse chatCompletionStream(ChatCompletionRequest request) {
        StreamingChatResponse stream = new StreamingChatResponse();
        openAiHttpClient.chatCompletionStream(request, stream);
        return stream;
    }

    public StreamingChatResponse chatCompletionStream(ChatCompletionRequest request,
                                                       Map<String, String> headers) {
        StreamingChatResponse stream = new StreamingChatResponse();
        openAiHttpClient.chatCompletionStream(request, headers, stream);
        return stream;
    }

    public void chatCompletionStream(ChatCompletionRequest request, SseEventHandler handler) {
        openAiHttpClient.chatCompletionStream(request, handler);
    }

    public void chatCompletionStream(ChatCompletionRequest request,
                                     Map<String, String> headers,
                                     SseEventHandler handler) {
        openAiHttpClient.chatCompletionStream(request, headers, handler);
    }

    public void createResponseStream(ResponseRequest request, SseEventHandler handler) {
        openAiHttpClient.createResponseStream(request, handler);
    }

    public void createResponseStream(ResponseRequest request,
                                     Map<String, String> headers,
                                     SseEventHandler handler) {
        openAiHttpClient.createResponseStream(request, headers, handler);
    }

    // ============================================================
    // Tools Invoke
    // ============================================================

    public OpenClawToolsInvokeClient toolsInvoke() {
        return toolsInvokeClient;
    }

    public ToolInvokeResult toolInvoke(ToolInvokeRequest request) {
        return toolsInvokeClient.invoke(request);
    }

    // ============================================================
    // CLI
    // ============================================================

    public OpenClawCli cli() {
        return cli;
    }

    // ============================================================
    // 生命周期
    // ============================================================

    @Override
    public void close() {
        gatewayHttpClient.close();
        openAiHttpClient.close();
        toolsInvokeClient.close();
        try { wsClient.close(); } catch (Exception ignored) {}
    }

    // ============================================================
    // Session Keys helper
    // ============================================================

    /**
     * Since {@link InvokeAgentRequest} is an immutable record, copying it is
     * simply returning the same instance. Use {@link InvokeAgentRequest#withSessionKey(String)}
     * to change the sessionKey.
     */
    public static InvokeAgentRequest copyRequest(InvokeAgentRequest request) {
        return request;
    }
}
