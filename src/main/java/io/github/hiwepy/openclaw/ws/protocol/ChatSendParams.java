package io.github.hiwepy.openclaw.ws.protocol;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * {@code chat.send} RPC 参数。
 * <p>与 {@code src/gateway/server-methods/chat.ts} 中 {@code chat.send} handler 对齐。</p>
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ChatSendParams {

    private final String sessionKey;
    private final String message;
    private final String thinking;
    private final Boolean deliver;
    private final String originatingChannel;
    private final String originatingTo;
    private final Integer timeoutMs;

    private ChatSendParams(Builder b) {
        this.sessionKey = b.sessionKey;
        this.message = b.message;
        this.thinking = b.thinking;
        this.deliver = b.deliver;
        this.originatingChannel = b.originatingChannel;
        this.originatingTo = b.originatingTo;
        this.timeoutMs = b.timeoutMs;
    }

    public String getSessionKey() { return sessionKey; }
    public String getMessage() { return message; }
    public String getThinking() { return thinking; }
    public Boolean getDeliver() { return deliver; }
    public String getOriginatingChannel() { return originatingChannel; }
    public String getOriginatingTo() { return originatingTo; }
    public Integer getTimeoutMs() { return timeoutMs; }

    /**
     * 构建为 RPC params Map。
     */
    public Map<String, Object> toParamsMap() {
        Map<String, Object> m = new LinkedHashMap<>();
        if (sessionKey != null) m.put("sessionKey", sessionKey);
        m.put("message", message);
        if (thinking != null) m.put("thinking", thinking);
        if (deliver != null) m.put("deliver", deliver);
        if (originatingChannel != null) m.put("originatingChannel", originatingChannel);
        if (originatingTo != null) m.put("originatingTo", originatingTo);
        if (timeoutMs != null) m.put("timeoutMs", timeoutMs);
        return m;
    }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private String sessionKey;
        private String message;
        private String thinking;
        private Boolean deliver;
        private String originatingChannel;
        private String originatingTo;
        private Integer timeoutMs;

        public Builder sessionKey(String sessionKey) { this.sessionKey = sessionKey; return this; }
        public Builder message(String message) { this.message = message; return this; }
        public Builder thinking(String thinking) { this.thinking = thinking; return this; }
        public Builder deliver(Boolean deliver) { this.deliver = deliver; return this; }
        public Builder originatingChannel(String ch) { this.originatingChannel = ch; return this; }
        public Builder originatingTo(String to) { this.originatingTo = to; return this; }
        public Builder timeoutMs(Integer ms) { this.timeoutMs = ms; return this; }
        public ChatSendParams build() { return new ChatSendParams(this); }
    }
}
