package io.github.hiwepy.openclaw.cli;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;

/**
 * {@code openclaw} 进程执行结果（不解析业务语义；{@code --json} 输出在 {@link #stdout}）。
 */
@Getter
@Slf4j
public final class OpenClawCliResult {

    private final int exitCode;
    private final String stdout;
    private final String stderr;

    public OpenClawCliResult(int exitCode, String stdout, String stderr) {
        this.exitCode = exitCode;
        this.stdout = stdout != null ? stdout : "";
        this.stderr = stderr != null ? stderr : "";
    }

    /**
     * @return 进程是否以 0 退出
     */
    public boolean isSuccess() {
        return exitCode == 0;
    }

    @Override
    public String toString() {
        return "OpenClawCliResult{exitCode=" + exitCode + ", stdout.len=" + stdout.length()
                + ", stderr.len=" + stderr.length() + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof OpenClawCliResult)) {
            return false;
        }
        OpenClawCliResult that = (OpenClawCliResult) o;
        return exitCode == that.exitCode && Objects.equals(stdout, that.stdout) && Objects.equals(stderr, that.stderr);
    }

    @Override
    public int hashCode() {
        return Objects.hash(exitCode, stdout, stderr);
    }
}
