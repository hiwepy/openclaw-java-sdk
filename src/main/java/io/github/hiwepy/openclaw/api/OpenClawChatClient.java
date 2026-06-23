package io.github.hiwepy.openclaw.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.hiwepy.openclaw.OpenClawHttpClientConfig;
import io.github.hiwepy.openclaw.exception.OpenClawHttpException;
import io.github.hiwepy.openclaw.util.OpenClawStrings;
import io.github.hiwepy.openclaw.api.model.*;
import io.github.hiwepy.openclaw.api.sse.SseStreamReader;
import io.github.hiwepy.openclaw.api.sse.StreamingChatResponse;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;

import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

/**
 * Chat Completions API 客户端。
 *
 * @see <a href="https://docs.openclaw.ai/gateway/openai-http-api">OpenAI Chat Completions</a>
 */
@Slf4j
public class OpenClawChatClient extends OpenClawHttpClient {

    public OpenClawChatClient(OpenClawHttpClientConfig config) {
        super(config);
    }

    public OpenClawChatClient(OpenClawHttpClientConfig config, ObjectMapper objectMapper, OkHttpClient httpClient) {
        super(config, objectMapper, httpClient);
    }

    // ============================================================
    // Chat Completions
    // ============================================================

    public ChatResponse chatCompletion(ChatRequest request) {
        return chatCompletion(request, (Map<String, String>) null);
    }

    public ChatResponse chatCompletion(ChatRequest request, Map<String, String> headers) {
        Objects.requireNonNull(request, "request");

        debug("=== Chat Completion Request ===");
        debug("agent: {}", request.getAgent());
        debug("model: {}", request.getModel());
        debug("messages count: {}", request.getMessages() != null ? request.getMessages().size() : 0);
        debug("stream: {}", request.getStream());
        debug("tools: {}", request.getTools() != null ? request.getTools().size() : 0);

        // 验证请求
        validateRequest(request);

        headers = buildHeaders(request, headers);
        String bodyModel = resolveModel(request);

        debug("Resolved body model (agent routing): {}", bodyModel);
        debug("Headers to send: {}", headers);

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
                .responseFormat(request.getResponseFormat())
                .build();

        String json;
        try {
            json = postJson(OpenClawConstants.ENDPOINT_CHAT_COMPLETIONS, normalized, headers);
        } catch (OpenClawHttpException e) {
            error("Chat completion failed: status={}, message={}", e.getStatusCode(), e.getMessage());
            throw e;
        }

        debug("Response received, parsing...");
        ChatResponse response = parse(json, ChatResponse.class, "chat completion");
        debug("Chat completion success: id={}", response.getId());

        return response;
    }

    /**
     * 流式 chat completion。
     */
    public StreamingChatResponse chatCompletionStream(ChatRequest request) {
        return chatCompletionStream(request, (Map<String, String>) null);
    }

    public StreamingChatResponse chatCompletionStream(ChatRequest request, Map<String, String> headers) {
        StreamingChatResponse response = new StreamingChatResponse();
        Response httpResponse = chatCompletionStreamRaw(request, headers);
        startStreamConsumer(httpResponse, response);
        return response;
    }

    public StreamingChatResponse chatCompletionStream(ChatRequest request, StreamingChatResponse.Builder callbackBuilder) {
        StreamingChatResponse response = callbackBuilder.build();
        Response httpResponse = chatCompletionStreamRaw(request, null);
        startStreamConsumer(httpResponse, response);
        return response;
    }

    /**
     * 获取流式响应的原始 OkHttp Response（高级用法）。
     */
    public Response chatCompletionStreamRaw(ChatRequest request) {
        return chatCompletionStreamRaw(request, null);
    }

