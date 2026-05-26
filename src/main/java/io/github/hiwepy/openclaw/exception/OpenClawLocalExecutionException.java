package io.github.hiwepy.openclaw.exception;

import lombok.Getter;

/**
 * 本地命令行执行 OpenClaw 失败时抛出。
 */
@Getter
public class OpenClawLocalExecutionException extends OpenClawException {

    private static final long serialVersionUID = 1L;

    /** 进程退出码；未执行完成时为 {@link Integer#MIN_VALUE} */
    private final int exitCode;

    public OpenClawLocalExecutionException(String message, int exitCode) {
        super(message);
        this.exitCode = exitCode;
    }

    public OpenClawLocalExecutionException(String message, Throwable cause) {
        super(message, cause);
        this.exitCode = Integer.MIN_VALUE;
    }
}
