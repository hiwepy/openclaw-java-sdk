package io.github.hiwepy.openclaw;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link OpenClawGatewayHttpClient} 响应解析单元测试（无需真实 Gateway）。
 */
class OpenClawGatewayHttpClientTest {

    @Test
    void parseOk_extractsBooleanFromJson() {
        assertTrue(OpenClawGatewayHttpClient.parseOk("{\"ok\":true,\"runId\":\"x\"}"));
        assertFalse(OpenClawGatewayHttpClient.parseOk("{\"ok\":false}"));
    }

    @Test
    void parseOk_fallbackToSubstring() {
        assertTrue(OpenClawGatewayHttpClient.parseOk("not-json but \"ok\":true here"));
    }

    @Test
    void parseRunId_readsField() {
        assertEquals("2795185c-cb1c-4b43-b27d-87496e78cb87",
                OpenClawGatewayHttpClient.parseRunId(
                        "{\"ok\":true,\"runId\":\"2795185c-cb1c-4b43-b27d-87496e78cb87\"}"));
    }

    @Test
    void normalizeHookName_acceptsSimpleNameAndHooksPrefix() {
        assertEquals("gmail", OpenClawGatewayHttpClient.normalizeHookName("gmail"));
        assertEquals("gmail", OpenClawGatewayHttpClient.normalizeHookName("hooks/gmail"));
        assertEquals("gmail", OpenClawGatewayHttpClient.normalizeHookName("/hooks/gmail"));
    }

    @Test
    void normalizeHookName_rejectsIllegalPathTraversal() {
        assertThrows(IllegalArgumentException.class,
                () -> OpenClawGatewayHttpClient.normalizeHookName("../wake"));
    }

    /**
     * {@link OpenClawGatewayHttpClient#buildHooksAgentBody}：必填 message，可选扩展字段仅在设置时出现。
     */
    @Test
    void buildHooksAgentBody_includesOptionalFieldsWhenSet() {
        InvokeAgentRequest r = new InvokeAgentRequest();
        r.setMessage("hi");
        r.setAgentId("main");
        r.setName("Email");
        r.setWakeMode("next-heartbeat");
        r.setTimeoutSeconds(120);
        r.setSessionKey("hook:test:1");
        r.setDeliver(true);
        r.setChannel("last");
        r.setTo("user1");
        r.setModel("openai/gpt-5.5");
        r.setThinking("off");
        r.setFallbacks(java.util.Arrays.asList("openai/gpt-5.4", "anthropic/claude"));
        Map<String, Object> body = OpenClawGatewayHttpClient.buildHooksAgentBody(r);
        assertEquals("hi", body.get("message"));
        assertEquals("main", body.get("agentId"));
        assertEquals("Email", body.get("name"));
        assertEquals("next-heartbeat", body.get("wakeMode"));
        assertEquals(120, body.get("timeoutSeconds"));
        assertEquals("hook:test:1", body.get("sessionKey"));
        assertEquals(true, body.get("deliver"));
        assertEquals("last", body.get("channel"));
        assertEquals("user1", body.get("to"));
        assertEquals("openai/gpt-5.5", body.get("model"));
        assertEquals("off", body.get("thinking"));
        assertEquals(2, ((java.util.List<?>) body.get("fallbacks")).size());
    }

    /**
     * 未设置可选字段时不写入 body，与文档 curl 示例（仅 message/name/model）一致。
     */
    @Test
    void buildHooksAgentBody_omitsUnsetOptionals() {
        InvokeAgentRequest r = new InvokeAgentRequest();
        r.setMessage("only");
        Map<String, Object> body = OpenClawGatewayHttpClient.buildHooksAgentBody(r);
        assertFalse(body.containsKey("agentId"));
        assertFalse(body.containsKey("sessionKey"));
        assertFalse(body.containsKey("name"));
        assertFalse(body.containsKey("wakeMode"));
        assertFalse(body.containsKey("timeoutSeconds"));
        assertFalse(body.containsKey("fallbacks"));
    }

    @Test
    void buildHooksAgentBody_includesEmptyFallbacksList() {
        InvokeAgentRequest r = new InvokeAgentRequest();
        r.setMessage("strict");
        r.setFallbacks(java.util.Collections.emptyList());
        Map<String, Object> body = OpenClawGatewayHttpClient.buildHooksAgentBody(r);
        assertTrue(body.containsKey("fallbacks"));
        assertTrue(((java.util.List<?>) body.get("fallbacks")).isEmpty());
    }

    @Test
    void buildHooksAgentBody_rejectsBlankMessage() {
        InvokeAgentRequest r = new InvokeAgentRequest();
        r.setMessage("   ");
        assertThrows(IllegalArgumentException.class, () -> OpenClawGatewayHttpClient.buildHooksAgentBody(r));
    }

    @Test
    void resolveHooksPath_rejectsRootPath() {
        OpenClawClientConfig config = new OpenClawClientConfig();
        config.setHooksPath("/");
        assertThrows(IllegalArgumentException.class, config::resolveHooksPath);
    }
}
