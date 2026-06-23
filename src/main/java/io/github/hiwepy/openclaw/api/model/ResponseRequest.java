package io.github.hiwepy.openclaw.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.hiwepy.openclaw.api.OpenClawConstants;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.Map;

/**
 * OpenResponses API 请求体。
 * <p>
 * 对应 {@code POST /v1/responses} 的请求 JSON。
 * </p>
 *
 * <h3>字段语义</h3>
 * <ul>
 *   <li>{@code agent} - Agent 目标路由（如 {@code "openclaw/default"}）
 *   <li>{@code model} - 后端 LLM 模型（如 {@code "gpt-4o"}）
 * </ul>
 *
 * <h3>用法示例</h3>
 * <pre>{@code
 * // 方式1：字符串输入
 * ResponseRequest request = ResponseRequest.builder()
 *     .agent("openclaw/default")
 *     .input("What is the weather?")
 *     .build();
 *
 * // 方式2：Item 数组输入
 * ResponseRequest request = ResponseRequest.builder()
 *     .agent("openclaw/default")
 *     .input(List.of(
 *         InputItem.message().role("user").content("What is the weather?").build(),
 *         InputItem.imageSource("url", "https://example.com/photo.jpg").build()
 *     ))
 *     .build();
 *
 * // 方式3：工具调用结果
 * ResponseRequest request = ResponseRequest.builder()
 *     .agent("openclaw/default")
 *     .input(List.of(
 *         InputItem.message().role("assistant").content(null).build(),
 *         InputItem.functionCallOutput().callId("call_abc").output("{\"temperature\":\"25C\"}").build()
 *     ))
 *     .build();
 * }</pre>
 *
 * @see <a href="https://docs.openclaw.ai/gateway/openresponses-http-api">OpenResponses API</a>
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ResponseRequest {

    /**
     * Agent 目标标识。
     */
    private String agent;

    /**
     * 后端 LLM 模型标识。
     */
    private String model;

    /**
     * 输入内容。
     * <p>可以是单个字符串或 Item 对象数组。</p>
     */
    private Object input;

    /**
     * 系统指令（合并到系统提示）。
     */
    private String instructions;

    /**
     * 客户端工具定义。
     */
    private List<Map<String, Object>> tools;

    /**
     * 工具选择策略。
     * <p>支持：{@code "auto"}、{@code "none"}、{@code "required"}、
     * 或 {@code { "type": "function", "name": "..." }}。</p>
     */
    private Object toolChoice;

    /**
     * 是否启用 SSE 流式响应。
     */
    private Boolean stream;

    /**
     * 最大输出 token 数。
     */
    private Integer maxOutputTokens;

    /**
     * 采样温度。
     */
    private Double temperature;

    /**
     * nucleus 采样参数。
     */
    private Double topP;

    /**
     * 用户标识。
     */
    private String user;

    /**
     * 先前响应 ID。
     */
    private String previousResponseId;

    // ==================== Inner Classes ====================

    /**
     * Response API 的 Input Item 类型。
     * <p>
     * 支持：message、function_call_output、input_image、input_file
     * </p>
     *
     * <h3>用法示例</h3>
     * <pre>{@code
     * // 图片输入（URL）
     * InputItem.imageSource("url", "https://example.com/photo.jpg")
     *
     * // 图片输入（base64）
     * InputItem.imageSource("base64", "data:image/png;base64,...")
     *
     * // 文件输入（URL，带 MIME 类型）
     * InputItem.fileSource("url", "https://example.com/doc.pdf", "application/pdf")
     *
     * // 文件输入（base64）
     * InputItem.fileSource("base64", "data:application/pdf;base64,...", "application/pdf")
     * }</pre>
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class InputItem {

        /** Item 类型：message、function_call_output、input_image、input_file */
        private String type;

        // message 类型字段
        private String role;
        private String content;

        // function_call_output 类型字段
        @JsonProperty("call_id")
        private String callId;
        private String output;

        // input_image / input_file 共享 source 字段
        private Source source;

        // ==================== Factory Methods ====================

        /**
         * 创建消息类型 Item。
         */
        public static InputItemBuilder message() {
            return InputItem.builder().type(OpenClawConstants.INPUT_TYPE_MESSAGE);
        }

        /**
         * 创建函数调用结果类型 Item。
         */
        public static InputItemBuilder functionCallOutput() {
            return InputItem.builder().type(OpenClawConstants.INPUT_TYPE_FUNCTION_CALL_OUTPUT);
        }

        /**
         * 创建图片输入类型 Item。
         *
         * @param sourceType 来源类型：{@code "url"} 或 {@code "base64"}
         * @param value     URL 地址或 base64 数据
         */
        public static InputItemBuilder imageSource(String sourceType, String value) {
            return InputItem.builder()
                    .type(OpenClawConstants.INPUT_TYPE_IMAGE)
                    .source(Source.builder()
                            .type(sourceType)
                            .url(value)
                            .build());
        }

        /**
         * 创建图片输入类型 Item（URL）。
         */
        public static InputItemBuilder imageUrl(String url) {
            return imageSource("url", url);
        }

        /**
         * 创建图片输入类型 Item（base64）。
         */
        public static InputItemBuilder imageBase64(String base64Data) {
            return imageSource("base64", base64Data);
        }

        /**
         * 创建文件输入类型 Item。
         *
         * @param sourceType 来源类型：{@code "url"} 或 {@code "base64"}
         * @param value     URL 地址或 base64 数据
         * @param mediaType MIME 类型（如 {@code "text/plain"}、{@code "application/pdf"}）
         */
        public static InputItemBuilder fileSource(String sourceType, String value, String mediaType) {
            return InputItem.builder()
                    .type(OpenClawConstants.INPUT_TYPE_FILE)
                    .source(Source.builder()
                            .type(sourceType)
                            .url(value)
                            .mediaType(mediaType)
                            .build());
        }

        /**
         * 创建文件输入类型 Item（URL）。
         */
        public static InputItemBuilder fileUrl(String url) {
            return fileUrl(url, null);
        }

        /**
         * 创建文件输入类型 Item（URL，带 MIME 类型）。
         */
        public static InputItemBuilder fileUrl(String url, String mediaType) {
            return fileSource("url", url, mediaType);
        }

        /**
         * 创建文件输入类型 Item（base64）。
         */
        public static InputItemBuilder fileBase64(String base64Data) {
            return fileBase64(base64Data, null);
        }

        /**
         * 创建文件输入类型 Item（base64，带 MIME 类型）。
         */
        public static InputItemBuilder fileBase64(String base64Data, String mediaType) {
            return fileSource("base64", base64Data, mediaType);
        }

        // ==================== Source Inner Class ====================

        /**
         * 图片/文件来源。
         * <p>
         * 格式：{@code { type: "url" | "base64", url?: string, media_type?: string, filename?: string, detail?: string }}
         * </p>
         */
        @Getter
        @Setter
        @NoArgsConstructor
        @AllArgsConstructor
        @Builder
        public static class Source {
            /** 来源类型：{@code "url"} 或 {@code "base64"} */
            private String type;

            /** URL 地址或 base64 数据 */
            private String url;

            /** MIME 类型 */
            @JsonProperty("media_type")
            private String mediaType;

            /** 文件名 */
            private String filename;

            /** detail 级别：{@code "low"}、{@code "high"}、{@code "auto"} */
            private String detail;
        }
    }
}
