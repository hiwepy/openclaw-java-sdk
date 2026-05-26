package io.github.hiwepy.openclaw.ws.protocol;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * {@code sessions.send} RPC 参数。
 * <p>与 {@code src/gateway/protocol/schema/sessions.ts} 中 {@code SessionsSendParamsSchema} 对齐。</p>
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SessionsSendParams {

    private final String key;
    private final String message;
    private final String thinking;
    private final Integer timeoutMs;

    private SessionsSendParams(Builder b) {
        this.key = b.key;
        this.message = b.message;
        this.thinking = b.thinking;
        this.timeoutMs = b.timeoutMs;
    }

    public String getKey() { return key; }
    public String getMessage() { return message; }
    public String getThinking() { return thinking; }
    public Integer getTimeoutMs() { return timeoutMs; }

    public Map<String, Object> toParamsMap() {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("key", key);
        m.put("message", message);
        if (thinking != null) m.put("thinking", thinking);
        if (timeoutMs != null) m.put("timeoutMs", timeoutMs);
        return m;
    }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private String key;
        private String message;
        private String thinking;
        private Integer timeoutMs;

        public Builder key(String key) { this.key = key; return this; }
        public Builder message(String message) { this.message = message; return this; }
        public Builder thinking(String thinking) { this.thinking = thinking; return this; }
        public Builder timeoutMs(Integer ms) { this.timeoutMs = ms; return this; }
        public SessionsSendParams build() { return new SessionsSendParams(this); }
    }
}
