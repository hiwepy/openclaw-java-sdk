package io.github.hiwepy.openclaw.ws.protocol;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.ToString;

/**
 * Gateway RPC 错误结构。
 */
@Getter
@ToString(of = {"code", "message"})
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
}
