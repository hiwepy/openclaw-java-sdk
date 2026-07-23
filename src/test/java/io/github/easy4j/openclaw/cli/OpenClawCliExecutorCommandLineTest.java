package io.github.easy4j.openclaw.cli;

import io.github.easy4j.openclaw.OpenClawCliConfig;
import org.apache.commons.exec.CommandLine;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

/**
 * {@link OpenClawCliExecutor#toCommandLine(OpenClawCliRequest)} 参数序列单元测试（不执行真实 openclaw）。
 */
class OpenClawCliExecutorCommandLineTest {

    @Test
    void toCommandLine_globalFlagsAndSubcommands() {
        OpenClawCliConfig cfg = new OpenClawCliConfig();
        cfg.setExecutable("openclaw");
        OpenClawCliExecutor exec = new OpenClawCliExecutor(cfg);
        OpenClawCliRequest req = OpenClawCliRequest.builder()
                .dev(true)
                .profile("p1")
                .container("c1")
                .noColor(true)
                .arguments("gateway", "health")
                .build();
        CommandLine cmd = exec.toCommandLine(req);
        assertArrayEquals(
                new String[]{"openclaw", "--dev", "--profile", "p1", "--container", "c1", "--no-color", "gateway", "health"},
                cmd.toStrings());
    }

    @Test
    void version_usesSingleFlagArgument() {
        OpenClawCliConfig cfg = new OpenClawCliConfig();
        cfg.setExecutable("openclaw");
        OpenClawCliExecutor exec = new OpenClawCliExecutor(cfg);
        OpenClawCliRequest req = OpenClawCliRequest.builder().arguments("--version").build();
        assertArrayEquals(new String[]{"openclaw", "--version"}, exec.toCommandLine(req).toStrings());
    }
}
