package io.github.hiwepy.openclaw.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;

/**
 * OpenResponses API 请求体 —— {@code POST /v1/responses}。
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ResponseRequest(
        @JsonProperty("model") String model,
        @JsonProperty("input") Object input,
        @JsonProperty("instructions") String instructions,
        @JsonProperty("tools") List<Map<String, Object>> tools,
        @JsonProperty("tool_choice") Object toolChoice,
        @JsonProperty("stream") Boolean stream,
        @JsonProperty("max_output_tokens") Integer maxOutputTokens,
        @JsonProperty("temperature") Double temperature,
        @JsonProperty("top_p") Double topP,
        @JsonProperty("user") String user,
        @JsonProperty("previous_response_id") String previousResponseId) {

    /**
     * Return a copy with {@code stream} set to {@code true}.
     */
    public ResponseRequest withStream() {
        return new ResponseRequest(model, input, instructions, tools, toolChoice, true,
                maxOutputTokens, temperature, topP, user, previousResponseId);
    }
}
