package com.github.hiwepy.openclaw;

import lombok.Data;

/**
 * OpenClaw 客户端配置（纯 POJO，可与 Spring {@code @ConfigurationProperties} 映射）。
 */
@Data
public class OpenClawClientConfig {

    /** Gateway 根地址，例如 {@code http://localhost:18789} */
    private String gatewayBaseUrl = "http://localhost:18789";
    /** Webhook 鉴权，优先于 {@link #apiKey} */
    private String hooksToken;
    /** 未设置 hooksToken 时用作 Bearer */
    private String apiKey;
    /** 是否校验 HTTPS 证书；为 false 时关闭校验（仅建议开发环境） */
    private boolean verifySsl = true;
    /** 连接超时（毫秒） */
    private int connectTimeoutMillis = 15_000;
    /** 读取超时（毫秒） */
    private int readTimeoutMillis = 120_000;

    /** 本地可执行文件名或绝对路径 */
    private String localExecutable = "openclaw";
    /** 本地 agent 命令超时（秒） */
    private int localTimeoutSeconds = 300;
    /** 探测本地运行时是否可用的超时（秒） */
    private int localProbeTimeoutSeconds = 5;

    /**
     * @return 用于 Bearer 的 token：hooksToken 优先，否则 apiKey
     */
    public String resolveBearerToken() {
        if (hooksToken != null && !hooksToken.isEmpty()) {
            return hooksToken;
        }
        return apiKey != null ? apiKey : "";
    }
}
