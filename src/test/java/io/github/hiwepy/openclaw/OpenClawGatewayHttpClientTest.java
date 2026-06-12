package io.github.hiwepy.openclaw;

import io.github.hiwepy.openclaw.api.InvokeAgentRequest;
import io.github.hiwepy.openclaw.api.InvokeAgentResult;
import io.github.hiwepy.openclaw.api.OpenClawClient;
import io.github.hiwepy.openclaw.api.OpenClawClientConfig;
import io.github.hiwepy.openclaw.api.OpenClawGatewayHttpClient;
import io.github.hiwepy.openclaw.api.OpenClawSessionKeys;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

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

    @Test
    void buildHooksAgentBody_includesOptionalFieldsWhenSet() {
        InvokeAgentRequest r = new InvokeAgentRequest("hi", "main", "Generation", "now", 300,
                "hook:test:1", true, "last", "user1", "openai/gpt-5.5", "off");
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

    @Test
    void buildHooksAgentBody_omitsUnsetOptionals() {
        InvokeAgentRequest r = new InvokeAgentRequest("only", null, "Generation", "now", 300,
                null, null, null, null, null, null);
        Map<String, Object> body = OpenClawGatewayHttpClient.buildHooksAgentBody(r);
        assertFalse(body.containsKey("agentId"));
        assertFalse(body.containsKey("sessionKey"));
    }

    @Test
    void buildHooksAgentBody_rejectsBlankMessage() {
        InvokeAgentRequest r = new InvokeAgentRequest("main", "   ");
        assertThrows(IllegalArgumentException.class,
                () -> OpenClawGatewayHttpClient.buildHooksAgentBody(r));
    }
}
