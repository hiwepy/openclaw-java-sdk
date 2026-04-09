package com.github.hiwepy.openclaw;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.hiwepy.openclaw.exception.OpenClawHttpException;
import kong.unirest.core.HttpResponse;
import kong.unirest.core.UnirestInstance;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * OpenClaw Gateway Webhooks 客户端。
 * <p>覆盖文档中的三个入口：</p>
 * <ul>
 *     <li>{@code POST /hooks/wake}：向主会话队列注入系统事件（text 必填，mode 可选）</li>
 *     <li>{@code POST /hooks/agent}：触发一次隔离 agent turn</li>
 *     <li>{@code POST /hooks/<name>}：调用 hooks.mappings 中的自定义映射 webhook</li>
 * </ul>
 * <p>安全建议（来自官方文档）：</p>
 * <ul>
 *     <li>Webhook 端点应仅暴露在 loopback/tailnet/受信代理之后</li>
 *     <li>使用独立 hook token，不复用 gateway auth token</li>
 *     <li>保持 {@code hooks.path} 为专用子路径（拒绝根路径）</li>
 *     <li>建议配置 {@code hooks.allowedAgentIds} 限制显式 {@code agentId}</li>
 *     <li>默认保持 {@code hooks.allowRequestSessionKey=false}；若开启需配 {@code hooks.allowedSessionKeyPrefixes}</li>
 * </ul>
 * <p>该客户端内部持有独立的 {@link UnirestInstance}，使用完成后应调用 {@link #close()} 释放连接池与线程资源。</p>
 *
 * @see <a href="https://docs.openclaw.ai/automation/cron-jobs#webhooks">Webhook 文档（cron-jobs）</a>
 */
public class OpenClawGatewayHttpClient implements AutoCloseable {

    private static final String HOOKS_AGENT_PATH = "/hooks/agent";
    private static final String HOOKS_WAKE_PATH = "/hooks/wake";
    private static final String HOOKS_PREFIX = "/hooks/";
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
        this.http = kong.unirest.core.Unirest.spawnInstance();
        this.http.config()
                .connectTimeout(config.getConnectTimeoutMillis())
                .requestTimeout(config.getReadTimeoutMillis());
        if (!config.isVerifySsl()) {
            this.http.config().verifySsl(false);
        }
    }

    public OpenClawGatewayHttpClient(OpenClawClientConfig config) {
        this(config, null);
    }

    /**
     * 对应 {@code POST /hooks/agent}：触发一次 agent webhook 调用并返回解析结果。
     * <p>请求体字段与文档一致：{@code message}（required）、{@code name}、{@code agentId}、
     * {@code wakeMode}、{@code deliver}、{@code channel}、{@code to}、
     * {@code model}、{@code thinking}、{@code timeoutSeconds}。
     * 当前 {@link InvokeAgentRequest} 已建模字段为 {@code message}、{@code name}、
     * {@code agentId}、{@code wakeMode}、{@code timeoutSeconds}。</p>
     *
     * @param request 请求体字段
     */
    public InvokeAgentResult postHooksAgent(InvokeAgentRequest request) {
        Objects.requireNonNull(request, "request");

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("message", request.getMessage());
        body.put("agentId", request.getAgentId());
        body.put("name", request.getName() != null ? request.getName() : "Generation");
        body.put("wakeMode", request.getWakeMode() != null ? request.getWakeMode() : "now");
        body.put("timeoutSeconds", request.getTimeoutSeconds());

        HttpResponse<String> response = postWebhook(HOOKS_AGENT_PATH, body);
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
     * <p>文档约束：{@code text} 必填；{@code mode} 可选，支持 {@code now}（默认）与
     * {@code next-heartbeat}。</p>
     *
     * @param text 事件文本（文档 required）
     * @param mode 触发模式：{@code now} 或 {@code next-heartbeat}；null/空则默认 {@code now}
     * @return 网关原始响应体
     */
    public String postHooksWake(String text, String mode) {
        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException("webhooks wake: text is required");
        }
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("text", text);
        body.put("mode", (mode == null || mode.isBlank()) ? "now" : mode);
        return postWebhook(HOOKS_WAKE_PATH, body).getBody();
    }

    /**
     * 调用自定义映射 webhook：{@code POST /hooks/<name>}。
     * <p>{@code <name>} 会在 Gateway 的 {@code hooks.mappings} 中解析，并可把任意负载
     * 通过模板或代码转换为 wake/agent 动作。</p>
     *
     * @param hookName 映射名（不含 {@code /hooks/} 前缀）
     * @param payload  请求体，null 时发送空 JSON 对象
     * @return 网关原始响应体
     */
    public String postMappedHook(String hookName, Map<String, Object> payload) {
        String normalized = normalizeHookName(hookName);
        Map<String, Object> body = payload != null ? payload : Collections.emptyMap();
        return postWebhook(HOOKS_PREFIX + normalized, body).getBody();
    }

    /**
     * 底层 webhook POST 调用：统一做鉴权头、异常语义与状态码检查。
     * <p>鉴权头优先使用文档推荐的 {@code Authorization: Bearer <token>}。</p>
     */
    private HttpResponse<String> postWebhook(String hookPath, Map<String, Object> body) {
        String base = config.getGatewayBaseUrl();
        if (base == null || base.isEmpty()) {
            throw new OpenClawHttpException("OpenClaw gatewayBaseUrl is empty", null);
        }
        String url = base.replaceAll("/+$", "") + hookPath;
        String token = config.resolveBearerToken();
        try {
            String json = objectMapper.writeValueAsString(body);
            var req = http.post(url)
                    .header("Content-Type", "application/json")
                    .body(json);
            if (token != null && !token.isEmpty()) {
                // Webhook 文档推荐 Authorization: Bearer <token>（也支持 x-openclaw-token）。
                req = req.header("Authorization", "Bearer " + token);
            }
            HttpResponse<String> response = req.asString();
            int status = response.getStatus();
            if (status < 200 || status >= 300) {
                throw new OpenClawHttpException(
                        "OpenClaw webhook returned status " + status, status, response.getBody());
            }
            return response;
        } catch (OpenClawHttpException e) {
            throw e;
        } catch (Exception e) {
            throw new OpenClawHttpException("OpenClaw webhook invoke failed: " + e.getMessage(), e);
        }
    }

    /**
     * 规范化自定义 hook 名称，防止路径注入或非法字符。
     */
    static String normalizeHookName(String hookName) {
        if (hookName == null || hookName.isBlank()) {
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

    /**
     * 从 JSON 响应中解析 {@code ok} 字段；非 JSON 时根据关键字推断。
     */
    static boolean parseOk(String body) {
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

    static String parseRunId(String body) {
        if (body == null || body.isEmpty()) {
            return null;
        }
        try {
            JsonNode root = RESPONSE_MAPPER.readTree(body);
            if (root.hasNonNull("runId")) {
                return root.get("runId").asText(null);
            }
        } catch (Exception ignored) {
            // ignore
        }
        return null;
    }

    /**
     * 关闭底层 {@link UnirestInstance}，释放连接池与相关资源。
     * <p>可重复调用；当实例已关闭或底层拒绝重复关闭时会被安全忽略。</p>
     */
    @Override
    public void close() {
        try {
            http.close();
        } catch (Exception ignored) {
            // Ignore close exceptions to keep shutdown idempotent.
        }
    }
}
