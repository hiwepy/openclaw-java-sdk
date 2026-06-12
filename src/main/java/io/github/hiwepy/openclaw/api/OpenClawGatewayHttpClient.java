package io.github.hiwepy.openclaw.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.hiwepy.openclaw.exception.OpenClawHttpException;
import io.github.hiwepy.openclaw.api.http.OpenClawHttpClient;
import io.github.hiwepy.openclaw.util.OpenClawStrings;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * OpenClaw Gateway HTTP Webhooks 客户端 ({@code /hooks/*})。
 * <p>封装无状态的 Hook HTTP 调用：agent、wake、mapped hooks。</p>
 */
@Slf4j
public class OpenClawGatewayHttpClient implements AutoCloseable {

    private static final ObjectMapper RESPONSE_MAPPER = new ObjectMapper();

    private final OpenClawClientConfig config;
    private final ObjectMapper objectMapper;
    private final OpenClawHttpClient httpClient;

    public OpenClawGatewayHttpClient(OpenClawClientConfig config, ObjectMapper mapper,
                                     OpenClawHttpClient httpClient) {
        this.config = Objects.requireNonNull(config, "config");
        this.objectMapper = mapper != null ? mapper : new ObjectMapper();
        this.httpClient = Objects.requireNonNull(httpClient, "httpClient");
    }

    public OpenClawGatewayHttpClient(OpenClawClientConfig config, OpenClawHttpClient httpClient) {
        this(config, null, httpClient);
    }

    // ============================================================
    // POST /hooks/agent
    // ============================================================

    public InvokeAgentResult postHooksAgent(InvokeAgentRequest request) {
        Objects.requireNonNull(request, "request");
        Map<String, Object> body = buildHooksAgentBody(request);
        String respBody = httpClient.postJsonRaw(resolveHooksSubPath("agent"), body, null);
        return InvokeAgentResult.http(200, respBody, parseOk(respBody), parseRunId(respBody));
    }

    // ============================================================
    // POST /hooks/wake
    // ============================================================

    public String postHooksWake(String text, String mode) {
        if (OpenClawStrings.isBlank(text)) {
            throw new IllegalArgumentException("webhooks wake: text is required");
        }
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("text", text);
        body.put("mode", OpenClawStrings.isBlank(mode) ? "now" : mode);
        return httpClient.postJsonRaw(resolveHooksSubPath("wake"), body, null);
    }

    // ============================================================
    // POST /hooks/<name>
    // ============================================================

    public String postMappedHook(String hookName, Map<String, Object> payload) {
        String normalized = normalizeHookName(hookName);
        Map<String, Object> body = payload != null ? payload : Collections.emptyMap();
        return httpClient.postJsonRaw(resolveHooksSubPath(normalized), body, null);
    }

    // ============================================================
    // Body building
    // ============================================================

    public static Map<String, Object> buildHooksAgentBody(InvokeAgentRequest request) {
        Objects.requireNonNull(request, "request");
        if (OpenClawStrings.isBlank(request.message())) {
            throw new IllegalArgumentException("hooks/agent: message is required");
        }
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("message", request.message());
        OpenClawStrings.putIfNotBlank(body, "agentId", request.agentId());
        body.put("name", OpenClawStrings.isNotBlank(request.name()) ? request.name() : "Generation");
        body.put("wakeMode", OpenClawStrings.isNotBlank(request.wakeMode()) ? request.wakeMode() : "now");
        body.put("timeoutSeconds", request.timeoutSeconds() != null ? request.timeoutSeconds() : 300);
        OpenClawStrings.putIfNotBlank(body, "sessionKey", request.sessionKey());
        if (request.deliver() != null) body.put("deliver", request.deliver());
        OpenClawStrings.putIfNotBlank(body, "channel", request.channel());
        OpenClawStrings.putIfNotBlank(body, "to", request.to());
        OpenClawStrings.putIfNotBlank(body, "model", request.model());
        OpenClawStrings.putIfNotBlank(body, "thinking", request.thinking());
        return body;
    }

    // ============================================================
    // Helpers
    // ============================================================

    private String resolveHooksSubPath(String subPath) {
        String base = config.resolveHooksPath();
        String child = subPath != null ? subPath.trim() : "";
        if (child.startsWith("/")) child = child.substring(1);
        if (child.isEmpty()) return base;
        return base + "/" + child;
    }

    public static String normalizeHookName(String hookName) {
        if (OpenClawStrings.isBlank(hookName))
            throw new IllegalArgumentException("hookName is required");
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

    @Override
    public void close() {
        httpClient.close();
    }
}
