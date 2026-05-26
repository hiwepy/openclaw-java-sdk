package io.github.hiwepy.openclaw.cli;

import io.github.hiwepy.openclaw.OpenClawClientConfig;
import io.github.hiwepy.openclaw.cli.support.SubprocessExecutionSupport;
import io.github.hiwepy.openclaw.util.OpenClawStrings;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecuteResultHandler;
import org.apache.commons.exec.ExecuteException;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * 使用 Apache Commons Exec 执行 {@code openclaw}，支持文档中的全局参数与子命令。
 */
@Slf4j
public class OpenClawCliExecutor {

    private final OpenClawClientConfig config;

    /**
     * @param config 客户端配置，不得为 null
     */
    public OpenClawCliExecutor(OpenClawClientConfig config) {
        this.config = Objects.requireNonNull(config, "config");
        SubprocessExecutionSupport.configureMaxConcurrentExecutions(config.getLocalMaxConcurrentExecutions());
    }

    public OpenClawClientConfig getConfig() {
        return config;
    }

    /**
     * 执行 CLI，始终返回 {@link OpenClawCliResult}（非 0 退出码不抛异常，由调用方处理）。
     */
    public OpenClawCliResult execute(OpenClawCliRequest request) {
        Objects.requireNonNull(request, "request");
        CommandLine cmd = toCommandLine(request);
        long timeoutMs = resolveTimeoutMillis(request);

        File workingDirectory = resolveWorkingDirectory();
        SubprocessExecutionSupport.ExecutionRequest execRequest =
                new SubprocessExecutionSupport.ExecutionRequest(cmd, workingDirectory, null, timeoutMs);

        try {
            SubprocessExecutionSupport.RunSession session = executeSubprocess(execRequest);
            String stdout = session.getStdout().toString(StandardCharsets.UTF_8);
            String stderr = session.getStderr().toString(StandardCharsets.UTF_8);

            if (session.timedOut()) {
                log.warn("openclaw timed out after {} ms: {}", timeoutMs, cmd);
                stderr = appendLine(stderr, "openclaw CLI timed out after " + timeoutMs + " ms");
                return new OpenClawCliResult(-1, stdout, stderr);
            }

            DefaultExecuteResultHandler handler = session.getHandler();
            Exception asyncFailure = handler.getException();
            if (asyncFailure instanceof ExecuteException ex) {
                return new OpenClawCliResult(ex.getExitValue(), stdout, stderr);
            }
            if (asyncFailure != null) {
                log.warn("openclaw async failure: {}", asyncFailure.getMessage());
                stderr = appendLine(stderr, asyncFailure.getMessage());
                return new OpenClawCliResult(-1, stdout, stderr);
            }

            try {
                int exit = handler.getExitValue();
                return new OpenClawCliResult(exit, stdout, stderr);
            } catch (IllegalStateException e) {
                log.warn("openclaw completed without exit code: {}", e.getMessage());
                stderr = appendLine(stderr, e.getMessage());
                return new OpenClawCliResult(-1, stdout, stderr);
            }
        } catch (Exception e) {
            log.warn("openclaw execution failed: {}", e.getMessage());
            return new OpenClawCliResult(-1, "", e.getMessage());
        }
    }

    /**
     * 启动子进程（包内可见，供单测注入失败场景）。
     */
    SubprocessExecutionSupport.RunSession executeSubprocess(SubprocessExecutionSupport.ExecutionRequest request)
            throws Exception {
        return SubprocessExecutionSupport.execute(request);
    }

    private static String appendLine(String base, String line) {
        if (OpenClawStrings.isNotBlank(base)) {
            return base + "\n" + line;
        }
        return line;
    }

    private File resolveWorkingDirectory() {
        String wdProperty = config.getLocalWorkingDirectory();
        if (OpenClawStrings.isBlank(wdProperty)) {
            return null;
        }
        File wd = new File(wdProperty.trim());
        if (!wd.isDirectory()) {
            throw new IllegalArgumentException(
                    "openclaw.local-working-directory is not an existing directory: " + wd.getAbsolutePath());
        }
        return wd;
    }

    private long resolveTimeoutMillis(OpenClawCliRequest request) {
        Integer t = request.getTimeoutSeconds();
        if (t != null && t > 0) {
            return t * 1000L;
        }
        return Math.max(1L, config.getLocalTimeoutSeconds()) * 1000L;
    }

    /**
     * 将请求转换为 {@link CommandLine}，便于测试与调试。
     */
    public CommandLine toCommandLine(OpenClawCliRequest request) {
        CommandLine cmd = new CommandLine(config.getLocalExecutable());
        if (request.isDev()) {
            cmd.addArgument("--dev");
        }
        if (request.getProfile() != null && !request.getProfile().isEmpty()) {
            cmd.addArgument("--profile");
            cmd.addArgument(request.getProfile());
        }
        if (request.getContainer() != null && !request.getContainer().isEmpty()) {
            cmd.addArgument("--container");
            cmd.addArgument(request.getContainer());
        }
        if (request.isNoColor()) {
            cmd.addArgument("--no-color");
        }
        for (String a : request.getArguments()) {
            cmd.addArgument(a);
        }
        return cmd;
    }
}
