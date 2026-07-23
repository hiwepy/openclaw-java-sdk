package io.github.easy4j.openclaw.ws.protocol.params;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

/**
 * {@code sessions.list} RPC 参数。
 * <p>对齐 {@code SessionsListParamsSchema}（{@code src/gateway/protocol/schema/sessions.ts}）。</p>
 */
@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SessionsListParams {

    private final Integer limit;
    private final Integer activeMinutes;
    private final Boolean includeGlobal;
    private final Boolean includeUnknown;
    private final Boolean configuredAgentsOnly;
    private final Boolean includeDerivedTitles;
    private final Boolean includeLastMessage;
    private final String label;
    private final String spawnedBy;
    private final String agentId;
    private final String search;

    /**
     * 无筛选条件的默认列表请求。
     */
    public static SessionsListParams defaults() {
        return SessionsListParams.builder().build();
    }
}
