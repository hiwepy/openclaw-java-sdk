package io.github.hiwepy.openclaw.api;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.hiwepy.openclaw.api.OpenClawClientConfig;
import io.github.hiwepy.openclaw.api.http.OpenClawHttpClient;
import io.github.hiwepy.openclaw.api.model.ToolInvokeRequest;
import io.github.hiwepy.openclaw.api.model.ToolInvokeResult;
import io.github.hiwepy.openclaw.util.OpenClawStrings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

/**
 * Tools Invoke HTTP API 客户端 —— {@code POST /tools/invoke}。
 */
public class OpenClawToolsInvokeClient implements AutoCloseable {

    private static final Logger log = LoggerFactory.getLogger(OpenClawToolsInvokeClient.class);

    private final OpenClawClientConfig config;
    private final ObjectMapper objectMapper;
    private final OpenClawHttpClient httpClient;

    public OpenClawToolsInvokeClient(OpenClawClientConfig config, ObjectMapper mapper,
                                     OpenClawHttpClient httpClient) {
        this.config = Objects.requireNonNull(config, "config");
        this.objectMapper = mapper != null ? mapper : new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        this.httpClient = Objects.requireNonNull(httpClient, "httpClient");
    }

    public OpenClawToolsInvokeClient(OpenClawClientConfig config, OpenClawHttpClient httpClient) {
        this(config, null, httpClient);
    }

    public ToolInvokeResult invoke(ToolInvokeRequest request) {
        Objects.requireNonNull(request, "request");
        if (OpenClawStrings.isBlank(request.tool())) {
            throw new IllegalArgumentException("tool name is required");
        }
        return httpClient.postJsonWithStatusHandling("/tools/invoke", request, null,
                ToolInvokeResult.class,
                (status, body) -> {
                    if (status == 404) {
                        return new ToolInvokeResult(false, null,
                                new ToolInvokeResult.ErrorDetail("not_found",
                                        "Tool not available: " + request.tool()));
                    }
                    return null;
                });
    }

    @Override
    public void close() {
        httpClient.close();
    }
}
