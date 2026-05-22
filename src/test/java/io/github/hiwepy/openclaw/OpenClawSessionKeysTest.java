package io.github.hiwepy.openclaw;

import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link OpenClawSessionKeys} 与 {@link OpenClawClient} Hook sessionKey 便捷方法单元测试。
 */
class OpenClawSessionKeysTest {

    @Test
    void forStableSession_formatsHookAgentPeer() {
        assertEquals(
                "hook:xiaohongshu-data-assistant:user-1001",
                OpenClawSessionKeys.forStableSession("xiaohongshu-data-assistant", "user-1001"));
        assertEquals(
                "hook:main:alice",
                OpenClawSessionKeys.forStableSession(" Main ", " Alice "));
    }

    @Test
    void forEphemeralPeer_formatsHookPeerCorrelation() {
        assertEquals(
                "hook:user-1001:run-abc",
                OpenClawSessionKeys.forEphemeralPeer("user-1001", "run-abc"));
    }

    @Test
    void forEphemeralPeer_generatesUuidSuffix() {
        String key = OpenClawSessionKeys.forEphemeralPeer("user-1001");
        assertTrue(key.startsWith("hook:user-1001:"));
        String suffix = key.substring("hook:user-1001:".length());
        UUID.fromString(suffix);
    }

    @Test
    void normalizeSegment_rejectsColon() {
        assertThrows(IllegalArgumentException.class,
                () -> OpenClawSessionKeys.forStableSession("main", "bad:id"));
    }

    @Test
    void agentOneShot_omitsSessionKeyInBody() {
        InvokeAgentRequest source = new InvokeAgentRequest("main", "ping");
        source.setSessionKey("hook:should:strip");

        InvokeAgentRequest copy = OpenClawClient.copyRequest(source);
        copy.setSessionKey(null);
        Map<String, Object> body = OpenClawGatewayHttpClient.buildHooksAgentBody(copy);
        assertFalse(body.containsKey("sessionKey"));
    }

    @Test
    void agentWithStableSession_buildsExpectedBody() {
        InvokeAgentRequest source = new InvokeAgentRequest();
        source.setMessage("hello");

        InvokeAgentRequest copy = OpenClawClient.copyRequest(source);
        copy.setSessionKey(OpenClawSessionKeys.forStableSession("my-agent", "peer-1"));
        copy.setAgentId("my-agent");

        Map<String, Object> body = OpenClawGatewayHttpClient.buildHooksAgentBody(copy);
        assertEquals("hook:my-agent:peer-1", body.get("sessionKey"));
        assertEquals("my-agent", body.get("agentId"));
    }

    @Test
    void agentOneShotForPeer_buildsExpectedBody() {
        InvokeAgentRequest copy = OpenClawClient.copyRequest(new InvokeAgentRequest("main", "ping"));
        copy.setSessionKey(OpenClawSessionKeys.forEphemeralPeer("u1", "corr-1"));

        Map<String, Object> body = OpenClawGatewayHttpClient.buildHooksAgentBody(copy);
        assertEquals("hook:u1:corr-1", body.get("sessionKey"));
    }

    @Test
    void agentWithStableSession_fromRequestAgentId() {
        InvokeAgentRequest source = new InvokeAgentRequest("my-agent", "hello");
        InvokeAgentRequest copy = OpenClawClient.copyRequest(source);
        copy.setSessionKey(OpenClawSessionKeys.forStableSession(source.getAgentId(), "peer-2"));

        Map<String, Object> body = OpenClawGatewayHttpClient.buildHooksAgentBody(copy);
        assertEquals("hook:my-agent:peer-2", body.get("sessionKey"));
    }

    @Test
    void newCorrelationId_isUuid() {
        UUID.fromString(OpenClawSessionKeys.newCorrelationId());
    }
}
