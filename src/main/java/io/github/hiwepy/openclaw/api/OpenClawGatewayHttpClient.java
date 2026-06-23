package io.github.hiwepy.openclaw.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.hiwepy.openclaw.OpenClawHttpClientConfig;
import io.github.hiwepy.openclaw.api.model.HookRequest;
import io.github.hiwepy.openclaw.api.model.HookResponse;
import io.github.hiwepy.openclaw.exception.OpenClawHttpException;
import io.github.hiwepy.openclaw.util.OpenClawStrings;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * OpenClaw Gateway HTTP Webhooks 客户端（{@code /hooks/*}）。
 * <p>基于 OkHttp，支持外部传入 {@link OkHttpClient}。</p>
 */
@Slf4j
public class OpenClawGatewayHttpClient implements AutoCloseable {

    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    private static final ObjectMapper RESPONSE_MAPPER = new ObjectMapper();

    private final OpenClawHttpClientConfig config;
    private final ObjectMapper objectMapper;
    private final OkHttpClient httpClient;

    public OpenClawGatewayHttpClient(OpenClawHttpClientConfig config, ObjectMapper mapper) {
        this(config, mapper, null);
    }

    public OpenClawGatewayHttpClient(OpenClawHttpClientConfig config) {
        this(config, null, null);
    }

    public OpenClawGatewayHttpClient(OpenClawHttpClientConfig config, ObjectMapper mapper, OkHttpClient httpClient) {
        this.config = Objects.requireNonNull(config, "config");
        this.objectMapper = mapper != null ? mapper : new ObjectMapper();
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

    public HookResponse postHooksAgent(HookRequest request) {
        Objects.requireNonNull(request, "request");
        Map<String, Object> body = buildHooksAgentBody(request);
        HttpResult response = postWebhook(resolveHooksSubPath("agent"), body);
        HookResponse result = new HookResponse();
        result.setHttpStatus(response.getStatus());
        result.setRawBody(response.getBody());
        result.setLocalInvocation(false);
        result.setSuccess(parseOk(response.getBody()));
        result.setRunId(parseRunId(response.getBody()));
        return result;
    }

    public String postHooksWake(String text, String mode) {
        if (OpenClawStrings.isBlank(text)) {
            throw new IllegalArgumentException("webhooks wake: text is required");
        }
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("text", text);
        body.put("mode", OpenClawStrings.isBlank(mode) ? "now" : mode);
        return postWebhook(resolveHooksSubPath("wake"), body).getBody();
    }

    public String postMappedHook(String hookName, Map<String, Object> payload) {
        String normalized = normalizeHookName(hookName);
        Map<String, Object> body = payload != null ? payload : Collections.emptyMap();
        return postWebhook(resolveHooksSubPath(normalized), body).getBody();
    }

    public static Map<String, Object> buildHooksAgentBody(HookRequest request) {
        Objects.requireNonNull(request, "request");
        if (OpenClawStrings.isBlank(request.getMessage())) {
            throw new IllegalArgumentException("hooks/agent: message is required");
        }
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("message", request.getMessage());
        if (OpenClawStrings.isNotBlank(request.getAgentId())) {
            body.put("agentId", request.getAgentId());
        }
        body.put("name", OpenClawStrings.isNotBlank(request.getName()) ? request.getName() : "Generation");
        body.put("wakeMode", OpenClawStrings.isNotBlank(request.getWakeMode()) ? request.getWakeMode() : "now");
        body.put("timeoutSeconds", request.getTimeoutSeconds());
        if (OpenClawStrings.isNotBlank(request.getSessionKey())) body.put("sessionKey", request.getSessionKey());
        if (request.getDeliver() != null) body.put("deliver", request.getDeliver());
        if (OpenClawStrings.isNotBlank(request.getChannel())) body.put("channel", request.getChannel());
        if (OpenClawStrings.isNotBlank(request.getTo())) body.put("to", request.getTo());
        if (OpenClawStrings.isNotBlank(request.getModel())) body.put("model", request.getModel());
        if (OpenClawStrings.isNotBlank(request.getThinking())) body.put("thinking", request.getThinking());
        return body;
    }

    private String resolveHooksSubPath(String subPath) {
        String base = config.resolveHooksPath();
        String child = subPath != null ? subPath.trim() : "";
        if (child.startsWith("/")) child = child.substring(1);
        return child.isEmpty() ? base : base + "/" + child;
    }

    private HttpResult postWebhook(String hookPath, Map<String, Object> body) {
        String base = config.getGatewayBaseUrl();
        if (OpenClawStrings.isBlank(base)) throw new OpenClawHttpException("OpenClaw gatewayBaseUrl is empty", null);
        String url = base.replaceAll("/+$", "") + hookPath;
        String token = config.resolveHooksBearerToken();
        try {
            Request.Builder builder = new Request.Builder().url(url).header("Content-Type", "application/json");
            if (OpenClawStrings.isNotBlank(token)) {
                if (config.isHooksUseXOpenclawTokenHeader()) {
                    builder.header(OpenClawConstants.HEADER_X_OPENCLAW_TOKEN, token);
                } else {
                    builder.header("Authorization", "Bearer " + token);
                }
            }
            Request request = builder.post(RequestBody.create(objectMapper.writeValueAsString(body), JSON)).build();
            try (Response response = httpClient.newCall(request).execute()) {
                int status = response.code();
                String respBody = response.body() != null ? response.body().string() : "";
                if (status < 200 || status >= 300) {
                    throw new OpenClawHttpException("OpenClaw webhook returned status " + status, status, respBody);
                }
                return new HttpResult(status, respBody);
            }
        } catch (OpenClawHttpException e) {
            throw e;
        } catch (Exception e) {
            throw new OpenClawHttpException("OpenClaw webhook invoke failed: " + e.getMessage(), e);
        }
    }

    public static String normalizeHookName(String hookName) {
        if (OpenClawStrings.isBlank(hookName)) throw new IllegalArgumentException("hookName is required");
        String normalized = hookName.trim();
        if (normalized.startsWith("/")) normalized = normalized.substring(1);
        if (normalized.startsWith("hooks/")) normalized = normalized.substring("hooks/".length());
        if (!normalized.matches("[A-Za-z0-9._-]+"))
            throw new IllegalArgumentException("hookName contains illegal characters: " + hookName);
        return normalized;
    }

    public static boolean parseOk(String body) {
        if (body == null || body.isEmpty()) return false;
        try {
            JsonNode root = RESPONSE_MAPPER.readTree(body);
            if (root.has("ok")) return root.get("ok").asBoolean(false);
        } catch (Exception ignored) {}
        return body.contains("\"ok\":true");
    }

    public static String parseRunId(String body) {
        if (body == null || body.isEmpty()) return null;
        try {
            JsonNode root = RESPONSE_MAPPER.readTree(body);
            if (root.hasNonNull("runId")) {
                JsonNode runId = root.get("runId");
                return runId.isNull() ? null : runId.asText();
            }
        } catch (Exception ignored) {}
        return null;
    }

    @Override public void close() {}

    private static final class HttpResult {
        private final int status;
        private final String body;
        private HttpResult(int status, String body) { this.status = status; this.body = body; }
        int getStatus() { return status; }
        String getBody() { return body; }
    }
}
