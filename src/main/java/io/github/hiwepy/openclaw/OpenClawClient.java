package io.github.hiwepy.openclaw;

import io.github.hiwepy.openclaw.cli.OpenClawCli;
import io.github.hiwepy.openclaw.cli.OpenClawCliExecutor;
import lombok.Generated;

import java.util.Map;
import java.util.Objects;

/**
 * 门面：<b>HTTP Webhooks</b>（{@code /hooks/*}）与通用本地 CLI（{@link #cli()}）相互独立，无自动降级。
 * <p>
 * {@link #agent(InvokeAgentRequest)} / {@link #agentOneShot} / {@link #agentOneShotForPeer} /
 * {@link #agentWithStableSession} / {@link #wake} / {@link #hook} 仅走 Gateway Webhook HTTP 路径，
 * 使用配置中的 {@link OpenClawClientConfig#getHooksToken()} 与兼容字段 {@link OpenClawClientConfig#getApiKey()}；
 * 不要把 {@code gateway.auth.token}（{@link OpenClawClientConfig#getGatewayAuthToken()}）当作 Hook Bearer。
 * </p>
 * <p>
 * 任意顶层命令请使用 {@link #cli()}。
 * 若创建了默认 {@link OpenClawGatewayHttpClient}，在不再使用时可调用 {@link #close()} 释放 HTTP 资源。
 * </p>
 * <p>
 * 官方外部 App SDK 以 WebSocket 为主；完整控制面与流式事件不在本 HTTP 封装范围内。
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
     * 一次性 Hook（无 peer）：不在 JSON 中发送 {@code sessionKey}，由 Gateway 生成 {@code hook:&lt;uuid&gt;}。
     *
     * @param request 请求体；若已设置 {@code sessionKey} 会被忽略
     */
    public InvokeAgentResult agentOneShot(InvokeAgentRequest request) {
        InvokeAgentRequest copy = copyRequest(Objects.requireNonNull(request, "request"));
        copy.setSessionKey(null);
        return agent(copy);
    }

    /**
     * 一次性 Hook（有 peer）：{@code sessionKey = hook:<peerId>:<uuid>}，每次调用默认新 UUID。
     *
     * @param peerId  业务 peer（userId 等）
     * @param request 请求体；若未设置 {@code sessionKey} 则自动填充
     */
    public InvokeAgentResult agentOneShotForPeer(String peerId, InvokeAgentRequest request) {
        Objects.requireNonNull(peerId, "peerId");
        InvokeAgentRequest copy = copyRequest(Objects.requireNonNull(request, "request"));
        if (isBlank(copy.getSessionKey())) {
            copy.setSessionKey(OpenClawSessionKeys.forEphemeralPeer(peerId));
        }
        return agent(copy);
    }

    /**
     * 一次性 Hook（有 peer + 指定 correlationId）：{@code hook:<peerId>:<correlationId>}。
     *
     * @param peerId          业务 peer
     * @param correlationId   本次唯一 id
     * @param request         请求体
     */
    public InvokeAgentResult agentOneShotForPeer(String peerId, String correlationId, InvokeAgentRequest request) {
        Objects.requireNonNull(peerId, "peerId");
        Objects.requireNonNull(correlationId, "correlationId");
        InvokeAgentRequest copy = copyRequest(Objects.requireNonNull(request, "request"));
        if (isBlank(copy.getSessionKey())) {
            copy.setSessionKey(OpenClawSessionKeys.forEphemeralPeer(peerId, correlationId));
        }
        return agent(copy);
    }

    /**
     * 固定多轮 Hook：{@code sessionKey = hook:<agentId>:<peerId>}；未设置 {@code agentId} 时写入 {@code agentId}。
     *
     * @param agentId 路由 agent
     * @param peerId  业务 peer
     * @param request 请求体
     */
    public InvokeAgentResult agentWithStableSession(String agentId, String peerId, InvokeAgentRequest request) {
        Objects.requireNonNull(agentId, "agentId");
        Objects.requireNonNull(peerId, "peerId");
        InvokeAgentRequest copy = copyRequest(Objects.requireNonNull(request, "request"));
        copy.setSessionKey(OpenClawSessionKeys.forStableSession(agentId, peerId));
        if (isBlank(copy.getAgentId())) {
            copy.setAgentId(agentId.trim());
        }
        return agent(copy);
    }

    /**
     * 固定多轮 Hook：从 {@code request.agentId} 与 {@code peerId} 构造 {@code hook:<agentId>:<peerId>}。
     *
     * @param peerId  业务 peer
     * @param request 请求体；{@code agentId} 必填
     */
    public InvokeAgentResult agentWithStableSession(String peerId, InvokeAgentRequest request) {
        Objects.requireNonNull(peerId, "peerId");
        InvokeAgentRequest copy = copyRequest(Objects.requireNonNull(request, "request"));
        if (isBlank(copy.getAgentId())) {
            throw new IllegalArgumentException("agentId is required on request");
        }
        return agentWithStableSession(copy.getAgentId(), peerId, copy);
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

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    /**
     * 浅拷贝 {@link InvokeAgentRequest}，避免 Hook 便捷方法修改调用方实例。
     */
    static InvokeAgentRequest copyRequest(InvokeAgentRequest source) {
        InvokeAgentRequest copy = new InvokeAgentRequest();
        copy.setMessage(source.getMessage());
        copy.setAgentId(source.getAgentId());
        copy.setName(source.getName());
        copy.setWakeMode(source.getWakeMode());
        copy.setTimeoutSeconds(source.getTimeoutSeconds());
        copy.setSessionKey(source.getSessionKey());
        copy.setDeliver(source.getDeliver());
        copy.setChannel(source.getChannel());
        copy.setTo(source.getTo());
        copy.setModel(source.getModel());
        copy.setThinking(source.getThinking());
        return copy;
    }
}
