package io.github.hiwepy.openclaw.api.http;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.hiwepy.openclaw.exception.OpenClawHttpException;
import io.github.hiwepy.openclaw.api.sse.SseEventHandler;
import io.github.hiwepy.openclaw.api.sse.SseStreamReader;

import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * Unified HTTP client for all OpenClaw Gateway HTTP endpoints.
 * <p>
 * Uses {@link java.net.http.HttpClient} (JDK 11+) as the single HTTP engine,
 * supporting synchronous JSON POST/GET and SSE streaming via
 * {@link HttpResponse.BodyHandlers#ofLines()}.
 * No Spring, no Unirest, no javax.ws — pure JDK.
 * </p>
 */
public class OpenClawHttpClient implements AutoCloseable {

    private final HttpClient http;
    private final ObjectMapper objectMapper;
    private final String baseUrl;
    private final Supplier<String> bearerTokenProvider;
    private final Duration readTimeout;
    private final SseStreamReader sseReader;

    private OpenClawHttpClient(Builder builder) {
        this.baseUrl = Objects.requireNonNull(builder.baseUrl, "baseUrl")
                .replaceAll("/+$", "");
        this.bearerTokenProvider = Objects.requireNonNull(builder.bearerTokenProvider, "bearerTokenProvider");
        this.readTimeout = Duration.ofMillis(builder.readTimeoutMillis);
        this.objectMapper = builder.objectMapper != null ? builder.objectMapper : new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        if (builder.httpClient != null) {
            this.http = builder.httpClient;
        } else {
            HttpClient.Builder httpBuilder = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofMillis(builder.connectTimeoutMillis))
                    .version(HttpClient.Version.HTTP_1_1);
            if (!builder.verifySsl) {
                httpBuilder.sslContext(TrustAllSsl.createUnsafeContext());
            }
            this.http = httpBuilder.build();
        }
        this.sseReader = new SseStreamReader(this.objectMapper);
    }

    // ============================================================
    // POST
    // ============================================================

    /**
     * POST JSON body, return parsed JSON response.
     */
    public <T> T postJson(String path, Object body, Map<String, String> extraHeaders, Class<T> responseType) {
        String url = resolveUrl(path);
        String json = serialize(body);
        try {
            HttpRequest request = newRequestBuilder(url, extraHeaders)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();
            HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());
            int status = response.statusCode();
            String respBody = response.body();
            if (status < 200 || status >= 300) {
                throw new OpenClawHttpException("POST " + url + " returned status " + status, status, respBody);
            }
            return objectMapper.readValue(respBody, responseType);
        } catch (OpenClawHttpException e) {
            throw e;
        } catch (Exception e) {
            throw new OpenClawHttpException("POST " + url + " failed: " + e.getMessage(), e);
        }
    }

    /**
     * POST JSON body, return raw string response.
     */
    public String postJsonRaw(String path, Object body, Map<String, String> extraHeaders) {
        String url = resolveUrl(path);
        String json = serialize(body);
        try {
            HttpRequest request = newRequestBuilder(url, extraHeaders)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();
            HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());
            int status = response.statusCode();
            String respBody = response.body();
            if (status < 200 || status >= 300) {
                throw new OpenClawHttpException("POST " + url + " returned status " + status, status, respBody);
            }
            return respBody;
        } catch (OpenClawHttpException e) {
            throw e;
        } catch (Exception e) {
            throw new OpenClawHttpException("POST " + url + " failed: " + e.getMessage(), e);
        }
    }

    /**
     * POST JSON body, return parsed JSON response with manual status handling (for 404 etc).
     */
    public <T> T postJsonWithStatusHandling(String path, Object body, Map<String, String> extraHeaders,
                                            Class<T> responseType, StatusHandler<T> statusHandler) {
        String url = resolveUrl(path);
        String json = serialize(body);
        try {
            HttpRequest request = newRequestBuilder(url, extraHeaders)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();
            HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());
            int status = response.statusCode();
            String respBody = response.body();
            T customResult = statusHandler.handle(status, respBody);
            if (customResult != null) {
                return customResult;
            }
            if (status < 200 || status >= 300) {
                throw new OpenClawHttpException("POST " + url + " returned status " + status, status, respBody);
            }
            return objectMapper.readValue(respBody, responseType);
        } catch (OpenClawHttpException e) {
            throw e;
        } catch (Exception e) {
            throw new OpenClawHttpException("POST " + url + " failed: " + e.getMessage(), e);
        }
    }

    // ============================================================
    // GET
    // ============================================================

    /**
     * GET request, return parsed JSON response.
     */
    public <T> T getJson(String path, Class<T> responseType) {
        String url = resolveUrl(path);
        try {
            HttpRequest request = newRequestBuilder(url, null)
                    .GET()
                    .build();
            HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());
            int status = response.statusCode();
            String respBody = response.body();
            if (status < 200 || status >= 300) {
                throw new OpenClawHttpException("GET " + url + " returned status " + status, status, respBody);
            }
            return objectMapper.readValue(respBody, responseType);
        } catch (OpenClawHttpException e) {
            throw e;
        } catch (Exception e) {
            throw new OpenClawHttpException("GET " + url + " failed: " + e.getMessage(), e);
        }
    }

    // ============================================================
    // SSE Streaming
    // ============================================================

    /**
     * POST JSON body and consume SSE stream via {@link SseEventHandler}.
     * <p>
     * Uses {@link HttpResponse.BodyHandlers#ofInputStream()} for efficient stream reading.
     * The SseStreamReader handles line-by-line parsing of SSE events.
     * </p>
     *
     * @param path       path relative to baseUrl
     * @param body       JSON-serializable body
     * @param extraHeaders optional extra headers
     * @param handler    SSE event handler
     * @param chunkClass class to parse each data payload into (null = raw only)
     */
    public void postStream(String path, Object body, Map<String, String> extraHeaders,
                           SseEventHandler handler, Class<?> chunkClass) {
        String url = resolveUrl(path);
        String json = serialize(body);
        try {
            HttpRequest request = newRequestBuilder(url, extraHeaders)
                    .header("Content-Type", "application/json")
                    .header("Accept", "text/event-stream")
                    .header("Cache-Control", "no-cache")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();
            HttpResponse<InputStream> response = http.send(request, HttpResponse.BodyHandlers.ofInputStream());
            int status = response.statusCode();
            if (status < 200 || status >= 300) {
                String errorBody = readErrorBody(response);
                throw new OpenClawHttpException("POST stream " + url + " returned status " + status, status, errorBody);
            }
            try (InputStream is = response.body()) {
                sseReader.readStream(is, handler, chunkClass);
            }
        } catch (OpenClawHttpException e) {
            handler.onError(e);
        } catch (Exception e) {
            handler.onError(new OpenClawHttpException("POST stream " + url + " failed: " + e.getMessage(), e));
        }
    }

    // ============================================================
    // Internal
    // ============================================================

    private String resolveUrl(String path) {
        return baseUrl + (path.startsWith("/") ? path : "/" + path);
    }

    private String serialize(Object body) {
        try {
            return objectMapper.writeValueAsString(body);
        } catch (Exception e) {
            throw new OpenClawHttpException("Failed to serialize request body: " + e.getMessage(), e);
        }
    }

    private HttpRequest.Builder newRequestBuilder(String url, Map<String, String> extraHeaders) {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(this.readTimeout);
        String token = bearerTokenProvider.get();
        if (token != null && !token.isEmpty()) {
            builder.header("Authorization", "Bearer " + token);
        }
        if (extraHeaders != null) {
            extraHeaders.forEach((k, v) -> {
                if (v != null && !v.isEmpty()) {
                    builder.header(k, v);
                }
            });
        }
        return builder;
    }

    private static String readErrorBody(HttpResponse<InputStream> response) {
        try (InputStream is = response.body()) {
            return new String(is.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
        } catch (Exception ignored) {
            return "";
        }
    }

    @Override
    public void close() {
        // HttpClient has no explicit close; connection pools are managed internally
    }

    // ============================================================
    // Builder
    // ============================================================

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private String baseUrl = "http://localhost:18789";
        private Supplier<String> bearerTokenProvider = () -> null;
        private ObjectMapper objectMapper;
        private int connectTimeoutMillis = 15_000;
        private int readTimeoutMillis = 120_000;
        private boolean verifySsl = true;
        private HttpClient httpClient;

        public Builder baseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
            return this;
        }

        public Builder bearerTokenProvider(Supplier<String> tokenProvider) {
            this.bearerTokenProvider = tokenProvider;
            return this;
        }

        public Builder objectMapper(ObjectMapper objectMapper) {
            this.objectMapper = objectMapper;
            return this;
        }

        public Builder connectTimeoutMillis(int ms) {
            this.connectTimeoutMillis = ms;
            return this;
        }

        public Builder readTimeoutMillis(int ms) {
            this.readTimeoutMillis = ms;
            return this;
        }

        public Builder verifySsl(boolean verify) {
            this.verifySsl = verify;
            return this;
        }

        /** Use a pre-configured {@link HttpClient} (shares connection pool). */
        public Builder httpClient(HttpClient httpClient) {
            this.httpClient = httpClient;
            return this;
        }

        public OpenClawHttpClient build() {
            return new OpenClawHttpClient(this);
        }
    }

    /**
     * Functional interface for custom HTTP status handling (e.g. 404 fallback).
     */
    @FunctionalInterface
    public interface StatusHandler<T> {
        T handle(int statusCode, String responseBody);
    }
}
