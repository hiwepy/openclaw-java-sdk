package com.github.hiwepy.openclaw;

import com.github.hiwepy.openclaw.cli.OpenClawCli;
import com.github.hiwepy.openclaw.cli.OpenClawCliExecutor;
import lombok.Generated;

import java.util.Map;
import java.util.Objects;

/**
 * 门面：HTTP Gateway 调用与通用本地 CLI（{@link #cli()}）相互独立，无自动降级。
 * <p>
 * 远程调用请使用 {@link #agent(InvokeAgentRequest)}；任意顶层命令请使用 {@link #cli()}。
 * 若创建了默认 {@link OpenClawGatewayHttpClient}，在不再使用时可调用 {@link #close()} 释放 HTTP 资源。
 * </p>
 */
public class OpenClawClient implements AutoCloseable {

    private final OpenClawClientConfig config;
    private final OpenClawGatewayHttpClient gatewayHttpClient;
    private final OpenClawCli cli;

    public OpenClawClient(OpenClawClientConfig config) {
        this.config = Objects.requireNonNull(config, "config");
        OpenClawCliExecutor exec = new OpenClawCliExecutor(config);
        this.gatewayHttpClient = new OpenClawGatewayHttpClient(config);
        this.cli = new OpenClawCli(exec);
    }

    /**
     * 完整依赖注入（用于测试或自定义 {@link OpenClawGatewayHttpClient}）。
     */
    public OpenClawClient(OpenClawClientConfig config,
                          OpenClawGatewayHttpClient gatewayHttpClient,
                          OpenClawCli cli) {
        this.config = Objects.requireNonNull(config, "config");
        this.gatewayHttpClient = Objects.requireNonNull(gatewayHttpClient, "gatewayHttpClient");
        this.cli = Objects.requireNonNull(cli, "cli");
    }

    /**
     * 官方 CLI 顶层命令封装（{@code gateway}、{@code agent}、{@code health} 等）。
     */
    public OpenClawCli cli() {
        return cli;
    }

    /**
     * 仅通过 OpenClaw Gateway 的 HTTP Webhook（{@code POST /hooks/agent}）触发智能体。
     *
     * @param request 请求体
     * @return Gateway 响应解析结果
     */
    public InvokeAgentResult agent(InvokeAgentRequest request) {
        return gatewayHttpClient.postHooksAgent(request);
    }

    /**
     * 兼容旧门面命名：等价于 {@link #agent(InvokeAgentRequest)}。
     *
     * @param request 请求体
     * @return Gateway 响应解析结果
     * @deprecated 请改用 {@link #agent(InvokeAgentRequest)}
     */
    @Deprecated
    public InvokeAgentResult invokeViaGateway(InvokeAgentRequest request) {
        return agent(request);
    }

    /**
     * 调用 Gateway webhook {@code POST /hooks/wake} 注入主会话系统事件。
     *
     * @param text 事件文本（必填）
     * @param mode 触发模式：{@code now} 或 {@code next-heartbeat}；空值时默认 {@code now}
     * @return 网关原始响应体
     */
    public String wake(String text, String mode) {
        return gatewayHttpClient.postHooksWake(text, mode);
    }

    /**
     * 兼容旧门面命名：等价于 {@link #wake(String, String)}。
     *
     * @param text 事件文本（必填）
     * @param mode 触发模式：{@code now} 或 {@code next-heartbeat}
     * @return 网关原始响应体
     * @deprecated 请改用 {@link #wake(String, String)}
     */
    @Deprecated
    public String wakeViaWebhook(String text, String mode) {
        return wake(text, mode);
    }

    /**
     * 调用映射 webhook {@code POST /hooks/<name>}。
     *
     * @param hookName 映射名（如 {@code gmail}）
     * @param payload 请求体对象（可为 null）
     * @return 网关原始响应体
     */
    public String hook(String hookName, Map<String, Object> payload) {
        return gatewayHttpClient.postMappedHook(hookName, payload);
    }

    /**
     * 兼容旧门面命名：等价于 {@link #hook(String, Map)}。
     *
     * @param hookName 映射名（如 {@code gmail}）
     * @param payload 请求体对象（可为 null）
     * @return 网关原始响应体
     * @deprecated 请改用 {@link #hook(String, Map)}
     */
    @Deprecated
    public String invokeMappedWebhook(String hookName, Map<String, Object> payload) {
        return hook(hookName, payload);
    }

    /**
     * 释放本客户端持有的外部资源。
     * <p>当前仅关闭 {@link OpenClawGatewayHttpClient} 的 HTTP 连接资源。</p>
     */
    @Override
    public void close() {
        gatewayHttpClient.close();
    }
}
