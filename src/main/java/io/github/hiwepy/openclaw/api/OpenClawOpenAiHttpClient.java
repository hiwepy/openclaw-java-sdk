package io.github.hiwepy.openclaw.api;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.hiwepy.openclaw.OpenClawClientConfig;
import io.github.hiwepy.openclaw.util.OpenClawStrings;
import io.github.hiwepy.openclaw.exception.OpenClawHttpException;
import io.github.hiwepy.openclaw.api.model.*;
import kong.unirest.core.HttpResponse;
import kong.unirest.core.Unirest;
import kong.unirest.core.UnirestInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

/**
 * OpenAI 兼容 HTTP API 客户端。
 * <p>
 * 封装 OpenClaw Gateway 的 OpenAI 兼容 HTTP 端点，包括：
 * <ul>
 *   <li>{@code POST /v1/chat/completions} - Chat Completions（同步 + SSE 流式）</li>
 *   <li>{@code GET /v1/models} - 模型列表</li>
 *   <li>{@code GET /v1/models/{id}} - 单个模型信息</li>
 *   <li>{@code POST /v1/embeddings} - 嵌入向量</li>
 *   <li>{@code POST /v1/responses} - OpenResponses API</li>
 * </ul>
 *
 * <h3>Agent 目标约定</h3>
 * <p>OpenClaw 将 OpenAI {@code model} 字段解释为 agent 目标：</p>
 * <ul>
 *   <li>{@code "openclaw"} 或 {@code "openclaw/default"} - 默认 agent</li>
 *   <li>{@code "openclaw/<agentId>"} - 指定 agent</li>
 * </ul>
 * <p>使用 HTTP 头 {@code x-openclaw-model} 覆盖后端模型（如 {@code openai/gpt-5.4}）。</p>
 *
 * <h3>鉴权</h3>
 * <p>使用 Gateway 鉴权配置。共享密钥模式下使用 {@code Authorization: Bearer <token-or-password>}。</p>
 *
 * @see <a href="https://docs.openclaw.ai/gateway/openai-http-api">OpenAI Chat Completions</a>
 * @see <a href="https://docs.openclaw.ai/gateway/openresponses-http-api">OpenResponses API</a>
 */
public class OpenClawOpenAiHttpClient  {

    private static final Logger log = LoggerFactory.getLogger(OpenClawOpenAiHttpClient.class);

    private final OpenClawClientConfig config;
    private final ObjectMapper objectMapper;
    private final UnirestInstance http;

    /**
     * 构造函数。
     *
     * @param config  OpenClaw 客户端配置（不得为 null）
     * @param mapper  JSON 序列化器（null 时使用默认配置）
     */
    public OpenClawOpenAiHttpClient(OpenClawClientConfig config, ObjectMapper mapper) {
        this.config = Objects.requireNonNull(config, "config");
        this.objectMapper = mapper != null ? mapper : new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        this.http = buildHttpClient(config);
    }

    public OpenClawOpenAiHttpClient(OpenClawClientConfig config) {
        this(config, null);
    }

    // ============================================================
    // Chat Completions
    // ============================================================

    /**
     * 发送 Chat Completions 请求（非流式）。
     * <p>
     * 对应 {@code POST /v1/chat/completions}，{@code stream} 为 {@code false} 或未设置。
     * </p>
     *
     * @param request 请求体
     * @return 解析后的响应
     */
    public ChatCompletionResponse chatCompletion(ChatCompletionRequest request) {
        Objects.requireNonNull(request, "request");
        String responseBody = postJson("/v1/chat/completions", request);
        try {
            return objectMapper.readValue(responseBody, ChatCompletionResponse.class);
        } catch (Exception e) {
            throw new OpenClawHttpException("Failed to parse chat completion response: " + e.getMessage(), e);
        }
    }

    // ============================================================
    // Models
    // ============================================================

    /**
     * 获取可用模型/agent 目标列表。
     * <p>
     * 对应 {@code GET /v1/models}。
     * 返回 OpenClaw agent 目标（如 {@code openclaw}、{@code openclaw/default}），
     * 而非原始 provider 模型目录。
     * </p>
     *
     * @return 模型列表
     */
    public ModelsResponse listModels() {
        String responseBody = getJson("/v1/models");
        try {
            return objectMapper.readValue(responseBody, ModelsResponse.class);
        } catch (Exception e) {
            throw new OpenClawHttpException("Failed to parse models response: " + e.getMessage(), e);
        }
    }

