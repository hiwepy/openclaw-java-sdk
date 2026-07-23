package io.github.easy4j.openclaw.api;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.easy4j.openclaw.OpenClawHttpClientConfig;
import io.github.easy4j.openclaw.exception.OpenClawHttpException;
import io.github.easy4j.openclaw.util.OpenClawStrings;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * HTTP 客户端基类。
 * <p>
 * 封装 OkHttp 和 ObjectMapper 的配置，提供通用的 HTTP 请求方法。
 * </p>
 */
@Getter
@Slf4j
public abstract class OpenClawHttpClient implements AutoCloseable {

    protected static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    protected final OpenClawHttpClientConfig config;
    protected final ObjectMapper objectMapper;
    protected final OkHttpClient httpClient;

    protected OpenClawHttpClient(OpenClawHttpClientConfig config) {
        this(config, null, null);
    }

    protected OpenClawHttpClient(OpenClawHttpClientConfig config, ObjectMapper objectMapper, OkHttpClient httpClient) {
        this.config = Objects.requireNonNull(config, "config");
        this.objectMapper = objectMapper != null ? objectMapper : createObjectMapper();
        this.httpClient = httpClient != null ? httpClient : buildOkHttpClient(config);
    }

    protected ObjectMapper createObjectMapper() {
        return new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    protected OkHttpClient buildOkHttpClient(OpenClawHttpClientConfig config) {
        OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .connectTimeout(config.getConnectTimeoutMillis(), TimeUnit.MILLISECONDS)
                .readTimeout(config.getReadTimeoutMillis(), TimeUnit.MILLISECONDS);
        if (!config.isVerifySsl()) {
            builder.hostnameVerifier((hostname, session) -> true);
        }
        return builder.build();
    }

    // ============================================================
    // HTTP primitives
    // ============================================================

    /**
     * 构建带认证的请求。
     */
    protected Request.Builder authedBuilder(String url) {
        return authedBuilder(url, null);
    }

    /**
     * 构建带认证的请求，追加额外请求头。
     */
    protected Request.Builder authedBuilder(String url, Map<String, String> headers) {
        debug("Building request: url={}", url);

        Request.Builder builder = new Request.Builder().url(url)
                .header("Content-Type", "application/json");

        String token = config.resolveGatewayBearerToken();
        if (OpenClawStrings.isNotBlank(token)) {
            builder.header("Authorization", "Bearer " + token);
            debug("Added Authorization header");
        } else {
            warn("No gateway bearer token configured");
        }

        if (headers != null && !headers.isEmpty()) {
            headers.forEach((k, v) -> {
                if (k != null && v != null) {
                    builder.header(k, v);
                    debug("Added header: {}={}", k, v);
                }
            });
        }

        return builder;
    }

    /**
     * POST JSON 请求。
     */
    protected String postJson(String path, Object body) {
        return postJson(path, body, null);
    }

    /**
     * POST JSON 请求，带额外请求头。
     */
    protected String postJson(String path, Object body, Map<String, String> headers) {
        String url = resolveUrl(path);
        debug("POST JSON: path={}, url={}", path, url);

        try {
            String json = objectMapper.writeValueAsString(body);
            debug("Request body: {}", json);

            Request request = authedBuilder(url, headers)
                    .post(RequestBody.create(json, JSON))
                    .build();

            return execute(request, url);
        } catch (OpenClawHttpException e) {
            throw e;
        } catch (IOException e) {
            throw new OpenClawHttpException("POST " + url + " failed: " + e.getMessage(), e);
        }
    }

    /**
     * GET JSON 请求。
     */
    protected String getJson(String path) {
        String url = resolveUrl(path);
        debug("GET JSON: path={}, url={}", path, url);

        try {
            Request request = authedBuilder(url).get().build();
            return execute(request, url);
        } catch (OpenClawHttpException e) {
            throw e;
        } catch (IOException e) {
            throw new OpenClawHttpException("GET " + url + " failed: " + e.getMessage(), e);
        }
    }

    /**
     * 执行请求。
     */
    protected String execute(Request request, String url) throws IOException {
        debug("Executing request: {} {}", request.method(), request.url());
        debug("Request headers: {}", request.headers());

        try (Response response = httpClient.newCall(request).execute()) {
            int status = response.code();
            String respBody = response.body() != null ? response.body().string() : "";

            debug("Response status: {}, body length: {}", status, respBody.length());
            if (status >= 300) {
                debug("Response body (error): {}", respBody);
            } else if (respBody.length() < 500) {
                debug("Response body: {}", respBody);
            } else {
                debug("Response body (truncated): {}...", respBody.substring(0, 500));
            }

            if (!response.isSuccessful()) {
                throw new OpenClawHttpException("Request returned status " + status, status, respBody);
            }
            return respBody;
        }
    }

    /**
     * 解析 JSON 响应。
     */
    protected <T> T parse(String json, Class<T> type) {
        try {
            return objectMapper.readValue(json, type);
        } catch (IOException e) {
            throw new OpenClawHttpException("Failed to parse response: " + e.getMessage(), e);
        }
    }

    /**
     * 解析 JSON 响应，带标签。
     */
    protected <T> T parse(String json, Class<T> type, String label) {
        try {
            return objectMapper.readValue(json, type);
        } catch (IOException e) {
            throw new OpenClawHttpException("Failed to parse " + label + " response: " + e.getMessage(), e);
        }
    }

    /**
     * 解析 URL。
     */
    protected String resolveUrl(String path) {
        String base = config.getGatewayBaseUrl();
        if (OpenClawStrings.isBlank(base)) {
            throw new OpenClawHttpException("gatewayBaseUrl is empty", null);
        }
        return base.replaceAll("/+$", "") + path;
    }

    // ============================================================
    // Logging helpers
    // ============================================================

    protected void debug(String msg, Object... args) {
        if (log.isDebugEnabled()) {
            log.debug(msg, args);
        }
    }

    protected void info(String msg, Object... args) {
        if (log.isInfoEnabled()) {
            log.info(msg, args);
        }
    }

    protected void warn(String msg, Object... args) {
        log.warn(msg, args);
    }

    protected void error(String msg, Object... args) {
        log.error(msg, args);
    }

    @Override
    public void close() {
        // 外部传入的 OkHttpClient 不关闭，由创建者管理
    }
}
