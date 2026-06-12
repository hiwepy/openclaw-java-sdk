package io.github.hiwepy.openclaw.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.hiwepy.openclaw.OpenClawClientConfig;
import io.github.hiwepy.openclaw.exception.OpenClawHttpException;
import io.github.hiwepy.openclaw.util.OpenClawStrings;
import kong.unirest.core.HttpResponse;
import kong.unirest.core.Unirest;
import kong.unirest.core.UnirestInstance;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * OpenClaw Gateway <b>HTTP Webhooks</b> 客户端（{@code /hooks/*}）。
 * <p>
 * 与官方 TypeScript {@code @openclaw/sdk} 的 WebSocket 控制面不同：本类仅封装无状态的 Hook HTTP 调用；
 * 需要会话流式、{@code agent.wait}、事件归一化等能力时请参阅 OpenClaw App SDK 文档或本仓库后续 WS 实现。
 * </p>
 * <p>覆盖文档中的三个入口：</p>
 * <ul>
 *     <li>{@code POST /hooks/wake}：向主会话队列注入系统事件（text 必填，mode 可选）</li>
 *     <li>{@code POST /hooks/agent}：触发一次隔离 agent turn</li>
 *     <li>{@code POST /hooks/<name>}：调用 hooks.mappings 中的自定义映射 webhook</li>
 * </ul>
* <p>该客户端内部持有独立的 {@link CloseableHttpClient}，使用完成后应调用 {@link #close()} 释放连接池。</p>
 *
 * <h2>其他 HTTP 端点（由独立客户端实现）</h2>
 * <ul>
 *   <li>{@code POST /v1/chat/completions}、{@code GET /v1/models}、{@code POST /v1/embeddings}
 *       → {@link io.github.hiwepy.openclaw.openai.OpenClawOpenAiHttpClient}</li>
 *   <li>{@code POST /v1/responses}
 *       → {@link io.github.hiwepy.openclaw.openai.OpenClawOpenAiHttpClient#createResponse}</li>
 *   <li>{@code POST /tools/invoke}
 *       → {@link io.github.hiwepy.openclaw.tools.OpenClawToolsInvokeClient}</li>
 * </ul>
 *
 * @see <a href="https://docs.openclaw.ai/automation/cron-jobs#webhooks">Webhook 文档（cron-jobs）</a>
 */
@Slf4j
public class OpenClawGatewayHttpClient implements AutoCloseable {

    /**
     * 轻量解析响应 JSON 的共享 mapper，避免在静态解析方法中重复创建对象。
     */
    private static final ObjectMapper RESPONSE_MAPPER = new ObjectMapper();

    private final OpenClawClientConfig config;
    private final ObjectMapper objectMapper;
    private final UnirestInstance http;

    /**
     * @param config  非 null
     * @param mapper  JSON 序列化；null 时使用默认 {@link ObjectMapper}
     */
    public OpenClawGatewayHttpClient(OpenClawClientConfig config, ObjectMapper mapper) {
        this.config = Objects.requireNonNull(config, "config");
        this.objectMapper = mapper != null ? mapper : new ObjectMapper();
        this.http = buildHttpClient(config);
    }

    public OpenClawGatewayHttpClient(OpenClawClientConfig config) {
        this(config, null);
    }

    /**
     * 对应 {@code POST /hooks/agent}：触发一次 agent webhook 调用并返回解析结果。
     */
    public InvokeAgentResult postHooksAgent(InvokeAgentRequest request) {
        Objects.requireNonNull(request, "request");

        Map<String, Object> body = buildHooksAgentBody(request);

        HttpResult response = postWebhook(resolveHooksSubPath("agent"), body);
        String respBody = response.getBody();
        int status = response.getStatus();

        InvokeAgentResult result = new InvokeAgentResult();
        result.setHttpStatus(status);
        result.setRawBody(respBody);
        result.setLocalInvocation(false);
        result.setSuccess(parseOk(respBody));
        result.setRunId(parseRunId(respBody));
        return result;
    }

    /**
     * 对应 {@code POST /hooks/wake}：向主会话队列注入系统事件。
     */
    public String postHooksWake(String text, String mode) {
        if (OpenClawStrings.isBlank(text)) {
            throw new IllegalArgumentException("webhooks wake: text is required");
        }
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("text", text);
        body.put("mode", OpenClawStrings.isBlank(mode) ? "now" : mode);
        return postWebhook(resolveHooksSubPath("wake"), body).getBody();
    }

    /**
     * 调用自定义映射 webhook：{@code POST /hooks/<name>}。
     */
    public String postMappedHook(String hookName, Map<String, Object> payload) {
        String normalized = normalizeHookName(hookName);
        Map<String, Object> body = payload != null ? payload : Collections.emptyMap();
        return postWebhook(resolveHooksSubPath(normalized), body).getBody();
    }

    /**
     * 构建 {@code POST /hooks/agent} 的请求体：{@code message} 必填；其余可选字段仅非空时写入。
     */
    public static Map<String, Object> buildHooksAgentBody(InvokeAgentRequest request) {
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
        if (OpenClawStrings.isNotBlank(request.getSessionKey())) {
            body.put("sessionKey", request.getSessionKey());
        }
        if (request.getDeliver() != null) {
            body.put("deliver", request.getDeliver());
        }
        if (OpenClawStrings.isNotBlank(request.getChannel())) {
            body.put("channel", request.getChannel());
        }
        if (OpenClawStrings.isNotBlank(request.getTo())) {
            body.put("to", request.getTo());
        }
        if (OpenClawStrings.isNotBlank(request.getModel())) {
            body.put("model", request.getModel());
        }
        if (OpenClawStrings.isNotBlank(request.getThinking())) {
            body.put("thinking", request.getThinking());
        }
        return body;
    }

    /**
     * 拼接 {@link OpenClawClientConfig#resolveHooksPath()} 与子路径（如 {@code agent}、{@code wake}、映射名）。
     */
    private String resolveHooksSubPath(String subPath) {
        String base = config.resolveHooksPath();
        String child = subPath != null ? subPath.trim() : "";
        if (child.startsWith("/")) {
            child = child.substring(1);
        }
        if (child.isEmpty()) {
            return base;
        }
        return base + "/" + child;
    }

    /**
     * 底层 webhook POST：统一鉴权头、超时与状态码检查。
     */
    private HttpResult postWebhook(String hookPath, Map<String, Object> body) {
        String base = config.getGatewayBaseUrl();
        if (OpenClawStrings.isBlank(base)) {
            throw new OpenClawHttpException("OpenClaw gatewayBaseUrl is empty", null);
        }
        String url = base.replaceAll("/+$", "") + hookPath;
        String token = config.resolveHooksBearerToken();
        try {
            String json = objectMapper.writeValueAsString(body);
            log.debug("POST webhook url={} bodyLen={}", url, json.length());
            kong.unirest.core.HttpRequestWithBody req = http.post(url)
                    .header("Content-Type", "application/json");
            if (OpenClawStrings.isNotBlank(token)) {
                if (config.isHooksUseXOpenclawTokenHeader()) {
                    req = req.header("x-openclaw-token", token);
                } else {
                    req = req.header("Authorization", "Bearer " + token);
                }
            }
            HttpResponse<String> response = req.body(json).asString();
            int status = response.getStatus();
            String respBody = response.getBody();
            if (status < 200 || status >= 300) {
                log.warn("OpenClaw webhook returned non-2xx status={} url={}", status, url);
                throw new OpenClawHttpException(
                        "OpenClaw webhook returned status " + status, status, respBody);
            }
            log.debug("OpenClaw webhook success status={} url={}", status, url);
            return new HttpResult(status, respBody);
        } catch (OpenClawHttpException e) {
            throw e;
        } catch (Exception e) {
            log.error("OpenClaw webhook invoke failed url={} error={}", url, e.getMessage(), e);
            throw new OpenClawHttpException("OpenClaw webhook invoke failed: " + e.getMessage(), e);
        }
    }

    /**
     * 规范化自定义 hook 名称，防止路径注入或非法字符。
     */
    public static String normalizeHookName(String hookName) {
        if (OpenClawStrings.isBlank(hookName)) {
            throw new IllegalArgumentException("hookName is required");
        }
        String normalized = hookName.trim();
        if (normalized.startsWith("/")) {
            normalized = normalized.substring(1);
        }
        if (normalized.startsWith("hooks/")) {
            normalized = normalized.substring("hooks/".length());
        }
        if (!normalized.matches("[A-Za-z0-9._-]+")) {
            throw new IllegalArgumentException("hookName contains illegal characters: " + hookName);
        }
        return normalized;
    }

    public static boolean parseOk(String body) {
        if (body == null || body.isEmpty()) {
            return false;
        }
        try {
            JsonNode root = RESPONSE_MAPPER.readTree(body);
            if (root.has("ok")) {
                return root.get("ok").asBoolean(false);
            }
        } catch (Exception ignored) {
            // fall through
        }
        return body.contains("\"ok\":true");
    }

    public static String parseRunId(String body) {
        if (body == null || body.isEmpty()) {
            return null;
        }
        try {
            JsonNode root = RESPONSE_MAPPER.readTree(body);
            if (root.hasNonNull("runId")) {
                JsonNode runId = root.get("runId");
                return runId.isNull() ? null : runId.asText();
            }
        } catch (Exception ignored) {
            // ignore
        }
        return null;
    }

    /**
     * 关闭底层 HTTP 客户端，释放连接池；可重复调用。
     */
    @Override
    public void close() {
        try {
            http.close();
        } catch (Exception ignored) {
            // idempotent shutdown
        }
    }

        private static UnirestInstance buildHttpClient(OpenClawClientConfig config) {
        UnirestInstance http = Unirest.spawnInstance();
        http.config()
                .connectTimeout(config.getConnectTimeoutMillis())
                .requestTimeout(config.getReadTimeoutMillis());
        if (!config.isVerifySsl()) {
            http.config().verifySsl(false);
        }
        return http;
    }

    /** 内部 HTTP 响应快照。 */
    private static final class HttpResult {
        private final int status;
        private final String body;

        private HttpResult(int status, String body) {
            this.status = status;
            this.body = body;
        }

        int getStatus() {
            return status;
        }

        String getBody() {
            return body;
        }
    }
}
