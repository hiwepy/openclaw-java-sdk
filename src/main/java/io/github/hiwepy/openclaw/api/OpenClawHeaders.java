package io.github.hiwepy.openclaw.api;

import io.github.hiwepy.openclaw.util.OpenClawStrings;

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

    public static final String X_OPENCLAW_MODEL = "x-openclaw-model";
    public static final String X_OPENCLAW_AGENT_ID = "x-openclaw-agent-id";
    public static final String X_OPENCLAW_SESSION_KEY = "x-openclaw-session-key";
    public static final String X_OPENCLAW_MESSAGE_CHANNEL = "x-openclaw-message-channel";
    public static final String X_OPENCLAW_SCOPES = "x-openclaw-scopes";

    private OpenClawHeaders() {
    }

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
         * <p>对应 {@code x-openclaw-model} 头。</p>
         */
        public Builder model(String model) {
            this.model = model;
            return this;
        }

        /**
         * 兼容性 agent 覆盖。
         * <p>对应 {@code x-openclaw-agent-id} 头。</p>
         */
        public Builder agentId(String agentId) {
            this.agentId = agentId;
            return this;
        }

        /**
         * 显式会话路由。
         * <p>对应 {@code x-openclaw-session-key} 头。</p>
         */
        public Builder sessionKey(String sessionKey) {
            this.sessionKey = sessionKey;
            return this;
        }

        /**
         * 合成入口通道上下文（如 {@code slack}、{@code telegram}）。
         * <p>对应 {@code x-openclaw-message-channel} 头。</p>
         */
        public Builder messageChannel(String messageChannel) {
            this.messageChannel = messageChannel;
            return this;
        }

        /**
         * 权限范围声明（逗号分隔，如 {@code operator.read,operator.write}）。
         * <p>对应 {@code x-openclaw-scopes} 头。
         * 共享密钥鉴权模式下此头被忽略。</p>
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
