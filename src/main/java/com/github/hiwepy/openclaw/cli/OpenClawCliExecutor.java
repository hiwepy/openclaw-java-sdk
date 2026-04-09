package com.github.hiwepy.openclaw.cli;

import com.github.hiwepy.openclaw.OpenClawClientConfig;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteWatchdog;
import org.apache.commons.exec.PumpStreamHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Objects;

/**
 * 使用 Apache Commons Exec 执行 {@code openclaw}，支持文档中的全局参数与子命令。
 */
@Slf4j
public class OpenClawCliExecutor {

    private final OpenClawClientConfig config;

    public OpenClawCliExecutor(OpenClawClientConfig config) {
        this.config = Objects.requireNonNull(config, "config");
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
        int timeoutSec = resolveTimeoutSeconds(request);

        DefaultExecutor executor = DefaultExecutor.builder().get();
        ExecuteWatchdog watchdog =
                ExecuteWatchdog.builder().setTimeout(Duration.ofSeconds(Math.max(1, timeoutSec))).get();
        executor.setWatchdog(watchdog);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ByteArrayOutputStream err = new ByteArrayOutputStream();
        executor.setStreamHandler(new PumpStreamHandler(out, err));

        try {
            int exit = executor.execute(cmd);
            String stdout = out.toString(StandardCharsets.UTF_8);
            String stderr = err.toString(StandardCharsets.UTF_8);
            return new OpenClawCliResult(exit, stdout, stderr);
        } catch (Exception e) {
            log.warn("openclaw execution failed: {}", e.getMessage());
            String stderr = err.toString(StandardCharsets.UTF_8);
            if (!stderr.isEmpty()) {
                stderr = stderr + "\n";
            }
            stderr = stderr + e.getMessage();
            return new OpenClawCliResult(-1, out.toString(StandardCharsets.UTF_8), stderr);
        }
    }

    private int resolveTimeoutSeconds(OpenClawCliRequest request) {
        Integer t = request.getTimeoutSeconds();
        if (t != null && t > 0) {
            return t;
        }
        return Math.max(1, config.getLocalTimeoutSeconds());
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
