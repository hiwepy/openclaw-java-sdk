package io.github.easy4j.openclaw.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.easy4j.openclaw.api.OpenClawConstants;
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
 * <h3>用法示例</h3>
 * <pre>{@code
 * // 创建消息
 * ChatMessage msg = ChatMessage.ofUser("Hello");
 * ChatMessage msg = ChatMessage.ofSystem("You are a helpful assistant");
 * ChatMessage msg = ChatMessage.ofAssistant("I can help with that.");
 *
 * // 工具调用响应
 * ChatMessage toolResult = ChatMessage.ofTool("call_abc123", "{\"result\": \"done\"}");
 *
 * // 带有工具调用的助手消息
 * ChatMessage assistantMsg = ChatMessage.ofAssistant(null,
 *     List.of(ToolCall.of("call_abc", "get_weather", "{\"city\": \"Beijing\"}")));
 * }</pre>
 *
 * @see <a href="https://docs.openclaw.ai/gateway/openai-http-api">OpenAI Chat Completions</a>
 */
@Getter
@Setter
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ChatMessage {

    /** 消息角色常量 */
    public static final String ROLE_SYSTEM = OpenClawConstants.ROLE_SYSTEM;
    public static final String ROLE_USER = OpenClawConstants.ROLE_USER;
    public static final String ROLE_ASSISTANT = OpenClawConstants.ROLE_ASSISTANT;
    public static final String ROLE_TOOL = OpenClawConstants.ROLE_TOOL;

    /** 消息角色 */
    private String role;

    /** 消息文本内容 */
    private String content;

    /** 工具调用列表 */
    @JsonProperty("tool_calls")
    private List<ToolCall> toolCalls;

    /** 工具调用 ID（tool 角色使用） */
    @JsonProperty("tool_call_id")
    private String toolCallId;

    // ==================== Factory Methods ====================

    /**
     * 创建系统消息。
     */
    public static ChatMessage ofSystem(String content) {
        ChatMessage msg = new ChatMessage();
        msg.setRole(ROLE_SYSTEM);
        msg.setContent(content);
        return msg;
    }

    /**
     * 创建用户消息。
     */
    public static ChatMessage ofUser(String content) {
        ChatMessage msg = new ChatMessage();
        msg.setRole(ROLE_USER);
        msg.setContent(content);
        return msg;
    }

    /**
     * 创建助手消息。
     */
    public static ChatMessage ofAssistant(String content) {
        ChatMessage msg = new ChatMessage();
        msg.setRole(ROLE_ASSISTANT);
        msg.setContent(content);
        return msg;
    }

    /**
     * 创建助手消息（带工具调用）。
     *
     * @param content 消息内容（可为空）
     * @param toolCalls 工具调用列表
     */
    public static ChatMessage ofAssistant(String content, List<ToolCall> toolCalls) {
        ChatMessage msg = new ChatMessage();
        msg.setRole(ROLE_ASSISTANT);
        msg.setContent(content);
        msg.setToolCalls(toolCalls);
        return msg;
    }

    /**
     * 创建工具结果消息。
     *
     * @param toolCallId 对应工具调用的 ID
     * @param output 工具执行结果（JSON 字符串）
     */
    public static ChatMessage ofTool(String toolCallId, String output) {
        ChatMessage msg = new ChatMessage();
        msg.setRole(ROLE_TOOL);
        msg.setContent(output);
        msg.setToolCallId(toolCallId);
        return msg;
    }

    // ==================== Inner Classes ====================

    /**
     * 工具调用对象。
     * <p>包含工具调用的 ID、类型、函数名称和参数。</p>
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ToolCall {

        /** 工具调用唯一标识符，如 {@code call_abc123} */
        private String id;

        /** 工具类型，固定为 {@code "function"} */
        private String type = "function";

        /** 函数调用详情 */
        private FunctionCall function;

        /**
         * 创建工具调用。
         *
         * @param id 工具调用 ID
         * @param name 函数名称
         * @param arguments 函数参数（JSON 字符串）
         */
        public static ToolCall of(String id, String name, String arguments) {
            FunctionCall fc = new FunctionCall();
            fc.setName(name);
            fc.setArguments(arguments);
            ToolCall tc = new ToolCall();
            tc.setId(id);
            tc.setType(OpenClawConstants.TOOL_TYPE_FUNCTION);
            tc.setFunction(fc);
            return tc;
        }
    }

    /**
     * 函数调用详情。
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class FunctionCall {

        /** 函数名称 */
        private String name;

        /**
         * 函数参数（JSON 字符串）。
         * <p>调用方需自行解析此 JSON 字符串为具体参数对象。</p>
         */
        private String arguments;
    }
}
