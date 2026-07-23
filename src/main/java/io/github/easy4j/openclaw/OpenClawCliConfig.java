package io.github.easy4j.openclaw;

import lombok.Data;

/**
 * OpenClaw 本地 CLI 客户端配置。
 * <p>
 * 涵盖本地 {@code openclaw} 可执行文件路径、超时、并发、工作目录等所有 CLI 运行时设置。
 * </p>
 *
 * @author wandl
 * @since 1.0.0
 */
@Data
public class OpenClawCliConfig {

    /**
     * 本地可执行文件名或绝对路径
     */
    private String executable = "openclaw";

    /**
     * 本地 agent 命令超时（秒）
     */
    private int timeout = 300;

    /**
     * 本地 CLI 子进程工作目录；为空时使用 JVM 当前目录。
     */
    private String workingDirectory;

    /**
     * 本机 CLI 子进程最大并发数；小于等于 0 时使用 CPU 核心数与 2 的较大值。
     */
    private int maxConcurrentExecutions = 0;

    /**
     * 探测本地运行时是否可用的超时（秒）
     */
    private int probeTimeoutSeconds = 5;

}
