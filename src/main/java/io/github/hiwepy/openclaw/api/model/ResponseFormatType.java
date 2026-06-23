package io.github.hiwepy.openclaw.api.model;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * {@link ResponseFormat} 的 type 枚举。
 *
 * <ul>
 *   <li>{@code text} — 默认文本输出</li>
 *   <li>{@code json_object} — 强制返回合法 JSON</li>
 *   <li>{@code json_schema} — 结构化输出（需配合 {@code json_schema} 字段）</li>
 * </ul>
 */
public enum ResponseFormatType {

    TEXT("text"),
    JSON_OBJECT("json_object"),
    JSON_SCHEMA("json_schema");

    private final String value;

    ResponseFormatType(String value) {
        this.value = value;
    }

    @JsonValue
    public String value() {
        return value;
    }
}
