package io.github.easy4j.openclaw.api;

import io.github.easy4j.openclaw.util.OpenClawStrings;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * OpenClaw Gateway HTTP API 自定义请求头常量与构建器。
 *
 * <p>对应文档中的 {@code x-openclaw-*} 系列头：</p>
 * <ul>
 *   <li>{@code x-openclaw-model} - 覆盖后端模型（如 {@code openai/gpt-5.4}）</li>
 *   <li>{@code x-openclaw-agent-id} - 兼容性 agent 覆盖</li>
 *   <li>{@code x-openclaw-session-key} - 显式会话路由</li>
 *   <li>{@code x-openclaw-message-channel} - 合成入口通道上下文</li>
 *   <li>{@code x-openclaw-scopes} - 权限范围声明（逗号分隔）</li>
 * </ul>
 *
 * @see <a href="https://docs.openclaw.ai/gateway/openai-http-api#agent-first-model-contract">Agent-first model contract</a>
 */
public final class OpenClawHeaders {

    /** Header: 覆盖后端模型 */
    public static final String X_OPENCLAW_MODEL = OpenClawConstants.HEADER_X_OPENCLAW_MODEL;

    /** Header: Agent ID 兼容性覆盖 */
    public static final String X_OPENCLAW_AGENT_ID = OpenClawConstants.HEADER_X_OPENCLAW_AGENT_ID;

    /** Header: 会话路由 Key */
    public static final String X_OPENCLAW_SESSION_KEY = OpenClawConstants.HEADER_X_OPENCLAW_SESSION_KEY;

    /** Header: 入口通道上下文 */
    public static final String X_OPENCLAW_MESSAGE_CHANNEL = OpenClawConstants.HEADER_X_OPENCLAW_MESSAGE_CHANNEL;

    /** Header: 权限范围声明 */
    public static final String X_OPENCLAW_SCOPES = OpenClawConstants.HEADER_X_OPENCLAW_SCOPES;

    private OpenClawHeaders() {}

    /**
     * 创建新的 Builder。
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * OpenClaw 自定义请求头构建器。
     */
    public static final class Builder {
        private String model;
        private String agentId;
        private String sessionKey;
        private String messageChannel;
        private String scopes;

        /**
         * 覆盖后端模型（如 {@code openai/gpt-5.4}、{@code gpt-5.5}）。
         */
        public Builder model(String model) {
            this.model = model;
            return this;
        }

        /**
         * 兼容性 agent 覆盖。
         */
        public Builder agentId(String agentId) {
            this.agentId = agentId;
            return this;
        }

        /**
         * 显式会话路由。
         */
        public Builder sessionKey(String sessionKey) {
            this.sessionKey = sessionKey;
            return this;
        }

        /**
         * 合成入口通道上下文（如 {@code slack}、{@code telegram}）。
         */
        public Builder messageChannel(String messageChannel) {
            this.messageChannel = messageChannel;
            return this;
        }

        /**
         * 权限范围声明（逗号分隔，如 {@code operator.read,operator.write}）。
         */
        public Builder scopes(String scopes) {
            this.scopes = scopes;
            return this;
        }

        /**
         * 构建不可变的头 Map（仅包含非空值）。
         */
        public Map<String, String> build() {
            Map<String, String> headers = new LinkedHashMap<>();
            if (OpenClawStrings.isNotBlank(model)) {
                headers.put(X_OPENCLAW_MODEL, model);
            }
            if (OpenClawStrings.isNotBlank(agentId)) {
                headers.put(X_OPENCLAW_AGENT_ID, agentId);
            }
            if (OpenClawStrings.isNotBlank(sessionKey)) {
                headers.put(X_OPENCLAW_SESSION_KEY, sessionKey);
            }
            if (OpenClawStrings.isNotBlank(messageChannel)) {
                headers.put(X_OPENCLAW_MESSAGE_CHANNEL, messageChannel);
            }
            if (OpenClawStrings.isNotBlank(scopes)) {
                headers.put(X_OPENCLAW_SCOPES, scopes);
            }
            return Collections.unmodifiableMap(headers);
        }
    }
}
