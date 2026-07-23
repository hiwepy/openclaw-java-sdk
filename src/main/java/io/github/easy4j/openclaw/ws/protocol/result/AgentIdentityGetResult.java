package io.github.easy4j.openclaw.ws.protocol.result;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

/**
 * {@code agent.identity.get} RPC 成功响应体。
 * <p>对齐 {@code AgentIdentityResultSchema}。</p>
 */
@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class AgentIdentityGetResult {

    @JsonProperty("agentId")
    private String agentId;

    @JsonProperty("name")
    private String name;

    @JsonProperty("avatar")
    private String avatar;

    @JsonProperty("avatarSource")
    private String avatarSource;

    @JsonProperty("avatarStatus")
    private String avatarStatus;

    @JsonProperty("avatarReason")
    private String avatarReason;

    @JsonProperty("emoji")
    private String emoji;
}
