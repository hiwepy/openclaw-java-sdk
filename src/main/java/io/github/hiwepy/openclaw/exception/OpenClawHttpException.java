package io.github.hiwepy.openclaw.exception;

/**
 * 通过 Gateway HTTP 调用 OpenClaw 失败时抛出。
 */
public class OpenClawHttpException extends OpenClawException {

    private static final long serialVersionUID = 1L;

    private final int statusCode;
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

    /**
     * @return HTTP 状态码；未知时为 -1
     */
    public int getStatusCode() {
        return statusCode;
    }

    /**
     * @return 响应体原文，可能为 null
     */
    public String getResponseBody() {
        return responseBody;
    }
}
