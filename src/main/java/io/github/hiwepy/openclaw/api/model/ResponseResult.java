package io.github.hiwepy.openclaw.api.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;

/**
 * OpenResponses API 非流式响应 —— {@code POST /v1/responses} (stream: false)。
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public record ResponseResult(
        @JsonProperty("id") String id,
        @JsonProperty("object") String object,
        @JsonProperty("status") String status,
        @JsonProperty("model") String model,
        @JsonProperty("output") List<Map<String, Object>> output,
        @JsonProperty("usage") Usage usage) {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Usage(
            @JsonProperty("input_tokens") Integer inputTokens,
            @JsonProperty("output_tokens") Integer outputTokens,
            @JsonProperty("total_tokens") Integer totalTokens) {
    }
}
