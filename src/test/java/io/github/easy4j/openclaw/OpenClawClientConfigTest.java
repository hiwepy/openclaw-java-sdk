package io.github.easy4j.openclaw;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * {@link OpenClawClientConfig} 组装与凭证解析单测。
 */
class OpenClawClientConfigTest {

    /**
     * 默认构造后 http / cli 子配置均非空。
     */
    @Test
    void defaults_bothSubConfigsPresent() {
        OpenClawClientConfig config = new OpenClawClientConfig();
        assertNotNull(config.getHttp());
        assertNotNull(config.getCli());
    }

    /**
     * 通过包装类的 http 子配置设置 hooksToken 后可正确解析。
     */
    @Test
    void resolveHooksBearerToken_prefersHooksToken() {
        OpenClawClientConfig config = new OpenClawClientConfig();
        config.getHttp().setHooksToken("hook-only");
        assertEquals("hook-only", config.getHttp().resolveHooksBearerToken());
    }

    /**
     * 未配置 hooksToken 时返回空字符串。
     */
    @Test
    void resolveHooksBearerToken_returnsEmptyWhenNotConfigured() {
        OpenClawClientConfig config = new OpenClawClientConfig();
        assertEquals("", config.getHttp().resolveHooksBearerToken());
    }

    /**
     * gatewayAuthToken 不参与 Webhook Bearer 解析。
     */
    @Test
    void resolveHooksBearerToken_ignoresGatewayAuthToken() {
        OpenClawClientConfig config = new OpenClawClientConfig();
        config.getHttp().setGatewayAuthToken("gateway-secret");
        assertEquals("", config.getHttp().resolveHooksBearerToken());
    }

    /**
     * 通过包装类的 cli 子配置可正确读取可执行文件路径默认值。
     */
    @Test
    void cliConfig_defaultExecutable() {
        OpenClawClientConfig config = new OpenClawClientConfig();
        assertEquals("openclaw", config.getCli().getExecutable());
    }
}
