package io.github.easy4j.openclaw.ws.protocol.params;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

import java.util.Objects;

/**
 * {@code chat.abort} RPC 参数。
 * <p>对齐 {@code ChatAbortParamsSchema}（{@code src/gateway/protocol/schema/logs-chat.ts}）。</p>
 */
@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ChatAbortParams {

    private final String sessionKey;
    private final String runId;

    /**
     * 中止指定会话上所有进行中的 run。
     */
    public static ChatAbortParams abortSession(String sessionKey) {
        Objects.requireNonNull(sessionKey, "sessionKey");
        return ChatAbortParams.builder().sessionKey(sessionKey).build();
    }

    /**
     * 中止指定 run。
     */
    public static ChatAbortParams abortRun(String sessionKey, String runId) {
        Objects.requireNonNull(sessionKey, "sessionKey");
        Objects.requireNonNull(runId, "runId");
        return ChatAbortParams.builder().sessionKey(sessionKey).runId(runId).build();
    }
}
