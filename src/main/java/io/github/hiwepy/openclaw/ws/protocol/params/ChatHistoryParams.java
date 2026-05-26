package io.github.hiwepy.openclaw.ws.protocol.params;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

import java.util.Objects;

/**
 * {@code chat.history} RPC 参数。
 * <p>对齐 {@code ChatHistoryParamsSchema}（{@code src/gateway/protocol/schema/logs-chat.ts}）。</p>
 */
@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ChatHistoryParams {

    private final String sessionKey;
    private final Integer limit;
    private final Integer maxChars;

    /**
     * @param sessionKey 会话键（必填）
     * @param limit      最大消息条数（可选，Gateway 默认 200，上限 1000）
     */
    public static ChatHistoryParams of(String sessionKey, Integer limit) {
        Objects.requireNonNull(sessionKey, "sessionKey");
        return ChatHistoryParams.builder().sessionKey(sessionKey).limit(limit).build();
    }
}
