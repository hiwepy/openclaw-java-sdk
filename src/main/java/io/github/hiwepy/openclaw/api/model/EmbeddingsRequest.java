package io.github.hiwepy.openclaw.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * OpenAI Embeddings API 请求体 —— {@code POST /v1/embeddings}。
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record EmbeddingsRequest(
        @com.fasterxml.jackson.annotation.JsonProperty("model") String model,
        @com.fasterxml.jackson.annotation.JsonProperty("input") Object input) {
}
