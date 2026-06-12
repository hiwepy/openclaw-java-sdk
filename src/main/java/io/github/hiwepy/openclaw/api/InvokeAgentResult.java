package io.github.hiwepy.openclaw.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 调用智能体后的统一结果（HTTP 或本地 CLI）。
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record InvokeAgentResult(
        @JsonProperty("success") boolean success,
        @JsonProperty("httpStatus") int httpStatus,
        @JsonProperty("runId") String runId,
        @JsonProperty("rawBody") String rawBody,
        @JsonProperty("localInvocation") boolean localInvocation) {

    public static InvokeAgentResult empty() {
        return new InvokeAgentResult(false, -1, null, null, false);
    }

    public static InvokeAgentResult http(int status, String rawBody, boolean ok, String runId) {
        return new InvokeAgentResult(ok, status, runId, rawBody, false);
    }
}
