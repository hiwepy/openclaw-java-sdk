package io.github.hiwepy.openclaw.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 调用 OpenClaw Gateway {@code POST /hooks/agent} 的请求体。
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record InvokeAgentRequest(
        @JsonProperty("message") String message,
        @JsonProperty("agentId") String agentId,
        @JsonProperty("name") String name,
        @JsonProperty("wakeMode") String wakeMode,
        @JsonProperty("timeoutSeconds") Integer timeoutSeconds,
        @JsonProperty("sessionKey") String sessionKey,
        @JsonProperty("deliver") Boolean deliver,
        @JsonProperty("channel") String channel,
        @JsonProperty("to") String to,
        @JsonProperty("model") String model,
        @JsonProperty("thinking") String thinking) {

    public InvokeAgentRequest {
        if (name == null) name = "Generation";
        if (wakeMode == null) wakeMode = "now";
        if (timeoutSeconds == null) timeoutSeconds = 300;
    }

    public InvokeAgentRequest(String agentId, String message) {
        this(message, agentId, "Generation", "now", 300, null, null, null, null, null, null);
    }

    public InvokeAgentRequest withSessionKey(String sessionKey) {
        return new InvokeAgentRequest(message, agentId, name, wakeMode, timeoutSeconds,
                sessionKey, deliver, channel, to, model, thinking);
    }
}
