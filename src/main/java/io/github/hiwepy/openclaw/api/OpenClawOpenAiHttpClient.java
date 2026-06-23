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
        return chatCompletion0(request, null);
    }

    public ChatResponse chatCompletion(ChatRequest request, OpenClawHeaders.Builder headers) {
        return chatCompletion0(request, headers.build());
    }

    private ChatResponse chatCompletion0(ChatRequest request, Map<String, String> headers) {
        Objects.requireNonNull(request, "request");
        // 处理 agent 和 model 字段
        headers = buildChatHeaders(request, headers);
        // 使用 agent 字段（如果有）作为 HTTP body 的 model
        String bodyModel = resolveRequestModel(request);
        ChatRequest normalized = ChatRequest.builder()
                .model(bodyModel)
                .messages(request.getMessages())
                .stream(request.getStream())
                .streamOptions(request.getStreamOptions())
                .tools(request.getTools())
                .toolChoice(request.getToolChoice())
                .user(request.getUser())
                .maxCompletionTokens(request.getMaxCompletionTokens())
                .maxTokens(request.getMaxTokens())
                .temperature(request.getTemperature())
                .topP(request.getTopP())
                .frequencyPenalty(request.getFrequencyPenalty())
                .presencePenalty(request.getPresencePenalty())
                .seed(request.getSeed())
                .stop(request.getStop())
                .build();
        String responseBody = postJson(OpenClawConstants.ENDPOINT_CHAT_COMPLETIONS, normalized, headers);
        return parse(responseBody, ChatResponse.class, "chat completion");
    }

    /**
     * 构建请求头，处理 agent 和 model 字段。
     */
    private Map<String, String> buildChatHeaders(ChatRequest request, Map<String, String> existingHeaders) {
        Map<String, String> headers = existingHeaders != null ? new java.util.HashMap<>(existingHeaders) : new java.util.HashMap<>();
        // 如果有独立的 model 字段（非 agent 路由），设置 x-openclaw-model header
        if (request.getModel() != null && !isAgentTarget(request.getModel())) {
            headers.put(OpenClawConstants.HEADER_X_OPENCLAW_MODEL, request.getModel());
        }
        return headers.isEmpty() ? null : headers;
    }

    /**
     * 判断 model 值是否为 Agent 目标路由。
     */
    private boolean isAgentTarget(String value) {
        if (value == null) {
            return false;
        }
        return value.startsWith(OpenClawConstants.AGENT_PREFIX_OPENCLAW) ||
               value.startsWith(OpenClawConstants.AGENT_PREFIX_AGENT_COLON) ||
               value.startsWith(OpenClawConstants.AGENT_PREFIX_OPENCLAW_COLON);
    }

    /**
     * 解析请求中的 model 字段：
     * - 如果有 agent 字段，优先使用 agent
     * - 否则使用 model 字段
     */
    private String resolveRequestModel(ChatRequest request) {
        if (request.getAgent() != null) {
            return request.getAgent();
        }
        return request.getModel();
    }

    /**
     * 流式 chat completion（POST /v1/chat/completions with stream=true）。
     * <p>返回 OkHttp Response，调用方用 {@code response.body().source()} 消费 SSE 流。</p>
     */
    public Response chatCompletionStream(ChatRequest request) {
        return this.chatCompletionStream(request, null);
    }

    public Response chatCompletionStream(ChatRequest request, Map<String, String> headers) {
        Objects.requireNonNull(request, "request");
        request.setStream(true);
        // 处理 agent 和 model 字段
        headers = buildChatHeaders(request, headers);
        String bodyModel = resolveRequestModel(request);
        ChatRequest normalized = ChatRequest.builder()
                .model(bodyModel)
                .messages(request.getMessages())
                .stream(true)
                .streamOptions(request.getStreamOptions())
                .tools(request.getTools())
                .toolChoice(request.getToolChoice())
                .user(request.getUser())
                .maxCompletionTokens(request.getMaxCompletionTokens())
                .maxTokens(request.getMaxTokens())
                .temperature(request.getTemperature())
                .topP(request.getTopP())
                .frequencyPenalty(request.getFrequencyPenalty())
                .presencePenalty(request.getPresencePenalty())
                .seed(request.getSeed())
                .stop(request.getStop())
                .build();
        Request.Builder builder = authedBuilder(resolveUrl(OpenClawConstants.ENDPOINT_CHAT_COMPLETIONS))
                .header("Accept", "text/event-stream");
        if (headers != null) {
            headers.forEach((k, v) -> {
                if (k != null && v != null) {
                    builder.header(k, v);
                }
            });
        }
        try {
            Request req = builder.post(RequestBody.create(objectMapper.writeValueAsString(normalized), JSON)).build();
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
        return parse(getJson(OpenClawConstants.ENDPOINT_MODELS), ModelsResponse.class, "models");
    }

    public ModelsResponse.ModelData getModel(String modelId) {
        Objects.requireNonNull(modelId, "modelId");
        String encodedId = URLEncoder.encode(modelId, StandardCharsets.UTF_8).replace("+", "%20");
        return parse(getJson(OpenClawConstants.ENDPOINT_MODELS + "/" + encodedId), ModelsResponse.ModelData.class, "model");
    }

    // ============================================================
    // Embeddings
    // ============================================================

    public EmbeddingsResponse createEmbeddings(EmbeddingsRequest request) {
        Objects.requireNonNull(request, "request");
        // 处理 agent 和 model 字段
        Map<String, String> headers = buildEmbeddingsHeaders(request);
        String bodyModel = resolveEmbeddingsModel(request);
        EmbeddingsRequest normalized = EmbeddingsRequest.builder()
                .model(bodyModel)
                .input(request.getInput())
                .build();
        return parse(postJson(OpenClawConstants.ENDPOINT_EMBEDDINGS, normalized, headers), EmbeddingsResponse.class, "embeddings");
    }

    private Map<String, String> buildEmbeddingsHeaders(EmbeddingsRequest request) {
        Map<String, String> headers = new java.util.HashMap<>();
        if (request.getModel() != null && !isAgentTarget(request.getModel())) {
            headers.put(OpenClawConstants.HEADER_X_OPENCLAW_MODEL, request.getModel());
        }
        return headers.isEmpty() ? null : headers;
    }

    private String resolveEmbeddingsModel(EmbeddingsRequest request) {
        if (request.getAgent() != null) {
            return request.getAgent();
        }
        return request.getModel();
    }

    // ============================================================
    // OpenResponses
    // ============================================================

    public ResponseResult createResponse(ResponseRequest request) {
        Objects.requireNonNull(request, "request");
        // 处理 agent 和 model 字段
        Map<String, String> headers = buildResponseHeaders(request);
        String bodyModel = resolveResponseModel(request);
        ResponseRequest normalized = ResponseRequest.builder()
                .model(bodyModel)
                .input(request.getInput())
                .instructions(request.getInstructions())
                .tools(request.getTools())
                .toolChoice(request.getToolChoice())
                .stream(request.getStream())
                .maxOutputTokens(request.getMaxOutputTokens())
                .temperature(request.getTemperature())
                .topP(request.getTopP())
                .user(request.getUser())
                .previousResponseId(request.getPreviousResponseId())
                .build();
        return parse(postJson(OpenClawConstants.ENDPOINT_RESPONSES, normalized, headers), ResponseResult.class, "response");
    }

    private Map<String, String> buildResponseHeaders(ResponseRequest request) {
        Map<String, String> headers = new java.util.HashMap<>();
        if (request.getModel() != null && !isAgentTarget(request.getModel())) {
            headers.put(OpenClawConstants.HEADER_X_OPENCLAW_MODEL, request.getModel());
        }
        return headers.isEmpty() ? null : headers;
    }

    private String resolveResponseModel(ResponseRequest request) {
        if (request.getAgent() != null) {
            return request.getAgent();
        }
        return request.getModel();
    }

    /**
     * 流式 OpenResponses 请求。
     * <p>
     * 返回 OkHttp Response，调用方用 {@code response.body().source()} 消费 SSE 流。
     * 流式事件类型：{@code response.created}、{@code response.in_progress}、{@code response.output_item.added} 等。
     * 流以 {@code data: [DONE]} 结束。
     * </p>
     *
     * @param request 请求体
     * @return OkHttp 响应
     */
    public Response createResponseStream(ResponseRequest request) {
        return createResponseStream(request, (Map<String, String>) null);
    }

    public Response createResponseStream(ResponseRequest request, Map<String, String> headers) {
        Objects.requireNonNull(request, "request");
        request.setStream(true);
        headers = buildResponseHeaders(request);
        String bodyModel = resolveResponseModel(request);
        ResponseRequest normalized = ResponseRequest.builder()
                .model(bodyModel)
                .input(request.getInput())
                .instructions(request.getInstructions())
                .tools(request.getTools())
                .toolChoice(request.getToolChoice())
                .stream(true)
                .maxOutputTokens(request.getMaxOutputTokens())
                .temperature(request.getTemperature())
                .topP(request.getTopP())
                .user(request.getUser())
                .previousResponseId(request.getPreviousResponseId())
                .build();
        Request.Builder builder = authedBuilder(resolveUrl(OpenClawConstants.ENDPOINT_RESPONSES))
                .header("Accept", "text/event-stream");
        if (headers != null) {
            headers.forEach((k, v) -> {
                if (k != null && v != null) builder.header(k, v);
            });
        }
        try {
            Request req = builder.post(RequestBody.create(objectMapper.writeValueAsString(normalized), JSON)).build();
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

    /**
     * 流式 OpenResponses 请求（使用 OpenClawHeaders.Builder）。
     */
    public Response createResponseStream(ResponseRequest request, OpenClawHeaders.Builder headers) {
        return createResponseStream(request, headers != null ? headers.build() : null);
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
