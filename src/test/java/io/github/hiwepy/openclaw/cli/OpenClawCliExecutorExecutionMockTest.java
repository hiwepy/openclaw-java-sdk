package io.github.hiwepy.openclaw.cli;

import io.github.hiwepy.openclaw.OpenClawClientConfig;
import io.github.hiwepy.openclaw.cli.support.SubprocessExecutionSupport;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermission;
import java.util.EnumSet;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link OpenClawCliExecutor} 子进程超时与并发辅助行为（mock 脚本，不依赖真实 openclaw）。
 */
class OpenClawCliExecutorExecutionMockTest {

    private Path scriptPath;
    private OpenClawCliExecutor executor;

    @BeforeEach
    void setUp() throws Exception {
        Path root = Files.createTempDirectory("openclaw-mock-cli-");
        scriptPath = root.resolve("openclaw");
        Files.writeString(
                scriptPath,
                """
                #!/usr/bin/env bash
                cmd="${1:-}"
                case "$cmd" in
                  __sleep_forever)
                    sleep 3600
                    ;;
                  __exit_zero)
                    exit 0
                    ;;
                  *)
                    echo "unknown"
                    exit 2
                    ;;
                esac
                """,
                StandardCharsets.UTF_8);
        makeExecutable(scriptPath);

        OpenClawClientConfig cfg = new OpenClawClientConfig();
        cfg.setLocalExecutable(scriptPath.toAbsolutePath().toString());
        cfg.setLocalTimeoutSeconds(1);
        cfg.setLocalMaxConcurrentExecutions(2);
        executor = new OpenClawCliExecutor(cfg);
    }

    @Test
    void sleepForeverShouldReturnTimedOutResult() {
        OpenClawCliRequest req = OpenClawCliRequest.builder().arguments("__sleep_forever").build();
        OpenClawCliResult result = executor.execute(req);
        assertEquals(-1, result.getExitCode());
        assertFalse(result.isSuccess());
        assertTrue(result.getStderr().contains("timed out"));
    }

    @Test
    void successCommandShouldReturnZeroExit() {
        OpenClawCliRequest req = OpenClawCliRequest.builder().arguments("__exit_zero").build();
        OpenClawCliResult result = executor.execute(req);
        assertEquals(0, result.getExitCode());
        assertTrue(result.isSuccess());
    }

    @Test
    void spawnFailureShouldReturnNegativeExit() {
        OpenClawClientConfig cfg = new OpenClawClientConfig();
        cfg.setLocalExecutable(scriptPath.toAbsolutePath().toString());
        cfg.setLocalTimeoutSeconds(5);
        OpenClawCliExecutor failing = new OpenClawCliExecutor(cfg) {
            @Override
            SubprocessExecutionSupport.RunSession executeSubprocess(
                    SubprocessExecutionSupport.ExecutionRequest request) throws Exception {
                throw new IOException("spawn-fail");
            }
        };
        OpenClawCliResult result = failing.execute(
                OpenClawCliRequest.builder().arguments("__exit_zero").build());
        assertEquals(-1, result.getExitCode());
        assertTrue(result.getStderr().contains("spawn-fail"));
    }

    private static void makeExecutable(Path script) throws IOException {
        Set<PosixFilePermission> perms = EnumSet.of(
                PosixFilePermission.OWNER_READ,
                PosixFilePermission.OWNER_WRITE,
                PosixFilePermission.OWNER_EXECUTE);
        Files.setPosixFilePermissions(script, perms);
    }
}
