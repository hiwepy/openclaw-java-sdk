package io.github.hiwepy.openclaw.cli.availability;

import io.github.hiwepy.openclaw.OpenClawClientConfig;
import io.github.hiwepy.openclaw.cli.OpenClawCli;
import io.github.hiwepy.openclaw.cli.OpenClawCliExecutor;
import io.github.hiwepy.openclaw.cli.OpenClawCliResult;
import io.github.hiwepy.openclaw.util.OpenClawStrings;
import java.io.File;
import java.util.Objects;
import java.util.Optional;

/**
 * 探测本机 {@code openclaw} 是否已安装且可执行 {@code openclaw --version}。
 *
 * @author wandl
 * @since 1.0.0
 */
public class OpenClawCliAvailabilityChecker {

    /**
     * 使用与运行时一致的配置探测 CLI。
     *
     * @param config 客户端配置，不得为 null
     * @return 探测报告
     */
    public OpenClawCliAvailabilityReport check(OpenClawClientConfig config) {
        Objects.requireNonNull(config, "config");
        String configured = config.getLocalExecutable();
        if (OpenClawStrings.isBlank(configured)) {
            return unavailable(
                    OpenClawCliAvailabilityStatus.EXECUTABLE_NOT_CONFIGURED,
                    configured,
                    null,
                    "openclaw.local.executable is blank",
                    null);
        }
        String trimmed = configured.trim();
        Optional<String> resolved = resolveExecutablePath(trimmed);
        if (!resolved.isPresent()) {
            if (looksLikePath(trimmed)) {
                File file = new File(trimmed);
                if (!file.exists()) {
                    return unavailable(
                            OpenClawCliAvailabilityStatus.EXECUTABLE_NOT_FOUND,
                            trimmed,
                            null,
                            "executable file does not exist: " + file.getAbsolutePath(),
                            null);
                }
                return unavailable(
                        OpenClawCliAvailabilityStatus.EXECUTABLE_NOT_EXECUTABLE,
                        trimmed,
                        file.getAbsolutePath(),
                        "executable exists but is not executable: " + file.getAbsolutePath(),
                        null);
            }
            return unavailable(
                    OpenClawCliAvailabilityStatus.EXECUTABLE_NOT_FOUND,
                    trimmed,
                    null,
                    "executable not found on PATH: " + trimmed,
                    null);
        }

        OpenClawClientConfig probeConfig = copyForProbe(config);
        OpenClawCliExecutor probeExecutor = new OpenClawCliExecutor(probeConfig);
        OpenClawCliResult result = new OpenClawCli(probeExecutor).version();
        if (result.isSuccess()) {
            return OpenClawCliAvailabilityReport.builder()
                    .status(OpenClawCliAvailabilityStatus.AVAILABLE)
                    .available(true)
                    .configuredExecutable(trimmed)
                    .resolvedExecutablePath(resolved.get())
                    .message("openclaw --version succeeded")
                    .probeResult(result)
                    .build();
        }
        if (result.getExitCode() == -1 && containsTimeoutHint(result)) {
            return unavailable(
                    OpenClawCliAvailabilityStatus.TIMEOUT,
                    trimmed,
                    resolved.get(),
                    "openclaw --version timed out",
                    result);
        }
        if (result.getExitCode() == -1 && isSpawnFailure(result)) {
            return unavailable(
                    OpenClawCliAvailabilityStatus.SPAWN_FAILED,
                    trimmed,
                    resolved.get(),
                    firstLine(result.getStderr()),
                    result);
        }
        return unavailable(
                OpenClawCliAvailabilityStatus.NON_ZERO_EXIT,
                trimmed,
                resolved.get(),
                "openclaw --version exitCode=" + result.getExitCode(),
                result);
    }

    /**
     * 解析可执行文件：绝对/相对路径直接检查；否则在 {@code PATH} 中查找。
     */
    static Optional<String> resolveExecutablePath(String executable) {
        if (OpenClawStrings.isBlank(executable)) {
            return Optional.empty();
        }
        String trimmed = executable.trim();
        File direct = new File(trimmed);
        if (looksLikePath(trimmed)) {
            if (direct.isFile() && direct.canExecute()) {
                return Optional.of(direct.getAbsolutePath());
            }
            return Optional.empty();
        }
        String pathEnv = System.getenv("PATH");
        if (pathEnv == null || pathEnv.isEmpty()) {
            return Optional.empty();
        }
        for (String dir : pathEnv.split(File.pathSeparator)) {
            if (OpenClawStrings.isBlank(dir)) {
                continue;
            }
            File candidate = new File(dir.trim(), trimmed);
            if (candidate.isFile() && candidate.canExecute()) {
                return Optional.of(candidate.getAbsolutePath());
            }
        }
        return Optional.empty();
    }

    private static OpenClawClientConfig copyForProbe(OpenClawClientConfig source) {
        OpenClawClientConfig copy = new OpenClawClientConfig();
        copy.setLocalExecutable(source.getLocalExecutable());
        copy.setLocalWorkingDirectory(source.getLocalWorkingDirectory());
        copy.setLocalMaxConcurrentExecutions(source.getLocalMaxConcurrentExecutions());
        int probeSec = source.getLocalProbeTimeoutSeconds();
        if (probeSec <= 0) {
            probeSec = 5;
        }
        copy.setLocalTimeoutSeconds(probeSec);
        copy.setLocalProbeTimeoutSeconds(probeSec);
        return copy;
    }

    private static boolean looksLikePath(String executable) {
        return executable.contains("/") || executable.contains("\\") || new File(executable).isAbsolute();
    }

    private static boolean containsTimeoutHint(OpenClawCliResult result) {
        String combined = result.getStderr() + result.getStdout();
        return combined.toLowerCase().contains("timed out");
    }

    private static boolean isSpawnFailure(OpenClawCliResult result) {
        String stderr = result.getStderr();
        return stderr != null
                && (stderr.contains("could not be started")
                        || stderr.contains("No such file")
                        || stderr.contains("spawn"));
    }

    private static String firstLine(String text) {
        if (OpenClawStrings.isBlank(text)) {
            return "openclaw execution failed";
        }
        int idx = text.indexOf('\n');
        return idx >= 0 ? text.substring(0, idx) : text;
    }

    private static OpenClawCliAvailabilityReport unavailable(
            OpenClawCliAvailabilityStatus status,
            String configured,
            String resolved,
            String message,
            OpenClawCliResult partial) {
        return OpenClawCliAvailabilityReport.builder()
                .status(status)
                .available(false)
                .configuredExecutable(configured)
                .resolvedExecutablePath(resolved)
                .message(message)
                .probeResult(partial)
                .build();
    }
}
