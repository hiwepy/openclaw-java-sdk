package io.github.easy4j.openclaw;

import io.github.easy4j.openclaw.api.model.HookRequest;
import io.github.easy4j.openclaw.api.OpenClawWebhookClient;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class OpenClawGatewayHttpClientTest {

    @Test
    void parseOk_extractsBooleanFromJson() {
        assertTrue(OpenClawWebhookClient.parseOk("{\"ok\":true,\"runId\":\"x\"}"));
        assertFalse(OpenClawWebhookClient.parseOk("{\"ok\":false}"));
    }

    @Test
    void parseOk_fallbackToSubstring() {
        assertTrue(OpenClawWebhookClient.parseOk("not-json but \"ok\":true here"));
    }

    @Test
    void parseRunId_readsField() {
        assertEquals("2795185c-cb1c-4b43-b27d-87496e78cb87",
                OpenClawWebhookClient.parseRunId(
                        "{\"ok\":true,\"runId\":\"2795185c-cb1c-4b43-b27d-87496e78cb87\"}"));
    }

    @Test
    void normalizeHookName_acceptsSimpleNameAndHooksPrefix() {
        assertEquals("gmail", OpenClawWebhookClient.normalizeHookName("gmail"));
        assertEquals("gmail", OpenClawWebhookClient.normalizeHookName("hooks/gmail"));
        assertEquals("gmail", OpenClawWebhookClient.normalizeHookName("/hooks/gmail"));
    }

    @Test
    void normalizeHookName_rejectsIllegalPathTraversal() {
        assertThrows(IllegalArgumentException.class,
                () -> OpenClawWebhookClient.normalizeHookName("../wake"));
    }

    @Test
    void buildHooksAgentBody_includesOptionalFieldsWhenSet() {
        HookRequest r = new HookRequest();
        r.setMessage("hi"); r.setAgentId("main"); r.setSessionKey("hook:test:1");
        r.setDeliver(true); r.setChannel("last"); r.setTo("user1");
        r.setModel("openai/gpt-5.5"); r.setThinking("off");
        Map<String, Object> body = OpenClawWebhookClient.buildHooksAgentBody(r);
        assertEquals("hi", body.get("message"));
        assertEquals("main", body.get("agentId"));
        assertEquals("hook:test:1", body.get("sessionKey"));
        assertEquals(true, body.get("deliver"));
        assertEquals("user1", body.get("to"));
        assertEquals("openai/gpt-5.5", body.get("model"));
        assertEquals("off", body.get("thinking"));
    }

    @Test
    void buildHooksAgentBody_omitsUnsetOptionals() {
        HookRequest r = new HookRequest();
        r.setMessage("only");
        Map<String, Object> body = OpenClawWebhookClient.buildHooksAgentBody(r);
        assertFalse(body.containsKey("agentId"));
        assertFalse(body.containsKey("sessionKey"));
    }

    @Test
    void buildHooksAgentBody_rejectsBlankMessage() {
        HookRequest r = new HookRequest();
        r.setMessage("   ");
        assertThrows(IllegalArgumentException.class,
                () -> OpenClawWebhookClient.buildHooksAgentBody(r));
    }
}
