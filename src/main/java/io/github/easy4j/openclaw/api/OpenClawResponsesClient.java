package io.github.easy4j.openclaw.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.easy4j.openclaw.OpenClawHttpClientConfig;
import io.github.easy4j.openclaw.util.OpenClawStrings;
import io.github.easy4j.openclaw.api.model.ResponseRequest;
import io.github.easy4j.openclaw.api.model.ResponseResult;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;

import java.util.HashMap;
import java.util.Map;

/**
 * OpenResponses API 客户端。
 *
 * @see <a href="https://docs.openclaw.ai/gateway/openresponses-http-api">OpenResponses API</a>
 */
@Slf4j
public class OpenClawResponsesClient extends OpenClawHttpClient {

    public OpenClawResponsesClient(OpenClawHttpClientConfig config) {
        super(config);
    }

    public OpenClawResponsesClient(OpenClawHttpClientConfig config, ObjectMapper objectMapper, OkHttpClient httpClient) {
        super(config, objectMapper, httpClient);
    }

    /**
     * 发送 OpenResponses 请求。
     */
    public ResponseResult createResponse(ResponseRequest request) {
        debug("=== Response Request ===");
        debug("agent: {}", request.getAgent());
        debug("model: {}", request.getModel());
        debug("input type: {}", request.getInput() != null ? request.getInput().getClass().getSimpleName() : "null");
        debug("stream: {}", request.getStream());

        // 验证请求
        validateRequest(request);

        Map<String, String> headers = buildHeaders(request);
        String bodyModel = resolveModel(request);

        debug("Resolved body model: {}", bodyModel);

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

        String json = postJson(OpenClawConstants.ENDPOINT_RESPONSES, normalized, headers);
        debug("Response API response received");

        return parse(json, ResponseResult.class, "response");
    }

    private void validateRequest(ResponseRequest request) {
        if (OpenClawStrings.isBlank(request.getAgent()) && OpenClawStrings.isBlank(request.getModel())) {
            String msg = "Response request requires either 'agent' or 'model' field. " +
                    "Use 'agent' for OpenClaw routing (e.g., 'openclaw/default'), " +
                    "or 'model' for direct backend model.";
            warn(msg);
            throw new IllegalArgumentException(msg);
        }

        if (request.getInput() == null) {
            warn("Response request has no input");
            throw new IllegalArgumentException("Response request requires input");
        }
    }

    private Map<String, String> buildHeaders(ResponseRequest request) {
        Map<String, String> headers = new HashMap<>();

        if (request.getModel() != null && !OpenClawStrings.isAgentTarget(request.getModel())) {
            headers.put(OpenClawConstants.HEADER_X_OPENCLAW_MODEL, request.getModel());
            debug("Added {} header: {}", OpenClawConstants.HEADER_X_OPENCLAW_MODEL, request.getModel());
        }

        return headers.isEmpty() ? null : headers;
    }

    private String resolveModel(ResponseRequest request) {
        if (request.getAgent() != null) {
            debug("Using agent as model: {}", request.getAgent());
            return request.getAgent();
        }
        debug("Using model: {}", request.getModel());
        return request.getModel();
    }
}
