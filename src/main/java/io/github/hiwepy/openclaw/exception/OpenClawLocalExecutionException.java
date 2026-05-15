package io.github.hiwepy.openclaw.exception;

import io.github.hiwepy.openclaw.exception.OpenClawException;

/**
 * 本地命令行执行 OpenClaw 失败时抛出。
 */
public class OpenClawLocalExecutionException extends OpenClawException {

    private static final long serialVersionUID = 1L;

    private final int exitCode;

    public OpenClawLocalExecutionException(String message, int exitCode) {
        super(message);
        this.exitCode = exitCode;
    }

    public OpenClawLocalExecutionException(String message, Throwable cause) {
        super(message, cause);
        this.exitCode = Integer.MIN_VALUE;
    }

    /**
     * @return 进程退出码；未执行完成时为 {@link Integer#MIN_VALUE}
     */
    public int getExitCode() {
        return exitCode;
    }
}
