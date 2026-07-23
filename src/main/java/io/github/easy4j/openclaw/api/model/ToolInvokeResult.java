package io.github.easy4j.openclaw.api.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.github.easy4j.openclaw.api.OpenClawConstants;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Tools Invoke API 响应。
 * <p>
 * 对应 {@code POST /tools/invoke} 返回的 JSON。
 * </p>
 *
 * <h3>响应状态码</h3>
 * <ul>
 *   <li>{@code 200} - 成功：{@code { ok: true, result }}</li>
 *   <li>{@code 400} - 请求无效或工具输入错误：{@code { ok: false, error: { type, message } }}</li>
 *   <li>{@code 401} - 未授权</li>
 *   <li>{@code 404} - 工具不可用（未找到或未在允许列表中）</li>
 *   <li>{@code 405} - 方法不允许</li>
 *   <li>{@code 429} - 鉴权速率限制（{@code Retry-After} 已设置）</li>
 *   <li>{@code 500} - 意外工具执行错误</li>
 * </ul>
 *
 * @see <a href="https://docs.openclaw.ai/gateway/tools-invoke-http-api">Tools Invoke API</a>
 */
@Getter
@Setter
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ToolInvokeResult {

    /** 错误类型：未找到 */
    public static final String ERROR_TYPE_NOT_FOUND = "not_found";

    /** 错误类型：无效请求 */
    public static final String ERROR_TYPE_INVALID_REQUEST = "invalid_request_error";

    /** 错误类型：工具错误 */
    public static final String ERROR_TYPE_TOOL_ERROR = "tool_error";

    /**
     * 是否成功。
     * <p>{@code true} 表示工具调用成功，{@code false} 表示失败。</p>
     */
    private Boolean ok;

    /**
     * 工具执行结果（仅 {@code ok} 为 {@code true} 时存在）。
     * <p>结果格式取决于具体工具。</p>
     */
    private Object result;

    /**
     * 错误信息（仅 {@code ok} 为 {@code false} 时存在）。
     */
    private ErrorDetail error;

    /**
     * 错误详情。
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ErrorDetail {
        /**
         * 错误类型。
         * <ul>
         *   <li>{@code "invalid_request_error"} - 请求无效</li>
         *   <li>{@code "tool_error"} - 工具执行错误</li>
         * </ul>
         */
        private String type;

        /**
         * 错误消息（已清理，可安全展示）。
         */
        private String message;
    }
}
