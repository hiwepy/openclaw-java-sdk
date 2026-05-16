package io.github.hiwepy.openclaw;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.hiwepy.openclaw.exception.OpenClawHttpException;
import io.github.hiwepy.openclaw.util.OpenClawStrings;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.util.EntityUtils;

import javax.net.ssl.SSLContext;
import java.nio.charset.StandardCharsets;
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
    private final CloseableHttpClient http;

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

        HttpResult response = postWebhook(HOOKS_AGENT_PATH, body);
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
        return postWebhook(HOOKS_WAKE_PATH, body).getBody();
    }

    /**
     * 调用自定义映射 webhook：{@code POST /hooks/<name>}。
     */
    public String postMappedHook(String hookName, Map<String, Object> payload) {
        String normalized = normalizeHookName(hookName);
        Map<String, Object> body = payload != null ? payload : Collections.emptyMap();
        return postWebhook(HOOKS_PREFIX + normalized, body).getBody();
    }

    /**
     * 构建 {@code POST /hooks/agent} 的请求体：{@code message} 必填；其余可选字段仅非空时写入。
     */
    static Map<String, Object> buildHooksAgentBody(InvokeAgentRequest request) {
        Objects.requireNonNull(request, "request");
        if (OpenClawStrings.isBlank(request.getMessage())) {
            throw new IllegalArgumentException("hooks/agent: message is required");
        }
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("message", request.getMessage());
        if (request.getAgentId() != null && !request.getAgentId().isEmpty()) {
            body.put("agentId", request.getAgentId());
        }
        String name = request.getName();
        body.put("name", (name != null && !name.isEmpty()) ? name : "Generation");
        String wakeMode = request.getWakeMode();
        body.put("wakeMode", (wakeMode != null && !wakeMode.isEmpty()) ? wakeMode : "now");
        body.put("timeoutSeconds", request.getTimeoutSeconds());
        if (request.getSessionKey() != null && !request.getSessionKey().isEmpty()) {
            body.put("sessionKey", request.getSessionKey());
        }
        if (request.getDeliver() != null) {
            body.put("deliver", request.getDeliver());
        }
        if (request.getChannel() != null && !request.getChannel().isEmpty()) {
            body.put("channel", request.getChannel());
        }
        if (request.getTo() != null && !request.getTo().isEmpty()) {
            body.put("to", request.getTo());
        }
        if (request.getModel() != null && !request.getModel().isEmpty()) {
            body.put("model", request.getModel());
        }
        if (request.getThinking() != null && !request.getThinking().isEmpty()) {
            body.put("thinking", request.getThinking());
        }
        return body;
    }

    /**
     * 底层 webhook POST：统一鉴权头、超时与状态码检查。
     */
    private HttpResult postWebhook(String hookPath, Map<String, Object> body) {
        String base = config.getGatewayBaseUrl();
        if (base == null || base.isEmpty()) {
            throw new OpenClawHttpException("OpenClaw gatewayBaseUrl is empty", null);
        }
        String url = base.replaceAll("/+$", "") + hookPath;
        String token = config.resolveHooksBearerToken();
        try {
            String json = objectMapper.writeValueAsString(body);
            HttpPost post = new HttpPost(url);
            post.setHeader("Content-Type", "application/json");
            post.setEntity(new StringEntity(json, ContentType.APPLICATION_JSON));
            if (OpenClawStrings.isNotBlank(token)) {
                if (config.isHooksUseXOpenclawTokenHeader()) {
                    post.setHeader("x-openclaw-token", token);
                } else {
                    post.setHeader("Authorization", "Bearer " + token);
                }
            }
            try (CloseableHttpResponse response = http.execute(post)) {
                int status = response.getStatusLine().getStatusCode();
                String respBody = response.getEntity() != null
                        ? EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8)
                        : "";
                if (status < 200 || status >= 300) {
                    throw new OpenClawHttpException(
                            "OpenClaw webhook returned status " + status, status, respBody);
                }
                return new HttpResult(status, respBody);
            }
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

    private static CloseableHttpClient buildHttpClient(OpenClawClientConfig config) {
        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectTimeout(config.getConnectTimeoutMillis())
                .setSocketTimeout(config.getReadTimeoutMillis())
                .build();
        try {
            if (!config.isVerifySsl()) {
                SSLContext sslContext = SSLContextBuilder.create()
                        .loadTrustMaterial(null, (chain, authType) -> true)
                        .build();
                SSLConnectionSocketFactory socketFactory =
                        new SSLConnectionSocketFactory(sslContext, NoopHostnameVerifier.INSTANCE);
                return HttpClients.custom()
                        .setSSLSocketFactory(socketFactory)
                        .setDefaultRequestConfig(requestConfig)
                        .build();
            }
        } catch (Exception e) {
            throw new IllegalStateException("Failed to configure OpenClaw HTTP client SSL", e);
        }
        return HttpClients.custom()
                .setDefaultRequestConfig(requestConfig)
                .build();
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
