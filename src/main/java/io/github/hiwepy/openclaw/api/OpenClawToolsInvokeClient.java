package io.github.hiwepy.openclaw.api;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.hiwepy.openclaw.OpenClawHttpClientConfig;
import io.github.hiwepy.openclaw.util.OpenClawStrings;
import io.github.hiwepy.openclaw.exception.OpenClawHttpException;
import io.github.hiwepy.openclaw.api.model.ToolInvokeRequest;
import io.github.hiwepy.openclaw.api.model.ToolInvokeResult;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * Tools Invoke HTTP API 客户端。
 * <p>基于 OkHttp，支持外部传入 {@link OkHttpClient}。</p>
 */
@Slf4j
public class OpenClawToolsInvokeClient implements AutoCloseable {

    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    private final OpenClawHttpClientConfig config;
    private final ObjectMapper objectMapper;
    private final OkHttpClient httpClient;

    public OpenClawToolsInvokeClient(OpenClawHttpClientConfig config, ObjectMapper mapper) {
        this(config, mapper, null);
    }

    public OpenClawToolsInvokeClient(OpenClawHttpClientConfig config) {
        this(config, null, null);
    }

    public OpenClawToolsInvokeClient(OpenClawHttpClientConfig config, ObjectMapper mapper, OkHttpClient httpClient) {
        this.config = Objects.requireNonNull(config, "config");
        this.objectMapper = mapper != null ? mapper : new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
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

    public ToolInvokeResult invoke(ToolInvokeRequest request) {
        Objects.requireNonNull(request, "request");
        if (OpenClawStrings.isBlank(request.getTool())) {
            throw new IllegalArgumentException("tool name is required");
        }

        String url = config.getGatewayBaseUrl().replaceAll("/+$", "") + OpenClawConstants.ENDPOINT_TOOLS_INVOKE;
        String token = config.resolveGatewayBearerToken();
        try {
            Request.Builder builder = new Request.Builder().url(url).header("Content-Type", "application/json");
            if (OpenClawStrings.isNotBlank(token)) {
                builder.header("Authorization", "Bearer " + token);
            }
            Request httpRequest = builder.post(RequestBody.create(objectMapper.writeValueAsString(request), JSON)).build();
            try (Response response = httpClient.newCall(httpRequest).execute()) {
                int status = response.code();
                String respBody = response.body() != null ? response.body().string() : "";

                if (status == 404) {
                    ToolInvokeResult result = new ToolInvokeResult();
                    result.setOk(false);
                    ToolInvokeResult.ErrorDetail error = new ToolInvokeResult.ErrorDetail();
                    error.setType(ToolInvokeResult.ERROR_TYPE_NOT_FOUND);
                    error.setMessage("Tool not available: " + request.getTool());
                    result.setError(error);
                    return result;
                }
                if (status < 200 || status >= 300) {
                    throw new OpenClawHttpException("POST /tools/invoke returned status " + status, status, respBody);
                }
                return objectMapper.readValue(respBody, ToolInvokeResult.class);
            }
        } catch (OpenClawHttpException e) {
            throw e;
        } catch (Exception e) {
            throw new OpenClawHttpException("POST /tools/invoke failed: " + e.getMessage(), e);
        }
    }

    @Override
    public void close() {
    }
}
