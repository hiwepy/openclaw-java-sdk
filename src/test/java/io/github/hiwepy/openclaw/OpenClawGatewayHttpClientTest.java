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
        r.setSessionKey("hook:test:1");
        r.setDeliver(true);
        r.setChannel("last");
        r.setTo("user1");
        r.setModel("openai/gpt-5.5");
        r.setThinking("off");
        Map<String, Object> body = OpenClawGatewayHttpClient.buildHooksAgentBody(r);
        assertEquals("hi", body.get("message"));
        assertEquals("main", body.get("agentId"));
        assertEquals("hook:test:1", body.get("sessionKey"));
        assertEquals(true, body.get("deliver"));
        assertEquals("last", body.get("channel"));
        assertEquals("user1", body.get("to"));
        assertEquals("openai/gpt-5.5", body.get("model"));
        assertEquals("off", body.get("thinking"));
        assertTrue(body.containsKey("timeoutSeconds"));
    }

    /**
     * 未设置 agentId 等可选字段时不写入 body。
     */
    @Test
    void buildHooksAgentBody_omitsUnsetOptionals() {
        InvokeAgentRequest r = new InvokeAgentRequest();
        r.setMessage("only");
        Map<String, Object> body = OpenClawGatewayHttpClient.buildHooksAgentBody(r);
        assertFalse(body.containsKey("agentId"));
        assertFalse(body.containsKey("sessionKey"));
    }

    @Test
    void buildHooksAgentBody_rejectsBlankMessage() {
        InvokeAgentRequest r = new InvokeAgentRequest();
        r.setMessage("   ");
        assertThrows(IllegalArgumentException.class, () -> OpenClawGatewayHttpClient.buildHooksAgentBody(r));
    }
}
