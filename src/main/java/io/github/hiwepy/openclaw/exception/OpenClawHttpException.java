package io.github.hiwepy.openclaw.exception;

import lombok.Getter;

/**
 * 通过 Gateway HTTP 调用 OpenClaw 失败时抛出。
 */
@Getter
public class OpenClawHttpException extends OpenClawException {

    private static final long serialVersionUID = 1L;

    /** HTTP 状态码；未知时为 -1 */
    private final int statusCode;

    /** 响应体原文，可能为 null */
    private final String responseBody;

    public OpenClawHttpException(String message, int statusCode, String responseBody) {
        super(message);
        this.statusCode = statusCode;
        this.responseBody = responseBody;
    }

    public OpenClawHttpException(String message, Throwable cause) {
        super(message, cause);
        this.statusCode = -1;
        this.responseBody = null;
    }
}
