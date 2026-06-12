package io.github.hiwepy.openclaw.api.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * OpenAI Chat Completions API 流式响应块。
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public record ChatCompletionChunk(
        @JsonProperty("id") String id,
        @JsonProperty("object") String object,
        @JsonProperty("created") Long created,
        @JsonProperty("model") String model,
        @JsonProperty("choices") List<DeltaChoice> choices,
        @JsonProperty("usage") Usage usage) {

    public ChatCompletionChunk() {
        this(null, null, null, null, null, null);
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record DeltaChoice(
            @JsonProperty("index") Integer index,
            @JsonProperty("delta") DeltaMessage delta,
            @JsonProperty("finish_reason") String finishReason) {

        public DeltaChoice() {
            this(null, null, null);
        }
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record DeltaMessage(
            @JsonProperty("role") String role,
            @JsonProperty("content") String content,
            @JsonProperty("tool_calls") List<ChatCompletionMessage.ToolCall> toolCalls) {

        public DeltaMessage() {
            this(null, null, null);
        }
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Usage(
            @JsonProperty("completion_tokens") Integer completionTokens,
            @JsonProperty("prompt_tokens") Integer promptTokens,
            @JsonProperty("total_tokens") Integer totalTokens) {
    }
}
