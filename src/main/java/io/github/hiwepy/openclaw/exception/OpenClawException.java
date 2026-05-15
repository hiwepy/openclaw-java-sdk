package io.github.hiwepy.openclaw.exception;

/**
 * 根异常：OpenClaw SDK 调用失败时抛出。
 */
public class OpenClawException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public OpenClawException(String message) {
        super(message);
    }

    public OpenClawException(String message, Throwable cause) {
        super(message, cause);
    }
}
