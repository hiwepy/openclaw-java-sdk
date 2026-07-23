package io.github.easy4j.openclaw.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.easy4j.openclaw.OpenClawHttpClientConfig;
import io.github.easy4j.openclaw.util.OpenClawStrings;
import io.github.easy4j.openclaw.api.model.EmbeddingsRequest;
import io.github.easy4j.openclaw.api.model.EmbeddingsResponse;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;

import java.util.HashMap;
import java.util.Map;

/**
 * Embeddings API 客户端。
 *
 * @see <a href="https://docs.openclaw.ai/gateway/openai-http-api">OpenAI Embeddings</a>
 */
@Slf4j
public class OpenClawEmbeddingsClient extends OpenClawHttpClient {

    public OpenClawEmbeddingsClient(OpenClawHttpClientConfig config) {
        super(config);
    }

    public OpenClawEmbeddingsClient(OpenClawHttpClientConfig config, ObjectMapper objectMapper, OkHttpClient httpClient) {
        super(config, objectMapper, httpClient);
    }

    public EmbeddingsResponse createEmbeddings(EmbeddingsRequest request) {
        debug("=== Embeddings Request ===");
        debug("agent: {}", request.getAgent());
        debug("model: {}", request.getModel());
        debug("input: {}", request.getInput());

        // 验证请求
        validateRequest(request);

        Map<String, String> headers = buildHeaders(request);
        String bodyModel = resolveModel(request);

        debug("Resolved body model: {}", bodyModel);

        EmbeddingsRequest normalized = EmbeddingsRequest.builder()
                .model(bodyModel)
                .input(request.getInput())
                .build();

        String json = postJson(OpenClawConstants.ENDPOINT_EMBEDDINGS, normalized, headers);
        debug("Embeddings response received");

        return parse(json, EmbeddingsResponse.class, "embeddings");
    }

    private void validateRequest(EmbeddingsRequest request) {
        if (OpenClawStrings.isBlank(request.getAgent()) && OpenClawStrings.isBlank(request.getModel())) {
            String msg = "Embeddings request requires either 'agent' or 'model' field. " +
                    "Use 'agent' for OpenClaw routing (e.g., 'openclaw/default'), " +
                    "or 'model' for direct backend model (e.g., 'text-embedding-3-small').";
            warn(msg);
            throw new IllegalArgumentException(msg);
        }

        if (request.getInput() == null) {
            warn("Embeddings request has no input");
            throw new IllegalArgumentException("Embeddings request requires input");
        }
    }

    private Map<String, String> buildHeaders(EmbeddingsRequest request) {
        Map<String, String> headers = new HashMap<>();

        if (request.getModel() != null && !OpenClawStrings.isAgentTarget(request.getModel())) {
            headers.put(OpenClawConstants.HEADER_X_OPENCLAW_MODEL, request.getModel());
            debug("Added {} header: {}", OpenClawConstants.HEADER_X_OPENCLAW_MODEL, request.getModel());
        }

        return headers.isEmpty() ? null : headers;
    }

    private String resolveModel(EmbeddingsRequest request) {
        if (request.getAgent() != null) {
            debug("Using agent as model: {}", request.getAgent());
            return request.getAgent();
        }
        debug("Using model: {}", request.getModel());
        return request.getModel();
    }
}
