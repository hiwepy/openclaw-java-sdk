package io.github.hiwepy.openclaw.exception;

import io.github.hiwepy.openclaw.cli.availability.OpenClawCliAvailabilityReport;
import lombok.Getter;

/**
 * 应用启动阶段 OpenClaw CLI 不可用且配置为 fail-fast 时抛出。
 *
 * @author wandl
 * @since 1.0.0
 */
@Getter
public class OpenClawCliStartupException extends RuntimeException {

    private final OpenClawCliAvailabilityReport availabilityReport;

    /**
     * @param message 诊断说明
     * @param report    探测报告
     */
    public OpenClawCliStartupException(String message, OpenClawCliAvailabilityReport report) {
        super(message);
        this.availabilityReport = report;
    }
}
