package io.github.hiwepy.openclaw.ws.protocol.params;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

/**
 * {@code cron.list} RPC 参数。
 * <p>对齐 {@code CronListParamsSchema}（{@code src/gateway/protocol/schema/cron.ts}）。</p>
 */
@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CronListParams {

    private final Boolean includeDisabled;
    private final Integer limit;
    private final Integer offset;
    private final String query;
    /** {@code all} | {@code enabled} | {@code disabled} */
    private final String enabled;
    /** {@code nextRunAtMs} | {@code updatedAtMs} | {@code name} */
    private final String sortBy;
    /** {@code asc} | {@code desc} */
    private final String sortDir;
    private final String agentId;

    public static CronListParams defaults() {
        return CronListParams.builder().build();
    }
}
