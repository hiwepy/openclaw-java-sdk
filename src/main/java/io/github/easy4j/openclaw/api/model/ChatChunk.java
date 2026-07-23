package io.github.easy4j.openclaw.api.model;

import io.github.easy4j.openclaw.api.OpenClawConstants;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * OpenAI Chat Completions API 流式响应块。
 * <p>
 * 当 {@code stream: true} 时，Gateway 以 SSE 格式推送多个此类对象。
 * 每个 SSE 事件格式为 {@code data: <json>}，流以 {@code data: [DONE]} 结束。
 * </p>
 *
 * <h3>工具调用流式响应</h3>
 * <p>当 agent 决定调用工具时，流式响应包含：</p>
 * <ol>
 *   <li>初始 assistant 角色 delta</li>
 *   <li>可选的 assistant 说明 delta</li>
 *   <li>一个或多个 {@code delta.toolCalls} 块，携带工具标识和参数片段</li>
 *   <li>最终块，{@code finishReason} 为 {@code "tool_calls"}</li>
 *   <li>{@code data: [DONE]}</li>
 * </ol>
 *
 * @see <a href="https://docs.openclaw.ai/gateway/openai-http-api">OpenAI Chat Completions</a>
 */
@Getter
@Setter
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ChatChunk {

    /** 响应唯一标识。 */
    private String id;

    /** 对象类型，固定为 {@code "chat.completion.chunk"}。 */
    private String object = OpenClawConstants.OBJECT_CHAT_COMPLETION_CHUNK;

    /** 创建时间戳（Unix epoch 秒）。 */
    private Long created;

    /** 使用的 agent 目标标识。 */
    private String model;

    /** 选择列表（通常只有一个元素）。 */
    private List<DeltaChoice> choices;

    /**
     * 流式响应中的选择。
     * <p>注意：流式响应中的消息使用 {@code delta} 字段而非 {@code message} 字段。</p>
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class DeltaChoice {
        private Integer index;

        /**
         * 增量消息内容。
         * <p>包含角色、内容片段、工具调用增量等。</p>
         */
        private DeltaMessage delta;

        /**
         * 完成原因（仅在最后一个块中非 null）。
         * <ul>
         *   <li>{@code "stop"} - 正常完成</li>
         *   <li>{@code "tool_calls"} - agent 请求调用客户端工具</li>
         * </ul>
         */
        @JsonProperty("finish_reason")
        private String finishReason;

        /** 判定是否为工具调用完成 */
        public boolean isToolCalls() {
            return OpenClawConstants.FINISH_REASON_TOOL_CALLS.equals(finishReason);
        }
    }

    /**
     * 增量消息。
     * <p>包含角色、内容片段、工具调用增量等字段。</p>
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class DeltaMessage {
        /** 消息角色（仅第一个块中存在）。 */
        private String role;
        /** 增量文本内容。 */
        private String content;
        /** 工具调用增量（用于流式传递工具标识和参数片段）。 */
        @JsonProperty("tool_calls")
        private List<ChatMessage.ToolCall> toolCalls;
    }
}
