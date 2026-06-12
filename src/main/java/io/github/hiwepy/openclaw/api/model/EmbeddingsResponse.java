package io.github.hiwepy.openclaw.api.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * OpenAI Embeddings API 响应 —— {@code POST /v1/embeddings}。
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public record EmbeddingsResponse(
        @JsonProperty("object") String object,
        @JsonProperty("data") List<EmbeddingData> data,
        @JsonProperty("model") String model,
        @JsonProperty("usage") Usage usage) {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record EmbeddingData(
            @JsonProperty("object") String object,
            @JsonProperty("embedding") List<Double> embedding,
            @JsonProperty("index") Integer index) {
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Usage(
            @JsonProperty("prompt_tokens") Integer promptTokens,
            @JsonProperty("total_tokens") Integer totalTokens) {
    }
}