    /**
     * 获取单个模型/agent 目标信息。
     * <p>
     * 对应 {@code GET /v1/models/{id}}。
     * </p>
     *
     * @param modelId 模型标识（如 {@code "openclaw/default"}）
     * @return 模型信息
     */
    public ModelsResponse.ModelData getModel(String modelId) {
        Objects.requireNonNull(modelId, "modelId");
        String responseBody = getJson("/v1/models/" + modelId);
        try {
            return objectMapper.readValue(responseBody, ModelsResponse.ModelData.class);
        } catch (Exception e) {
            throw new OpenClawHttpException("Failed to parse model response: " + e.getMessage(), e);
        }
    }

    // ============================================================
    // Embeddings
    // ============================================================

    /**
     * 创建嵌入向量。
     * <p>
     * 对应 {@code POST /v1/embeddings}。
     * 使用与 Chat Completions 相同的 agent 目标约定。
     * 如需指定嵌入模型，使用 HTTP 头 {@code x-openclaw-model}。
     * </p>
     *
     * @param request 请求体
     * @return 嵌入向量响应
     */
    public EmbeddingsResponse createEmbeddings(EmbeddingsRequest request) {
        Objects.requireNonNull(request, "request");
        String responseBody = postJson("/v1/embeddings", request);
        try {
            return objectMapper.readValue(responseBody, EmbeddingsResponse.class);
        } catch (Exception e) {
            throw new OpenClawHttpException("Failed to parse embeddings response: " + e.getMessage(), e);
        }
    }

    // ============================================================
    // OpenResponses
    // ============================================================

    /**
     * 发送 OpenResponses 请求（非流式）。
     * <p>
     * 对应 {@code POST /v1/responses}，{@code stream} 为 {@code false} 或未设置。
     * </p>
     *
     * @param request 请求体
     * @return 解析后的响应
     */
    public io.github.hiwepy.openclaw.api.model.ResponseResult createResponse(
            io.github.hiwepy.openclaw.api.model.ResponseRequest request) {
        Objects.requireNonNull(request, "request");
        String responseBody = postJson("/v1/responses", request);
        try {
            return objectMapper.readValue(responseBody,
                    io.github.hiwepy.openclaw.api.model.ResponseResult.class);
        } catch (Exception e) {
            throw new OpenClawHttpException("Failed to parse response result: " + e.getMessage(), e);
        }
    }

    // ============================================================
    // HTTP primitives
    // ============================================================

    /**
     * 发送 POST 请求并返回响应体字符串。
     */
    private String postJson(String path, Object body) {
        String url = resolveUrl(path);
        String token = config.resolveHooksBearerToken();
        try {
            String json = objectMapper.writeValueAsString(body);
            log.debug("POST {} bodyLen={}", url, json.length());
            kong.unirest.core.HttpRequestWithBody req = http.post(url)
                    .header("Content-Type", "application/json");
            if (OpenClawStrings.isNotBlank(token)) {
                req = req.header("Authorization", "Bearer " + token);
            }
            HttpResponse<String> response = req.body(json).asString();
            int status = response.getStatus();
            String respBody = response.getBody();
            if (status < 200 || status >= 300) {
                log.warn("POST {} returned status={}", url, status);
                throw new OpenClawHttpException(
                        "POST " + url + " returned status " + status, status, respBody);
            }
            return respBody;
        } catch (OpenClawHttpException e) {
            throw e;
        } catch (Exception e) {
            log.error("POST {} failed: {}", url, e.getMessage(), e);
            throw new OpenClawHttpException("POST " + url + " failed: " + e.getMessage(), e);
        }
    }

    /**
     * 发送 GET 请求并返回响应体字符串。
     */
    private String getJson(String path) {
        String url = resolveUrl(path);
        String token = config.resolveHooksBearerToken();
        try {
            kong.unirest.core.GetRequest req = http.get(url);
            if (OpenClawStrings.isNotBlank(token)) {
                req = req.header("Authorization", "Bearer " + token);
            }
            HttpResponse<String> response = req.asString();
            int status = response.getStatus();
            String respBody = response.getBody();
            if (status < 200 || status >= 300) {
                log.warn("GET {} returned status={}", url, status);
                throw new OpenClawHttpException(
                        "GET " + url + " returned status " + status, status, respBody);
            }
            return respBody;
        } catch (OpenClawHttpException e) {
            throw e;
        } catch (Exception e) {
            log.error("GET {} failed: {}", url, e.getMessage(), e);
            throw new OpenClawHttpException("GET " + url + " failed: " + e.getMessage(), e);
        }
    }

    private String resolveUrl(String path) {
        String base = config.getGatewayBaseUrl();
        if (OpenClawStrings.isBlank(base)) {
            throw new OpenClawHttpException("gatewayBaseUrl is empty", null);
        }
        return base.replaceAll("/+$", "") + path;
    }

    /**
     * 关闭底层 HTTP 客户端。
     */
    public void close() {
        try {
            http.close();
        } catch (Exception ignored) {
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
}
