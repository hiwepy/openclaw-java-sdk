package io.github.hiwepy.openclaw.api.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.hiwepy.openclaw.api.OpenClawConstants;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * OpenAI Chat Completions API 非流式响应。
 * <p>
 * 对应 {@code POST /v1/chat/completions}（{@code stream: false}）返回的 JSON。
 * 当 agent 决定调用工具时，{@code choices[0].finish_reason} 为 {@code "tool_calls"}，
 * {@code choices[0].message.toolCalls} 包含工具调用列表。
 * </p>
 *
 * <h3>工具跟进循环</h3>
 * <p>收到工具调用后，客户端应执行对应函数，然后发送包含以下内容的后续请求：</p>
 * <ul>
 *   <li>先前的 assistant 工具调用消息</li>
 *   <li>一个或多个 {@code role: "tool"} 消息，包含匹配的 {@code toolCallId}</li>
 * </ul>
 * <p>这允许 Gateway agent 运行继续推理循环并生成最终回复。</p>
 *
 * @see <a href="https://docs.openclaw.ai/gateway/openai-http-api">OpenAI Chat Completions</a>
 */
@Getter
@Setter
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ChatResponse {

    /** 响应唯一标识。 */
    private String id;

    /** 对象类型，固定为 {@code "chat.completion"}。 */
    private String object = OpenClawConstants.OBJECT_CHAT_COMPLETION;

    /** 创建时间戳（Unix epoch 秒）。 */
    private Long created;

    /** 使用的 agent 目标标识。 */
    private String model;

    /** 选择列表（通常只有一个元素）。 */
    private List<Choice> choices;

    /** Token 使用统计。 */
    private Usage usage;

    /**
     * 响应中的一个选择。
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Choice {

        /** 选择在数组中的索引。 */
        private Integer index;

        /**
         * agent 回复消息。
         * <p>当 {@code finishReason} 为 {@code "tool_calls"} 时，
         * 此消息的 {@code toolCalls} 包含工具调用列表，
         * {@code content} 可能为空字符串（表示 agent 在调用工具前无附加说明）。</p>
         */
        private ChatMessage message;

        /**
         * 完成原因。
         * <ul>
         *   <li>{@code "stop"} - 正常完成</li>
         *   <li>{@code "tool_calls"} - agent 请求调用客户端工具</li>
         *   <li>{@code "length"} - 达到 token 限制</li>
         * </ul>
         */
        @JsonProperty("finish_reason")
        private String finishReason;

        /** 判定是否为正常完成 */
        public boolean isStop() {
            return OpenClawConstants.FINISH_REASON_STOP.equals(finishReason);
        }

        /** 判定是否为工具调用 */
        public boolean isToolCalls() {
            return OpenClawConstants.FINISH_REASON_TOOL_CALLS.equals(finishReason);
        }

        /** 判定是否为长度限制 */
        public boolean isLength() {
            return OpenClawConstants.FINISH_REASON_LENGTH.equals(finishReason);
        }
    }

    /**
     * Token 使用统计。
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Usage {
        /** 输入 token 数（含 prompt token）。 */
        @JsonProperty("prompt_tokens")
        private Integer promptTokens;
        /** 输出 token 数（含 completion token）。 */
        @JsonProperty("completion_tokens")
        private Integer completionTokens;
        /** 总 token 数。 */
        @JsonProperty("total_tokens")
        private Integer totalTokens;
    }
}
