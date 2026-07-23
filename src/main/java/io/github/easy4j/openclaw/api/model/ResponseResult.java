package io.github.easy4j.openclaw.api.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.Map;

/**
 * OpenResponses API 非流式响应。
 * <p>
 * 对应 {@code POST /v1/responses}（{@code stream: false}）返回的 JSON。
 * </p>
 *
 * @see <a href="https://docs.openclaw.ai/gateway/openresponses-http-api">OpenResponses API</a>
 */
@Getter
@Setter
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ResponseResult {

    /** 响应唯一标识。 */
    private String id;

    /** 对象类型，固定为 {@code "response"}。 */
    private String object;

    /**
     * 响应状态。
     * <ul>
     *   <li>{@code "completed"} - 正常完成</li>
     *   <li>{@code "failed"} - 失败</li>
     *   <li>{@code "in_progress"} - 进行中（流式时）</li>
     * </ul>
     */
    private String status;

    /** 使用的 agent 目标标识。 */
    private String model;

    /**
     * 输出内容列表。
     * <p>每个输出项是消息对象，包含 {@code type}、{@code role}、{@code content} 等字段。</p>
     */
    private List<Map<String, Object>> output;

    /**
     * Token 使用统计。
     */
    private Usage usage;

    /**
     * Token 使用统计。
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Usage {
        @JsonProperty("input_tokens")
        private Integer inputTokens;
        @JsonProperty("output_tokens")
        private Integer outputTokens;
        @JsonProperty("total_tokens")
        private Integer totalTokens;
    }
}
