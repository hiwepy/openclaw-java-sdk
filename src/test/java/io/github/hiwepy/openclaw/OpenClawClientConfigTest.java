package io.github.hiwepy.openclaw;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * {@link OpenClawHttpClientConfig} 凭证解析单测。
 */
class OpenClawClientConfigTest {

    /**
     * {@link OpenClawHttpClientConfig#resolveHooksBearerToken()}：hooks 优先于兼容字段 apiKey。
     */
    @Test
    void resolveHooksBearerToken_prefersHooksToken() {
        OpenClawHttpClientConfig c = new OpenClawHttpClientConfig();
        c.setHooksToken("hook-only");
        c.setApiKey("should-not-win");
        assertEquals("hook-only", c.resolveHooksBearerToken());
    }

    /**
     * 未配置 hooksToken 时回退到 apiKey（兼容旧配置）。
     */
    @Test
    void resolveHooksBearerToken_fallsBackToApiKey() {
        OpenClawHttpClientConfig c = new OpenClawHttpClientConfig();
        c.setApiKey("legacy-key");
        assertEquals("legacy-key", c.resolveHooksBearerToken());
    }

    /**
     * gatewayAuthToken 不参与 Webhook Bearer 解析。
     */
    @Test
    void resolveHooksBearerToken_ignoresGatewayAuthToken() {
        OpenClawHttpClientConfig c = new OpenClawHttpClientConfig();
        c.setGatewayAuthToken("gateway-secret");
        assertEquals("", c.resolveHooksBearerToken());
    }
}
