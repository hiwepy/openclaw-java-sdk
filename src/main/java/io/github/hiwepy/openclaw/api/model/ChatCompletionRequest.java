package io.github.hiwepy.openclaw.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;

/**
 * OpenAI Chat Completions API 请求体 —— {@code POST /v1/chat/completions}。
 *
 * <h3>会话行为</h3>
 * 默认每次请求无状态。若请求包含 {@code user} 字符串，
 * Gateway 从中派生稳定 session key，使后续调用共享同一 agent 会话。
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ChatCompletionRequest(
        @JsonProperty("model") String model,
        @JsonProperty("messages") List<ChatCompletionMessage> messages,
        @JsonProperty("stream") Boolean stream,
        @JsonProperty("stream_options") Map<String, Object> streamOptions,
        @JsonProperty("tools") List<Map<String, Object>> tools,
        @JsonProperty("tool_choice") Object toolChoice,
        @JsonProperty("user") String user,
        @JsonProperty("max_completion_tokens") Integer maxCompletionTokens,
        @JsonProperty("max_tokens") Integer maxTokens,
        @JsonProperty("temperature") Double temperature,
        @JsonProperty("top_p") Double topP,
        @JsonProperty("frequency_penalty") Double frequencyPenalty,
        @JsonProperty("presence_penalty") Double presencePenalty,
        @JsonProperty("seed") Integer seed,
        @JsonProperty("stop") Object stop) {

    /**
     * Return a copy with {@code stream} set to {@code true}.
     */
    public ChatCompletionRequest withStream() {
        return new ChatCompletionRequest(model, messages, true, streamOptions, tools, toolChoice,
                user, maxCompletionTokens, maxTokens, temperature, topP,
                frequencyPenalty, presencePenalty, seed, stop);
    }
}
