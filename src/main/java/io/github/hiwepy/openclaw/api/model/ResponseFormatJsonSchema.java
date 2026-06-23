package io.github.hiwepy.openclaw.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.Map;

/**
 * JSON Schema 结构化输出配置（仅当 {@code type = "json_schema"} 时使用）。
 *
 * <h3>字段说明</h3>
 * <ul>
 *   <li>{@code name} — Schema 名称（必填）</li>
 *   <li>{@code schema} — JSON Schema 定义（必填）</li>
 *   <li>{@code strict} — 是否启用严格模式（可选，默认 false）</li>
 *   <li>{@code description} — Schema 描述（可选）</li>
 * </ul>
 *
 * <h3>用法示例</h3>
 * <pre>{@code
 * ResponseFormatJsonSchema.builder()
 *     .name("article")
 *     .strict(true)
 *     .schema(Map.of("type", "object",
 *         "properties", Map.of("title", Map.of("type", "string"))))
 *     .build();
 * }</pre>
 *
 * @see <a href="https://platform.openai.com/docs/guides/structured-outputs">OpenAI Structured Outputs</a>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ResponseFormatJsonSchema {

    /**
     * Schema 名称（必填）。
     */
    @JsonProperty("name")
    private String name;

    /**
     * JSON Schema 定义（必填）。
     * <p>格式：{@code { "type": "object", "properties": { ... }, "required": [...] }}</p>
     */
    @JsonProperty("schema")
    private Map<String, Object> schema;

    /**
     * 是否启用严格模式（可选）。
     * <p>启用后模型必须严格遵循 Schema，否则拒绝输出。默认 false。</p>
     */
    @JsonProperty("strict")
    private Boolean strict;

    /**
     * Schema 描述（可选）。
     */
    @JsonProperty("description")
    private String description;
}
