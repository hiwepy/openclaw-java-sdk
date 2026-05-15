package io.github.hiwepy.openclaw;

/**
 * 调用智能体后的统一结果（HTTP 或本地 CLI）。
 */
public class InvokeAgentResult {

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

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public int getHttpStatus() {
        return httpStatus;
    }

    public void setHttpStatus(int httpStatus) {
        this.httpStatus = httpStatus;
    }

    public String getRunId() {
        return runId;
    }

    public void setRunId(String runId) {
        this.runId = runId;
    }

    public String getRawBody() {
        return rawBody;
    }

    public void setRawBody(String rawBody) {
        this.rawBody = rawBody;
    }

    public boolean isLocalInvocation() {
        return localInvocation;
    }

    public void setLocalInvocation(boolean localInvocation) {
        this.localInvocation = localInvocation;
    }
}
