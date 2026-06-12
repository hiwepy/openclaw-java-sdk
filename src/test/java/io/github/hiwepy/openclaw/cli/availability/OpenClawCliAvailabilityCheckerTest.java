package io.github.hiwepy.openclaw.cli.availability;

import io.github.hiwepy.openclaw.api.OpenClawClientConfig;
import io.github.hiwepy.openclaw.cli.support.MockOpenClawCli;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link OpenClawCliAvailabilityChecker} 单元测试。
 */
class OpenClawCliAvailabilityCheckerTest {

    @Test
    void checkShouldSucceedWithMockExecutable() throws Exception {
        MockOpenClawCli mock = MockOpenClawCli.install();
        OpenClawCliAvailabilityReport report = new OpenClawCliAvailabilityChecker().check(mock.newConfig());

        assertTrue(report.isAvailable());
        assertEquals(OpenClawCliAvailabilityStatus.AVAILABLE, report.getStatus());
    }

    @Test
    void checkShouldFailWhenExecutableMissing() {
        OpenClawClientConfig config = new OpenClawClientConfig();
        config.setLocalExecutable("/nonexistent/openclaw-startup-test");
        config.setLocalProbeTimeoutSeconds(3);

        OpenClawCliAvailabilityReport report = new OpenClawCliAvailabilityChecker().check(config);

        assertFalse(report.isAvailable());
        assertEquals(OpenClawCliAvailabilityStatus.EXECUTABLE_NOT_FOUND, report.getStatus());
    }
}
