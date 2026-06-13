package io.github.hiwepy.openclaw.api;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.hiwepy.openclaw.OpenClawClientConfig;
import io.github.hiwepy.openclaw.util.OpenClawStrings;
import io.github.hiwepy.openclaw.exception.OpenClawHttpException;
import io.github.hiwepy.openclaw.api.model.ToolInvokeRequest;
import io.github.hiwepy.openclaw.api.model.ToolInvokeResult;
import kong.unirest.core.HttpResponse;
import kong.unirest.core.Unirest;
import kong.unirest.core.UnirestInstance;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;

/**
 * Tools Invoke HTTP API 客户端。
 * <p>
 * 封装 OpenClaw Gateway 的 {@code POST /tools/invoke} 端点，
 * 用于直接调用单个工具而无需运行完整的 agent 回合。
 * </p>
 *
 * <h3>特性</h3>
 * <ul>
 *   <li>始终启用（无需额外配置）</li>
 *   <li>使用 Gateway 鉴权 + 工具策略过滤</li>
 *   <li>共享密钥鉴权被视为受信任的 operator 访问</li>
 *   <li>默认 payload 大小限制 2MB</li>
 * </ul>
 *
 * <h3>安全边界</h3>
 * <p>此端点是 Gateway 的全 operator 访问表面。
 * 有效的 Gateway token/password 应被视为 owner/operator 凭据。
 * 不要将 Gateway 凭据与不受信任的调用方共享。</p>
 *
 * @see <a href="https://docs.openclaw.ai/gateway/tools-invoke-http-api">Tools Invoke API</a>
 */
@Slf4j
public class OpenClawToolsInvokeClient implements AutoCloseable {

    private final OpenClawClientConfig config;
    private final ObjectMapper objectMapper;
    private final UnirestInstance http;

    public OpenClawToolsInvokeClient(OpenClawClientConfig config, ObjectMapper mapper) {
        this.config = Objects.requireNonNull(config, "config");
        this.objectMapper = mapper != null ? mapper : new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        this.http = buildHttpClient(config);
    }

    public OpenClawToolsInvokeClient(OpenClawClientConfig config) {
        this(config, null);
    }

    /**
     * 调用单个工具。
     * <p>
     * 对应 {@code POST /tools/invoke}。
     * </p>
     *
     * @param request 工具调用请求
     * @return 工具调用结果（包含 {@code ok} 和 {@code result} 或 {@code error}）
     */
    public ToolInvokeResult invoke(ToolInvokeRequest request) {
        Objects.requireNonNull(request, "request");
        if (OpenClawStrings.isBlank(request.getTool())) {
            throw new IllegalArgumentException("tool name is required");
        }

        String url = config.getGatewayBaseUrl().replaceAll("/+$", "") + "/tools/invoke";
        String token = config.resolveGatewayBearerToken();
        try {
            String json = objectMapper.writeValueAsString(request);
            log.debug("POST /tools/invoke tool={} bodyLen={}", request.getTool(), json.length());
            kong.unirest.core.HttpRequestWithBody req = http.post(url)
                    .header("Content-Type", "application/json");
            if (OpenClawStrings.isNotBlank(token)) {
                req = req.header("Authorization", "Bearer " + token);
            }
            HttpResponse<String> response = req.body(json).asString();
            int status = response.getStatus();
            String respBody = response.getBody();
                if (status == 404) {
                    ToolInvokeResult result = new ToolInvokeResult();
                    result.setOk(false);
                    ToolInvokeResult.ErrorDetail error = new ToolInvokeResult.ErrorDetail();
                    error.setType("not_found");
                    error.setMessage("Tool not available: " + request.getTool());
                    result.setError(error);
                    return result;
                }
                if (status < 200 || status >= 300) {
                    log.warn("POST /tools/invoke returned status={}", status);
                    throw new OpenClawHttpException(
                            "POST /tools/invoke returned status " + status, status, respBody);
                }
                return objectMapper.readValue(respBody, ToolInvokeResult.class);
        } catch (OpenClawHttpException e) {
            throw e;
        } catch (Exception e) {
            log.error("POST /tools/invoke failed: {}", e.getMessage(), e);
            throw new OpenClawHttpException("POST /tools/invoke failed: " + e.getMessage(), e);
        }
    }

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
