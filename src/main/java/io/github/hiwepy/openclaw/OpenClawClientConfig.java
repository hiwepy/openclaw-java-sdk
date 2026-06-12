package io.github.hiwepy.openclaw;

import io.github.hiwepy.openclaw.api.OpenClawGatewayHttpClient;
import io.github.hiwepy.openclaw.util.OpenClawStrings;
import lombok.Data;

/**
 * OpenClaw 客户端配置（纯 POJO，可与 Spring {@code @ConfigurationProperties} 映射）。
 * <p>
 * <b>凭证语义（与 OpenClaw Gateway 文档对齐）：</b>
 * </p>
 * <ul>
 *     <li>{@link #hooksToken} / {@link #apiKey}：<b>仅</b>用于 {@code POST /hooks/*}（Webhooks）鉴权；
 *         文档允许 {@code Authorization: Bearer &lt;hooks.token&gt;} <b>或</b>
 *         {@code x-openclaw-token: &lt;token&gt;}（二选一，由 {@link #hooksUseXOpenclawTokenHeader} 选择），
 *         对应 {@code hooks.token}，<b>不得</b>与 {@code gateway.auth.token} 混用。</li>
 *     <li>{@link #gatewayAuthToken} / {@link #gatewayAuthPassword}：对应控制面凭证（如
 *         {@code gateway.auth.token}、{@code OPENCLAW_GATEWAY_TOKEN} 或密码模式），供 CLI /
 *         WebSocket 控制面 / OpenAI 兼容 API / Tools Invoke 使用。</li>
 * </ul>
 *
 * @see <a href="https://docs.openclaw.ai/gateway/protocol">Gateway Protocol</a>
 * @see <a href="https://docs.openclaw.ai/gateway/openai-http-api#authentication">OpenAI HTTP API Authentication</a>
 * @see <a href="https://docs.openclaw.ai/gateway/cli-backends">CLI Backends</a>
 */
@Data
public class OpenClawClientConfig {

    /**
     * Gateway HTTP 根地址（Webhooks 与部分 HTTP 面共用主机），例如 {@code http://localhost:18789}。
     */
    private String gatewayBaseUrl = "http://localhost:18789";

    /**
     * Webhook 鉴权令牌，对应 Gateway {@code hooks.token}；
     * 作为 {@code /hooks/*} 请求的 Bearer 时的<b>首选</b>值。
     */
    private String hooksToken;

    /**
     * 当 {@link #hooksToken} 未配置时，作为 Webhook Bearer 的兜底值。
     * <p>
     * 历史与 Spring 属性名 {@code api-key} 对齐；语义上仍是 <b>Hooks 入口</b>密钥，
     * 不是 {@code gateway.auth.token}。
     * </p>
     *
     * @deprecated 请优先配置 {@link #hooksToken}；保留本字段仅为兼容既有 YAML/属性名。
     */
    @Deprecated
    private String apiKey;

    /**
     * 网关控制面共享令牌（如 {@code gateway.auth.token} 或环境变量 {@code OPENCLAW_GATEWAY_TOKEN}）。
     * <p>
     * 外接官方 App SDK 时通常用于 WebSocket {@code connect} 与 RPC；本仓库尚未实现该传输层时，
     * 可配合 {@link io.github.hiwepy.openclaw.cli.OpenClawCli} 在请求参数中显式传入 {@code --token}，
     * 或依赖进程环境中已配置的 Gateway 凭证。
     * </p>
     */
    private String gatewayAuthToken;

    /**
     * 网关控制面密码（{@code gateway.auth.password} 模式时）；与 {@link #gatewayAuthToken} 二选一语境，
     * 不由 {@link OpenClawGatewayHttpClient} 使用。
     */
    private String gatewayAuthPassword;

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

    /**
     * 本地 CLI 子进程工作目录；为空时使用 JVM 当前目录。
     */
    private String localWorkingDirectory;

    /**
     * 本机 CLI 子进程最大并发数；小于等于 0 时使用 CPU 核心数与 2 的较大值。
     */
    private int localMaxConcurrentExecutions = 0;

    /** 探测本地运行时是否可用的超时（秒） */
    private int localProbeTimeoutSeconds = 5;

    /**
     * 为 {@code true} 时使用 {@code x-openclaw-token} 传递 hook 令牌；为 {@code false}（默认）时使用
     * {@code Authorization: Bearer …}。与官方 Gateway Webhook 文档一致，两种头不要同时发送。
     */
    private boolean hooksUseXOpenclawTokenHeader = false;

    /**
     * Gateway HTTP Webhooks 基础路径，对应 {@code hooks.path}，默认 {@code /hooks}。
     */
    private String hooksPath = "/hooks";

    /**
     * 解析用于 {@code /hooks/*} HTTP Webhook 请求的 Bearer 令牌。
     *
     * @return {@link #hooksToken} 非空则用之，否则 {@link #apiKey}，均为空则空字符串
     */
    public String resolveHooksBearerToken() {
        if (OpenClawStrings.isNotBlank(hooksToken)) {
            return hooksToken.trim();
        }
        return OpenClawStrings.nullToEmpty(apiKey);
    }

    /**
     * 解析用于 Gateway <b>控制面</b> HTTP API（{@code /v1/*}、{@code /tools/*}）的 Bearer 令牌。
     * <p>
     * 优先级：{@link #gatewayAuthToken} → {@link #gatewayAuthPassword} → {@link #resolveHooksBearerToken()}。
     * </p>
     * <p>
     * 与 Webhook 鉴权不同：OpenAI 兼容 API 和 Tools Invoke 使用 Gateway 控制面凭证
     * （{@code gateway.auth.token} 或 {@code gateway.auth.password}），
     * 而非 {@code hooks.token}。
     * </p>
     *
     * @return 控制面 Bearer 令牌，均为空则空字符串
     * @see <a href="https://docs.openclaw.ai/gateway/openai-http-api#authentication">OpenAI HTTP API Authentication</a>
     */
    public String resolveGatewayBearerToken() {
        if (OpenClawStrings.isNotBlank(gatewayAuthToken)) {
            return gatewayAuthToken.trim();
        }
        if (OpenClawStrings.isNotBlank(gatewayAuthPassword)) {
            return gatewayAuthPassword.trim();
        }
        return resolveHooksBearerToken();
    }

    /**
     * 与 {@link #resolveHooksBearerToken()} 相同，保留以兼容旧代码。
     *
     * @return Webhook Bearer 令牌
     * @deprecated 请使用 {@link #resolveHooksBearerToken()}，语义更明确。
     */
    @Deprecated
    public String resolveBearerToken() {
        return resolveHooksBearerToken();
    }

    /**
     * 规范化 {@link #hooksPath}，保证以 {@code /} 开头且不以 {@code /} 结尾。
     */
    public String resolveHooksPath() {
        String raw = OpenClawStrings.defaultIfBlank(hooksPath, "/hooks");
        if (!raw.startsWith("/")) {
            raw = "/" + raw;
        }
        while (raw.endsWith("/") && raw.length() > 1) {
            raw = raw.substring(0, raw.length() - 1);
        }
        if ("/".equals(raw)) {
            throw new IllegalArgumentException(
                    "hooks.path must be a dedicated subpath (e.g. /hooks); root path '/' is rejected by Gateway");
        }
        return raw;
    }
}
