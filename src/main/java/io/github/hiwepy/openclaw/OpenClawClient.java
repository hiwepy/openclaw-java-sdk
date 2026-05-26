package io.github.hiwepy.openclaw;

import io.github.hiwepy.openclaw.cli.OpenClawCli;
import io.github.hiwepy.openclaw.cli.OpenClawCliExecutor;
import io.github.hiwepy.openclaw.ws.ChatStreamHandler;
import io.github.hiwepy.openclaw.ws.OpenClawGatewayWsClient;
import io.github.hiwepy.openclaw.ws.OpenClawWsListener;
import io.github.hiwepy.openclaw.ws.protocol.ChatSendParams;
import io.github.hiwepy.openclaw.ws.protocol.HelloOk;
import io.github.hiwepy.openclaw.ws.protocol.SessionsSendParams;
import io.github.hiwepy.openclaw.ws.protocol.result.SessionsSendResult;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

/**
 * 门面：<b>HTTP Webhooks</b>（{@code /hooks/*}）+ <b>WebSocket 控制面</b> + 通用本地 CLI（{@link #cli()}）。
 * <p>
 * 三条通信通道相互独立：
 * </p>
 * <ul>
 *     <li><b>Webhook HTTP</b>：{@link #agent} / {@link #wake} / {@link #hook} — 无状态触发</li>
 *     <li><b>WebSocket</b>：{@link #ws()} — 双向实时通信，流式对话，完整 RPC</li>
 *     <li><b>CLI</b>：{@link #cli()} — 本地 {@code openclaw} 命令封装</li>
 * </ul>
 *
 * @see OpenClawGatewayWsClient
 */
public class OpenClawClient implements AutoCloseable {

    private final OpenClawClientConfig config;
    private final OpenClawGatewayHttpClient gatewayHttpClient;
    private final OpenClawCli cli;
    private final OpenClawGatewayWsClient wsClient;

    /**
     * 标准构造（自动创建 HTTP、CLI、WS 客户端）。
     */
    public OpenClawClient(OpenClawClientConfig config) {
        this.config = Objects.requireNonNull(config, "config");
        OpenClawCliExecutor exec = new OpenClawCliExecutor(config);
        this.gatewayHttpClient = new OpenClawGatewayHttpClient(config);
        this.cli = new OpenClawCli(exec);
        this.wsClient = new OpenClawGatewayWsClient(config);
    }

    /**
     * 完整依赖注入（用于测试或自定义组件）。
     */
    public OpenClawClient(OpenClawClientConfig config,
                          OpenClawGatewayHttpClient gatewayHttpClient,
                          OpenClawCli cli,
                          OpenClawGatewayWsClient wsClient) {
        this.config = Objects.requireNonNull(config, "config");
        this.gatewayHttpClient = Objects.requireNonNull(gatewayHttpClient, "gatewayHttpClient");
        this.cli = Objects.requireNonNull(cli, "cli");
        this.wsClient = Objects.requireNonNull(wsClient, "wsClient");
    }

    // ============================================================
    // HTTP Webhook
    // ============================================================

    /**
     * 通过 Gateway HTTP Webhook（{@code POST /hooks/agent}）触发智能体。
     */
    public InvokeAgentResult agent(InvokeAgentRequest request) {
        return gatewayHttpClient.postHooksAgent(request);
    }


    /**
     * 一次性调用（无会话保持）。
     */
    public InvokeAgentResult agentOneShot(InvokeAgentRequest request) {
        return agent(request);
    }

    /**
     * 带 peer 的一次性调用。
     */
    public InvokeAgentResult agentOneShotForPeer(String peerId, InvokeAgentRequest request) {
        return agent(request);
    }

    /**
     * 带 peer 和 correlationId 的一次性调用。
     */
    public InvokeAgentResult agentOneShotForPeer(String peerId, String correlationId, InvokeAgentRequest request) {
        return agent(request);
    }

