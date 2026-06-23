package io.github.hiwepy.openclaw.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

/**
 * OpenAI/OpenClaw 响应格式控制选项。
 *
 * <h3>有效类型</h3>
 * <table>
 *   <tr><th>type</th><th>说明</th><th>json_schema?</th></tr>
 *   <tr><td>{@code "text"}</td><td>默认文本输出</td><td>否</td></tr>
 *   <tr><td>{@code "json_object"}</td><td>强制返回合法 JSON</td><td>否</td></tr>
 *   <tr><td>{@code "json_schema"}</td><td>结构化输出（限定 JSON Schema）</td><td><b>是</b></td></tr>
 * </table>
 *
 * <h3>用法示例</h3>
 * <pre>{@code
 * // 简单 JSON 输出（最常用）
 * ResponseFormat.jsonObject()
 *
 * // 结构化输出（JSON Schema）
 * ResponseFormat.builder()
 *     .type(ResponseFormatType.JSON_SCHEMA)
 *     .jsonSchema(ResponseFormatJsonSchema.builder()
 *         .name("article")
 *         .strict(true)
 *         .schema(Map.of("type", "object", "properties", ...))
 *         .build())
 *     .build();
 * }</pre>
 *
 * @see <a href="https://docs.openclaw.ai/gateway/openai-http-api">OpenAI Chat Completions</a>
 * @see <a href="https://platform.openai.com/docs/guides/structured-outputs">Structured Outputs</a>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ResponseFormat {

    /**
     * 响应格式类型。
     */
    @JsonProperty("type")
    private ResponseFormatType type;

    /**
     * 仅当 {@code type = "json_schema"} 时填充。
     * <p>包含结构化输出的 name、schema、strict 等字段。</p>
     */
    @JsonProperty("json_schema")
    private ResponseFormatJsonSchema jsonSchema;

    // ---- 便捷工厂方法 ----

    /**
     * 强制 JSON 输出（最常用）。
     */
    public static ResponseFormat jsonObject() {
        return ResponseFormat.builder()
                .type(ResponseFormatType.JSON_OBJECT)
                .build();
    }

    /**
     * 默认文本输出。
     */
    public static ResponseFormat text() {
        return ResponseFormat.builder()
                .type(ResponseFormatType.TEXT)
                .build();
    }
}
