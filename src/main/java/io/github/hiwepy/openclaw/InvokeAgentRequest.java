package io.github.hiwepy.openclaw;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * 调用 OpenClaw Gateway {@code POST /hooks/agent} 的请求体，与
 * <a href="https://docs.openclaw.ai/automation/cron-jobs#webhooks">Cron Webhooks 文档</a>一致。
 * <p>
 * 可选字段未显式设置时不会出现在 JSON 中，以便使用 Gateway 默认值（与文档 curl 示例一致）。
 * {@code sessionKey} 仅在网关开启 {@code hooks.allowRequestSessionKey} 且符合
 * {@code hooks.allowedSessionKeyPrefixes} 等策略时才会被接受；否则网关可能拒绝请求。
 * </p>
 */
@Getter
@Setter
@NoArgsConstructor
public class InvokeAgentRequest {

    /** 必填：发给 agent 的提示/任务内容 */
    private String message;

    private String agentId;
    private String name;
    private String wakeMode;
    private Integer timeoutSeconds;

    /**
     * 会话键；需要网关 {@code hooks.allowRequestSessionKey=true} 等配置配合。
     * <p>
     * 推荐通过 {@link OpenClawSessionKeys} 或 {@link OpenClawClient#agentWithStableSession} /
     * {@link OpenClawClient#agentOneShotForPeer} / {@link OpenClawClient#agentOneShot} 设置，而非手写字符串。
     * </p>
     */
    private String sessionKey;

    /**
     * 为 {@code true} 时将最终回复投递到通道；{@code null} 表示不在 JSON 中发送该字段（使用网关默认）。
     */
    private Boolean deliver;

    /** 投递目标通道，常与 {@link #deliver} 配合；例如文档中的 {@code last} */
    private String channel;

    /** 投递目标标识（如收件人），文档字段 {@code to} */
    private String to;

    /** 模型覆盖，形如 {@code openai/gpt-5.5} 或网关允许的其他 ref */
    private String model;

    /** 思考等级或开关，如文档示例 {@code off} */
    private String thinking;

    /**
     * 模型 fallback 链；{@code null} 表示不在 JSON 中发送该字段（使用 Gateway 默认）。
     * 显式空列表 {@code []} 表示严格模式，仅尝试主模型。
     */
    private List<String> fallbacks;

    /**
     * @param agentId 路由 agent
     * @param message 发给 agent 的提示/任务内容
     */
    public InvokeAgentRequest(String agentId, String message) {
        this.agentId = agentId;
        this.message = message;
    }
}