    /**
     * 稳定会话调用。
     */
    public InvokeAgentResult agentWithStableSession(String agentId, String peerId, InvokeAgentRequest request) {
        return agent(request);
    }

    /**
     * @deprecated 请改用 {@link #agent(InvokeAgentRequest)}
     */
    @Deprecated
    public InvokeAgentResult invokeViaGateway(InvokeAgentRequest request) {
        return agent(request);
    }

    /**
     * 调用 {@code POST /hooks/wake} 注入系统事件。
     */
    public String wake(String text, String mode) {
        return gatewayHttpClient.postHooksWake(text, mode);
    }

    /**
     * @deprecated 请改用 {@link #wake(String, String)}
     */
    @Deprecated
    public String wakeViaWebhook(String text, String mode) {
        return wake(text, mode);
    }

    /**
     * 调用映射 webhook {@code POST /hooks/<name>}。
     */
    public String hook(String hookName, Map<String, Object> payload) {
        return gatewayHttpClient.postMappedHook(hookName, payload);
    }

    /**
     * @deprecated 请改用 {@link #hook(String, Map)}
     */
    @Deprecated
    public String invokeMappedWebhook(String hookName, Map<String, Object> payload) {
        return hook(hookName, payload);
    }

    // ============================================================
    // WebSocket 控制面
    // ============================================================

    /**
     * 获取 WebSocket 客户端实例。
     * <p>通过 WS 客户端可实现：流式对话（{@code chat.send}）、会话管理、cron 管理、配置管理等。</p>
     *
     * <pre>{@code
     * OpenClawGatewayWsClient ws = client.ws();
     * ws.addListener(new OpenClawWsListener() { ... });
     * ws.connectBlocking();
     * ws.chatSend(ChatSendParams.builder().message("你好").build(), handler);
     * }</pre>
     *
     * @return WebSocket 客户端
     */
    public OpenClawGatewayWsClient ws() {
        return wsClient;
    }

    /**
     * 快捷方式：连接 WS 并阻塞等待握手完成。
     *
     * @return 握手结果
     */
    public HelloOk wsConnect() {
        try {
            return wsClient.connectHandshake();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("WS connect interrupted", e);
        }
    }

    /**
     * 快捷方式：异步连接 WS。
     */
    public CompletableFuture<HelloOk> wsConnectAsync() {
        return wsClient.connectHandshakeAsync();
    }

    /**
     * 快捷方式：通过 WS 发送流式聊天。
     *
     * @param message 消息文本
     * @param handler 流式处理器
     */
    public void wsChatSend(String message, ChatStreamHandler handler) {
        wsClient.chatSend(ChatSendParams.builder().message(message).build(), handler);
    }

    /**
     * 快捷方式：通过 WS 发送流式聊天（指定会话）。
     *
     * @param sessionKey 会话键
     * @param message    消息文本
     * @param handler    流式处理器
     */
    public void wsChatSend(String sessionKey, String message, ChatStreamHandler handler) {
        wsClient.chatSend(
                ChatSendParams.builder().sessionKey(sessionKey).message(message).build(),
                handler);
    }

    /**
     * 快捷方式：通过 WS 向指定会话发消息（非流式 RPC）。
     */
    public SessionsSendResult wsSessionsSend(String sessionKey, String message) {
        return wsClient.sessionsSend(
                SessionsSendParams.builder().key(sessionKey).message(message).build());
    }

    /**
     * 快捷方式：添加 WS 事件监听器。
     */
    public OpenClawClient addWsListener(OpenClawWsListener listener) {
        wsClient.addListener(listener);
        return this;
    }

    // ============================================================
    // CLI
    // ============================================================

    /**
     * 官方 CLI 顶层命令封装。
     */
    public OpenClawCli cli() {
        return cli;
    }

    // ============================================================
    // 生命周期
    // ============================================================

    @Override
    public void close() {
        gatewayHttpClient.close();
        try {
            wsClient.close();
        } catch (Exception ignored) {
        }
    }
}
