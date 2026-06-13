package io.github.hiwepy.openclaw.api;

import java.util.Locale;
import java.util.Objects;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * OpenClaw Gateway {@code POST /hooks/agent} 的 {@code sessionKey} 命名工具。
 * <p>
 * 与 Gateway {@code hooks:} 命名空间对齐（参见 OpenClaw {@code resolveHookSessionKey}）：
 * </p>
 * <ul>
 *     <li><b>固定会话</b>（多轮对话）：{@code hook:<agentId>:<peerId>}</li>
 *     <li><b>一次性 + 有 peer</b>：{@code hook:<peerId>:<correlationId>}（每次新 correlationId → 新会话）</li>
 *     <li><b>一次性 + 无 peer</b>：请求体<b>不传</b> {@code sessionKey}，由 Gateway 生成 {@code hook:<uuid>}</li>
 * </ul>
 * <p>
 * 显式 {@code sessionKey} 需 Gateway 开启 {@code hooks.allowRequestSessionKey=true}，
 * 并满足 {@code hooks.allowedSessionKeyPrefixes}（通常包含 {@code hook:}）。
 * </p>
 */
public final class OpenClawSessionKeys {

    /** 允许字母/数字开头，支持大小写字母、数字、下划线、点、破折号，最长128字节 */
    private static final Pattern SAFE_SEGMENT = Pattern.compile("^[A-Za-z0-9][A-Za-z0-9._-]{0,127}$");

    private OpenClawSessionKeys() {
    }

    /**
     * 固定多轮会话：{@code hook:<agentId>:<peerId>}。
     *
     * @param agentId 路由 agent，与 {@link InvokeAgentRequest#setAgentId} 一致
     * @param peerId  业务 peer（如 userId、工单 id）；不可含 {@code :}
     */
    public static String forStableSession(String agentId, String peerId) {
        return "hook:" + normalizeSegment(agentId, "agentId") + ":"
                + normalizeSegment(peerId, "peerId");
    }

    /**
     * 一次性会话（有 peer）：{@code hook:<peerId>:<correlationId>}。
     *
     * @param peerId          业务 peer；不可含 {@code :}
     * @param correlationId   本次调用唯一 id（如 UUID）；不可含 {@code :}
     */
    public static String forEphemeralPeer(String peerId, String correlationId) {
        return "hook:" + normalizeSegment(peerId, "peerId") + ":"
                + normalizeSegment(correlationId, "correlationId");
    }

    /**
     * 一次性会话（有 peer），自动生成 {@link #newCorrelationId()} 作为 correlationId。
     *
     * @param peerId 业务 peer；不可含 {@code :}
     */
    public static String forEphemeralPeer(String peerId) {
        return forEphemeralPeer(peerId, newCorrelationId());
    }

    /**
     * 生成一次性 Hook 的 correlation 段（UUID），用于 {@link #forEphemeralPeer(String, String)}。
     */
    public static String newCorrelationId() {
        return UUID.randomUUID().toString();
    }

    /**
     * 规范化 hook session 片段：trim、小写，并校验不含 {@code :} 与非法字符。
     */
    static String normalizeSegment(String value, String fieldName) {
        Objects.requireNonNull(value, fieldName);
        String normalized = value.trim().toLowerCase(Locale.ROOT);
        if (normalized.isEmpty()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        if (normalized.contains(":")) {
            throw new IllegalArgumentException(fieldName + " must not contain ':'");
        }
        if (!SAFE_SEGMENT.matcher(normalized).matches()) {
            throw new IllegalArgumentException(fieldName + " contains illegal characters: " + value);
        }
        return normalized;
    }
}
