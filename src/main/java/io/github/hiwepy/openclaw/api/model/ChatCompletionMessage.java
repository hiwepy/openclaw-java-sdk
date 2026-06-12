package io.github.hiwepy.openclaw.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.List;

/**
 * OpenAI Chat Completions API 消息对象。
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ChatCompletionMessage(
        @JsonProperty("role") String role,
        @JsonProperty("content") String content,
        @JsonProperty("tool_calls") List<ToolCall> toolCalls,
        @JsonProperty("tool_call_id") String toolCallId,
        @JsonProperty("tool_name") String toolName,
        @JsonProperty("thinking") String thinking) {

    public ChatCompletionMessage(String role, String content) {
        this(role, content, null, null, null, null);
    }

    public ChatCompletionMessage(Role role, String content) {
        this(role.jsonValue, content, null, null, null, null);
    }

    /** Message roles as defined by OpenAI Chat Completions API. */
    public enum Role {
        SYSTEM("system"),
        USER("user"),
        ASSISTANT("assistant"),
        TOOL("tool");

        @JsonValue
        private final String jsonValue;
        Role(String jsonValue) { this.jsonValue = jsonValue; }
    }

    /**
     * 工具调用对象。
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record ToolCall(
            @JsonProperty("id") String id,
            @JsonProperty("type") String type,
            @JsonProperty("function") FunctionCall function,
            @JsonProperty("index") Integer index) {

        public ToolCall(String id, String type, FunctionCall function) {
            this(id, type, function, null);
        }
    }

    /**
     * 函数调用详情。
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record FunctionCall(
            @JsonProperty("name") String name,
            @JsonProperty("arguments") String arguments) {
    }
}
