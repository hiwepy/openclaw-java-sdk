package io.github.hiwepy.openclaw.api.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * OpenAI Embeddings API 响应。
 * <p>
 * 对应 {@code POST /v1/embeddings} 返回的 JSON。
 * </p>
 *
 * @see <a href="https://docs.openclaw.ai/gateway/openai-http-api">OpenAI Chat Completions</a>
 */
@Getter
@Setter
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class EmbeddingsResponse {

    /** 对象类型，固定为 {@code "list"}。 */
    private String object;

    /** 嵌入向量列表。 */
    private List<EmbeddingData> data;

    /** 使用的模型标识。 */
    private String model;

    /** Token 使用统计。 */
    private Usage usage;

    /**
     * 单个嵌入向量。
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class EmbeddingData {
        /** 对象类型，固定为 {@code "embedding"}。 */
        private String object;
        /** 嵌入向量（浮点数数组）。 */
        private List<Double> embedding;
        /** 输入文本在数组中的索引。 */
        private Integer index;
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
        @JsonProperty("prompt_tokens")
        private Integer promptTokens;
        @JsonProperty("total_tokens")
        private Integer totalTokens;
    }
}
