package io.github.hiwepy.openclaw.ws.protocol.params;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

/**
 * {@code agent.identity.get} RPC 参数。
 * <p>对齐 {@code AgentIdentityParamsSchema}（{@code src/gateway/protocol/schema/agent.ts}）。</p>
 */
@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AgentIdentityGetParams {

    private final String agentId;
    private final String sessionKey;

    public static AgentIdentityGetParams forAgent(String agentId) {
        return AgentIdentityGetParams.builder().agentId(agentId).build();
    }

    public static AgentIdentityGetParams forSession(String sessionKey) {
        return AgentIdentityGetParams.builder().sessionKey(sessionKey).build();
    }

    public static AgentIdentityGetParams empty() {
        return AgentIdentityGetParams.builder().build();
    }
}
