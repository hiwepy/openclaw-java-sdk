package io.github.hiwepy.openclaw.cli.availability;

import io.github.hiwepy.openclaw.cli.OpenClawCliResult;
import lombok.Builder;
import lombok.Getter;

/**
 * OpenClaw CLI 启动/就绪探测结果。
 *
 * @author wandl
 * @since 1.0.0
 */
@Getter
@Builder
public class OpenClawCliAvailabilityReport {

    private final OpenClawCliAvailabilityStatus status;
    private final boolean available;
    private final String configuredExecutable;
    private final String resolvedExecutablePath;
    private final String message;
    private final OpenClawCliResult probeResult;

    /**
     * @return 是否可安全调用本地 {@code openclaw}
     */
    public boolean isAvailable() {
        return available;
    }

    /**
     * 构造面向日志/异常的诊断文本。
     *
     * @return 说明字符串
     */
    public String toDiagnosticMessage() {
        StringBuilder sb = new StringBuilder();
        sb.append("OpenClaw CLI ");
        sb.append(available ? "ready" : "unavailable");
        sb.append(" [").append(status).append(']');
        if (configuredExecutable != null) {
            sb.append(" executable=").append(configuredExecutable);
        }
        if (resolvedExecutablePath != null) {
            sb.append(" resolved=").append(resolvedExecutablePath);
        }
        if (message != null && !message.isEmpty()) {
            sb.append(" — ").append(message);
        }
        return sb.toString();
    }
}
