package io.github.hiwepy.openclaw.api;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.hiwepy.openclaw.OpenClawHttpClientConfig;
import io.github.hiwepy.openclaw.util.OpenClawStrings;
import io.github.hiwepy.openclaw.exception.OpenClawHttpException;
import io.github.hiwepy.openclaw.api.model.*;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * OpenAI 兼容 HTTP API 客户端。
 * <p>基于 OkHttp，支持外部传入 {@link OkHttpClient}。</p>
 */
@Slf4j
public class OpenClawOpenAiHttpClient implements AutoCloseable {

    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    private final OpenClawHttpClientConfig config;
    private final ObjectMapper objectMapper;
    private final OkHttpClient httpClient;

    public OpenClawOpenAiHttpClient(OpenClawHttpClientConfig config, ObjectMapper mapper) {
        this(config, mapper, null);
    }

    public OpenClawOpenAiHttpClient(OpenClawHttpClientConfig config) {
        this(config, null, null);
    }

    public OpenClawOpenAiHttpClient(OpenClawHttpClientConfig config, ObjectMapper mapper, OkHttpClient httpClient) {
        this.config = Objects.requireNonNull(config, "config");
        this.objectMapper = mapper != null ? mapper : new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        this.httpClient = httpClient != null ? httpClient : buildOkHttpClient(config);
    }

    private static OkHttpClient buildOkHttpClient(OpenClawHttpClientConfig config) {
        OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .connectTimeout(config.getConnectTimeoutMillis(), TimeUnit.MILLISECONDS)
                .readTimeout(config.getReadTimeoutMillis(), TimeUnit.MILLISECONDS);
        if (!config.isVerifySsl()) {
            builder.hostnameVerifier((hostname, session) -> true);
        }
        return builder.build();
    }

    // ============================================================
    // Chat Completions
    // ============================================================

    public ChatResponse chatCompletion(ChatRequest request) {
        return chatCompletion(request, null);
    }

    public ChatResponse chatCompletion(ChatRequest request, Map<String, String> headers) {
        Objects.requireNonNull(request, "request");
        String responseBody = postJson("/v1/chat/completions", request, headers);
        return parse(responseBody, ChatResponse.class, "chat completion");
    }

    /**
     * 流式 chat completion（POST /v1/chat/completions with stream=true）。
     * <p>返回 OkHttp Response，调用方用 {@code response.body().source()} 消费 SSE 流。</p>
     */
    public Response chatCompletionStream(ChatRequest request) {
        return chatCompletionStream(request, null);
    }

    public Response chatCompletionStream(ChatRequest request, Map<String, String> headers) {
        Objects.requireNonNull(request, "request");
        request.setStream(true);
        Request.Builder builder = authedBuilder(resolveUrl("/v1/chat/completions"))
                .header("Accept", "text/event-stream");
        if (headers != null) {
            headers.forEach((k, v) -> { if (k != null && v != null) builder.header(k, v); });
        }
        try {
            Request req = builder.post(RequestBody.create(objectMapper.writeValueAsString(request), JSON)).build();
            Response response = httpClient.newCall(req).execute();
            if (!response.isSuccessful()) {
                String body = response.body() != null ? response.body().string() : "";
                response.close();
                throw new OpenClawHttpException("Stream returned status " + response.code(), response.code(), body);
            }
            return response;
        } catch (OpenClawHttpException e) {
            throw e;
        } catch (Exception e) {
            throw new OpenClawHttpException("Stream request failed: " + e.getMessage(), e);
        }
    }

    // ============================================================
    // Models
    // ============================================================

    public ModelsResponse listModels() {
        return parse(getJson("/v1/models"), ModelsResponse.class, "models");
    }

    public ModelsResponse.ModelData getModel(String modelId) {
        Objects.requireNonNull(modelId, "modelId");
        String encodedId = URLEncoder.encode(modelId, StandardCharsets.UTF_8).replace("+", "%20");
        return parse(getJson("/v1/models/" + encodedId), ModelsResponse.ModelData.class, "model");
    }

    // ============================================================
    // Embeddings
    // ============================================================

    public EmbeddingsResponse createEmbeddings(EmbeddingsRequest request) {
        Objects.requireNonNull(request, "request");
        return parse(postJson("/v1/embeddings", request), EmbeddingsResponse.class, "embeddings");
    }

    // ============================================================
    // OpenResponses
    // ============================================================

    public ResponseResult createResponse(ResponseRequest request) {
        Objects.requireNonNull(request, "request");
        return parse(postJson("/v1/responses", request), ResponseResult.class, "response");
    }

    /**
     * 暴露 OkHttpClient 供 SSE/WS 客户端复用。
     */
    public OkHttpClient getOkHttpClient() {
        return httpClient;
    }

    // ============================================================
    // HTTP primitives
    // ============================================================

    private Request.Builder authedBuilder(String url) {
        Request.Builder builder = new Request.Builder().url(url)
                .header("Content-Type", "application/json");
        String token = config.resolveGatewayBearerToken();
        if (OpenClawStrings.isNotBlank(token)) {
            builder.header("Authorization", "Bearer " + token);
        }
        return builder;
    }

    private String postJson(String path, Object body) {
        return postJson(path, body, null);
    }

    private String postJson(String path, Object body, Map<String, String> headers) {
        String url = resolveUrl(path);
        try {
            Request.Builder builder = authedBuilder(url);
            if (headers != null) {
                headers.forEach((k, v) -> { if (k != null && v != null) builder.header(k, v); });
            }
            Request request = builder.post(RequestBody.create(objectMapper.writeValueAsString(body), JSON)).build();
            return execute(request, url);
        } catch (OpenClawHttpException e) {
            throw e;
        } catch (Exception e) {
            throw new OpenClawHttpException("POST " + url + " failed: " + e.getMessage(), e);
        }
    }

    private String getJson(String path) {
        String url = resolveUrl(path);
        try {
            Request request = authedBuilder(url).get().build();
            return execute(request, url);
        } catch (OpenClawHttpException e) {
            throw e;
        } catch (Exception e) {
            throw new OpenClawHttpException("GET " + url + " failed: " + e.getMessage(), e);
        }
    }

    private String execute(Request request, String url) throws IOException {
        try (Response response = httpClient.newCall(request).execute()) {
            String respBody = response.body() != null ? response.body().string() : "";
            if (!response.isSuccessful()) {
                throw new OpenClawHttpException("Request returned status " + response.code(), response.code(), respBody);
            }
            return respBody;
        }
    }

    private <T> T parse(String json, Class<T> type, String label) {
        try {
            return objectMapper.readValue(json, type);
        } catch (Exception e) {
            throw new OpenClawHttpException("Failed to parse " + label + " response: " + e.getMessage(), e);
        }
    }

    private String resolveUrl(String path) {
        String base = config.getGatewayBaseUrl();
        if (OpenClawStrings.isBlank(base)) {
            throw new OpenClawHttpException("gatewayBaseUrl is empty", null);
        }
        return base.replaceAll("/+$", "") + path;
    }

    @Override
    public void close() {
        // 外部传入的 OkHttpClient 不关闭
    }
}
