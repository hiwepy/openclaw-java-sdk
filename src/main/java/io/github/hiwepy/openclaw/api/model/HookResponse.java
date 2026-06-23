package io.github.hiwepy.openclaw.api.model;

import lombok.Getter;
import lombok.Setter;

/**
 * 调用智能体后的统一结果（HTTP 或本地 CLI）。
 */
@Getter
@Setter
public class HookResponse {

    /** 是否整体成功 */
    private boolean success;

    /** HTTP 状态码；本地调用时为 -1 */
    private int httpStatus = -1;

    /** 解析出的 runId（若响应含 JSON 字段） */
    private String runId;

    /** 原始响应或进程输出 */
    private String rawBody;

    /** 是否经本地 CLI 完成 */
    private boolean localInvocation;
}
