package io.github.easy4j.openclaw.util;

import io.github.easy4j.openclaw.api.OpenClawConstants;

import java.util.Map;
import java.util.Objects;

/**
 * SDK 内部字符串工具，避免引入 Spring/Commons 等传递依赖。
 */
public final class OpenClawStrings {

    private OpenClawStrings() {
    }

    /**
     * @return {@code true} 当值为 {@code null}、空串或仅空白
     */
    public static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    /**
     * @return {@code true} 当值非 {@code null} 且含非空白字符
     */
    public static boolean isNotBlank(String value) {
        return !isBlank(value);
    }

    /**
     * @return 非 blank 时返回 trim 后的值，否则 {@code null}
     */
    public static String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    /**
     * @return blank 时返回 {@code defaultValue}，否则返回原值 trim 结果
     */
    public static String defaultIfBlank(String value, String defaultValue) {
        String trimmed = trimToNull(value);
        return trimmed != null ? trimmed : Objects.requireNonNull(defaultValue, "defaultValue");
    }

    /**
     * @return {@code null} 转为空串
     */
    public static String nullToEmpty(String value) {
        return value != null ? value : "";
    }

    /**
     * 值非 blank 时写入 Map。
     */
    public static void putIfNotBlank(Map<String, Object> target, String key, String value) {
        Objects.requireNonNull(target, "target");
        Objects.requireNonNull(key, "key");
        if (isNotBlank(value)) {
            target.put(key, value.trim());
        }
    }

    /**
     * 判断 model 值是否为 Agent 目标路由。
     */
    public static boolean isAgentTarget(String value) {
        if (value == null) {
            return false;
        }
        return value.startsWith(OpenClawConstants.AGENT_PREFIX_OPENCLAW) ||
                value.startsWith(OpenClawConstants.AGENT_PREFIX_AGENT_COLON) ||
                value.startsWith(OpenClawConstants.AGENT_PREFIX_OPENCLAW_COLON);
    }
}