    public Response chatCompletionStreamRaw(ChatRequest request, Map<String, String> headers) {
        Objects.requireNonNull(request, "request");

        debug("=== Chat Completion Stream Request ===");
        request.setStream(true);

        headers = buildHeaders(request, headers);
        String bodyModel = resolveModel(request);

        debug("Resolved body model: {}", bodyModel);

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

        Request.Builder builder = authedBuilder(resolveUrl(OpenClawConstants.ENDPOINT_CHAT_COMPLETIONS), headers)
                .header("Accept", "text/event-stream");

        try {
            Request req = builder.post(RequestBody.create(objectMapper.writeValueAsString(normalized), JSON)).build();
            debug("Sending streaming request...");

            Response response = httpClient.newCall(req).execute();
            int status = response.code();

            debug("Stream response status: {}", status);

            if (!response.isSuccessful()) {
                String body = response.body() != null ? response.body().string() : "";
                debug("Stream error response: {}", body);
                response.close();
                throw new OpenClawHttpException("Stream returned status " + status, status, body);
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
        debug("=== List Models ===");
        String json = getJson(OpenClawConstants.ENDPOINT_MODELS);
        ModelsResponse response = parse(json, ModelsResponse.class, "models");
        debug("Models count: {}", response.getData() != null ? response.getData().size() : 0);
        return response;
    }

    public ModelsResponse.ModelData getModel(String modelId) {
        Objects.requireNonNull(modelId, "modelId");
        debug("=== Get Model: {} ===", modelId);

        String encodedId;
        try {
            encodedId = URLEncoder.encode(modelId, "UTF-8").replace("+", "%20");
        } catch (java.io.UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }

        String json = getJson(OpenClawConstants.ENDPOINT_MODELS + "/" + encodedId);
        return parse(json, ModelsResponse.ModelData.class, "model");
    }

    // ============================================================
    // Private helpers
    // ============================================================

    private void validateRequest(ChatRequest request) {
        // 检查 agent 或 model 必须有一个
        if (OpenClawStrings.isBlank(request.getAgent()) && OpenClawStrings.isBlank(request.getModel())) {
            String msg = "Chat request requires either 'agent' or 'model' field. " +
                    "Use 'agent' for OpenClaw routing (e.g., 'openclaw/default'), " +
                    "or 'model' for direct backend model (e.g., 'gpt-4o').";
            warn(msg);
            throw new IllegalArgumentException(msg);
        }

        // 检查 messages
        if (request.getMessages() == null || request.getMessages().isEmpty()) {
            warn("Chat request has no messages");
            throw new IllegalArgumentException("Chat request requires at least one message");
        }
    }

    private Map<String, String> buildHeaders(ChatRequest request, Map<String, String> existingHeaders) {
        Map<String, String> headers = existingHeaders != null ? new HashMap<>(existingHeaders) : new HashMap<>();

        // 如果有独立的 model 字段（非 agent 路由），设置 x-openclaw-model header
        if (request.getModel() != null && !OpenClawStrings.isAgentTarget(request.getModel())) {
            headers.put(OpenClawConstants.HEADER_X_OPENCLAW_MODEL, request.getModel());
            debug("Added {} header: {}", OpenClawConstants.HEADER_X_OPENCLAW_MODEL, request.getModel());
        }

        return headers.isEmpty() ? null : headers;
    }

    private String resolveModel(ChatRequest request) {
        // 优先使用 agent 字段（用于 OpenClaw 路由）
        if (request.getAgent() != null) {
            debug("Using agent as model: {}", request.getAgent());
            return request.getAgent();
        }
        // 否则使用 model 字段（用于直接调用后端模型）
        debug("Using model: {}", request.getModel());
        return request.getModel();
    }

    private void startStreamConsumer(Response httpResponse, StreamingChatResponse response) {
        SseStreamReader reader = new SseStreamReader(objectMapper);
        CompletableFuture.runAsync(() -> {
            try {
                reader.readChatCompletionStream(httpResponse.body().byteStream(), response);
            } finally {
                httpResponse.close();
            }
        });
    }
}
