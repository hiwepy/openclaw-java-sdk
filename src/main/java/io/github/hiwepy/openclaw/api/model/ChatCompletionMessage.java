package io.github.hiwepy.openclaw.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * OpenAI Chat Completions API 消息对象。
 * <p>
 * 对应 OpenAI {@code /v1/chat/completions} 请求和响应中的 {@code messages} 数组元素。
 * 支持角色：{@code system}、{@code user}、{@code assistant}、{@code tool}。
 * </p>
 *
 * <p>当响应中 {@code finish_reason} 为 {@code tool_calls} 时，
 * {@code tool_calls} 字段包含 agent 请求调用的工具列表。</p>
 *
 * @see <a href="https://docs.openclaw.ai/gateway/openai-http-api">OpenAI Chat Completions</a>
 */
@Getter
@Setter
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ChatCompletionMessage {

    /**
     * 消息角色。
     * <ul>
     *   <li>{@code system} - 系统提示</li>
     *   <li>{@code user} - 用户输入</li>
     *   <li>{@code assistant} - agent 回复</li>
     *   <li>{@code tool} - 工具调用结果</li>
     * </ul>
     */
    private String role;

    /**
     * 消息文本内容。
     * <p>对于 {@code assistant} 角色，当同时返回工具调用时，此字段可能为空字符串。</p>
     */
    private String content;

    /**
     * 工具调用列表（仅 {@code assistant} 角色，且 {@code finish_reason} 为 {@code tool_calls} 时非空）。
     */
    private List<ToolCall> toolCalls;

    /**
     * 工具调用 ID（仅 {@code tool} 角色），用于将工具结果绑定到先前的工具调用。
     */
    private String toolCallId;

    /**
     * 工具调用对象。
     * <p>包含工具调用的 ID、类型、函数名称和参数。</p>
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ToolCall {
        /** 工具调用唯一标识符，如 {@code call_abc123}。 */
        private String id;
        /** 工具类型，固定为 {@code function}。 */
        private String type;
        /** 函数调用详情。 */
        private FunctionCall function;
    }

    /**
     * 函数调用详情。
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class FunctionCall {
        /** 函数名称。 */
        private String name;
        /**
         * 函数参数（JSON 字符串）。
         * <p>调用方需自行解析此 JSON 字符串为具体参数对象。</p>
         */
        private String arguments;
    }
}
