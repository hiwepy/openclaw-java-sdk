package io.github.hiwepy.openclaw.api;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.hiwepy.openclaw.api.OpenClawClientConfig;
import io.github.hiwepy.openclaw.api.http.OpenClawHttpClient;
import io.github.hiwepy.openclaw.api.model.*;
import io.github.hiwepy.openclaw.api.sse.SseEventHandler;
import io.github.hiwepy.openclaw.api.model.ResponseRequest;
import io.github.hiwepy.openclaw.api.model.ResponseResult;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.Objects;

/**
 * OpenAI 兼容 HTTP API 客户端。
 * <p>封装 OpenClaw Gateway 的 OpenAI 兼容端点 (/v1/*)。</p>
 */
@Slf4j
public class OpenClawOpenAiHttpClient implements AutoCloseable {

    private final OpenClawClientConfig config;
    private final ObjectMapper objectMapper;
    private final OpenClawHttpClient httpClient;

    public OpenClawOpenAiHttpClient(OpenClawClientConfig config, ObjectMapper mapper,
                                    OpenClawHttpClient httpClient) {
        this.config = Objects.requireNonNull(config, "config");
        this.objectMapper = mapper != null ? mapper : new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        this.httpClient = Objects.requireNonNull(httpClient, "httpClient");
    }

    public OpenClawOpenAiHttpClient(OpenClawClientConfig config, OpenClawHttpClient httpClient) {
        this(config, null, httpClient);
    }

    // ============================================================
    // Chat Completions
    // ============================================================

    public ChatCompletionResponse chatCompletion(ChatCompletionRequest request, Map<String, String> headers) {
        Objects.requireNonNull(request, "request");
        return httpClient.postJson("/v1/chat/completions", request, headers, ChatCompletionResponse.class);
    }

    public ChatCompletionResponse chatCompletion(ChatCompletionRequest request) {
        return chatCompletion(request, null);
    }

    // ============================================================
    // Models
    // ============================================================

    public ModelsResponse listModels() {
        return httpClient.getJson("/v1/models", ModelsResponse.class);
    }

    public ModelsResponse.ModelData getModel(String modelId) {
        Objects.requireNonNull(modelId, "modelId");
        String encoded = java.net.URLEncoder.encode(modelId, java.nio.charset.StandardCharsets.UTF_8);
        return httpClient.getJson("/v1/models/" + encoded, ModelsResponse.ModelData.class);
    }

    // ============================================================
    // Embeddings
    // ============================================================

    public EmbeddingsResponse createEmbeddings(EmbeddingsRequest request, Map<String, String> headers) {
        Objects.requireNonNull(request, "request");
        return httpClient.postJson("/v1/embeddings", request, headers, EmbeddingsResponse.class);
    }

    public EmbeddingsResponse createEmbeddings(EmbeddingsRequest request) {
        return createEmbeddings(request, null);
    }

    // ============================================================
    // OpenResponses
    // ============================================================

    public ResponseResult createResponse(ResponseRequest request, Map<String, String> headers) {
        Objects.requireNonNull(request, "request");
        return httpClient.postJson("/v1/responses", request, headers, ResponseResult.class);
    }

    public ResponseResult createResponse(ResponseRequest request) {
        return createResponse(request, null);
    }

    // ============================================================
    // Streaming (SSE)
    // ============================================================

    public void chatCompletionStream(ChatCompletionRequest request,
                                     Map<String, String> headers,
                                     SseEventHandler handler) {
        Objects.requireNonNull(request, "request");
        Objects.requireNonNull(handler, "handler");
        request.setStream(true);
        httpClient.postStream("/v1/chat/completions", request, headers, handler,
                ChatCompletionChunk.class);
    }

    public void chatCompletionStream(ChatCompletionRequest request, SseEventHandler handler) {
        chatCompletionStream(request, null, handler);
    }

    public void createResponseStream(ResponseRequest request,
                                     Map<String, String> headers,
                                     SseEventHandler handler) {
        Objects.requireNonNull(request, "request");
        Objects.requireNonNull(handler, "handler");
        request.setStream(true);
        httpClient.postStream("/v1/responses", request, headers, handler, null);
    }

    public void createResponseStream(ResponseRequest request, SseEventHandler handler) {
        createResponseStream(request, null, handler);
    }

    @Override
    public void close() {
        httpClient.close();
    }
}
