package io.github.hiwepy.openclaw.ws.protocol;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Gateway RPC 错误结构。
 */
public class ErrorShape {

    private final String code;
    private final String message;
    private final Object details;
    private final Boolean retryable;
    private final Integer retryAfterMs;

    @JsonCreator
    public ErrorShape(
            @JsonProperty("code") String code,
            @JsonProperty("message") String message,
            @JsonProperty("details") Object details,
            @JsonProperty("retryable") Boolean retryable,
            @JsonProperty("retryAfterMs") Integer retryAfterMs) {
        this.code = code;
        this.message = message;
        this.details = details;
        this.retryable = retryable;
        this.retryAfterMs = retryAfterMs;
    }

    public String getCode() { return code; }
    public String getMessage() { return message; }
    public Object getDetails() { return details; }
    public Boolean getRetryable() { return retryable; }
    public Integer getRetryAfterMs() { return retryAfterMs; }

    @Override
    public String toString() {
        return "ErrorShape{code='" + code + "', message='" + message + "'}";
    }
}
