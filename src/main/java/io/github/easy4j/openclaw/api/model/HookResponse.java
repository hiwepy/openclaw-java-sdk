package io.github.easy4j.openclaw.api.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

/**
 * 调用智能体后的统一结果（HTTP 或本地 CLI）。
 *
 * <p>Gateway {@code POST /hooks/agent} 成功响应结构：
 * <pre>{@code
 * { "ok": true, "runId": "..." }
 * }</pre>
 * 错误响应：
 * <pre>{@code
 * { "ok": false, "error": "..." }
 * }</pre>
 * </p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class HookResponse {

    /** 是否整体成功（由 {@code ok} 映射） */
    @JsonProperty("ok")
    private boolean success;

    /** HTTP 状态码；本地调用时为 -1 */
    private int httpStatus = -1;

    /** 解析出的 runId（对应响应 {@code runId} 字段） */
    private String runId;

    /** 原始响应或进程输出 */
    private String rawBody;

    /** 错误信息（来自响应的 {@code error} 字段） */
    private String error;

    /** 是否经本地 CLI 完成 */
    private boolean localInvocation;
}
