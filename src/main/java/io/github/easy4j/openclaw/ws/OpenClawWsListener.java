package io.github.easy4j.openclaw.ws;

import io.github.easy4j.openclaw.ws.protocol.EventFrame;
import io.github.easy4j.openclaw.ws.protocol.HelloOk;
import io.github.easy4j.openclaw.ws.protocol.ResponseFrame;

/**
 * Gateway WebSocket 事件监听器。
 * <p>实现此接口以接收 Gateway 推送的事件和状态变更。</p>
 *
 * @see <a href="https://docs.openclaw.ai/gateway/protocol">Gateway Protocol</a>
 */
public interface OpenClawWsListener {

    /**
     * WS 连接已建立并完成 {@code connect} 握手。
     *
     * @param helloOk Gateway 返回的握手信息
     */
    default void onConnected(HelloOk helloOk) {}

    /**
     * WS 连接已关闭。
     *
     * @param code    关闭码
     * @param reason  关闭原因
     * @param remote  是否由远端关闭
     */
    default void onDisconnected(int code, String reason, boolean remote) {}

    /**
     * WS 连接发生错误。
     *
     * @param ex 异常
     */
    default void onError(Exception ex) {}

    /**
     * 收到 Gateway 推送事件帧。
     * <p>常见事件：{@code chat}（智能体回复）、{@code agent}（智能体状态变更）、
     * {@code tick}（心跳）、{@code shutdown}（Gateway 关闭）等。</p>
     *
     * @param frame 事件帧
     */
    default void onEvent(EventFrame frame) {}

    /**
     * 收到 Gateway RPC 响应帧（未被内部 RPC 匹配消费的）。
     *
     * @param frame 响应帧
     */
    default void onResponse(ResponseFrame frame) {}
}
