package io.github.hiwepy.openclaw;

import io.github.hiwepy.openclaw.api.InvokeAgentRequest;
import io.github.hiwepy.openclaw.api.InvokeAgentResult;
import io.github.hiwepy.openclaw.api.OpenClawClient;
import io.github.hiwepy.openclaw.api.OpenClawClientConfig;
import io.github.hiwepy.openclaw.api.OpenClawGatewayHttpClient;
import io.github.hiwepy.openclaw.api.OpenClawSessionKeys;

import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OpenClawSessionKeysTest {

    @Test
    void forStableSession_formatsHookAgentPeer() {
        assertEquals("hook:xiaohongshu-data-assistant:user-1001",
                OpenClawSessionKeys.forStableSession("xiaohongshu-data-assistant", "user-1001"));
        assertEquals("hook:main:alice",
                OpenClawSessionKeys.forStableSession(" Main ", " Alice "));
    }

    @Test
    void forEphemeralPeer_formatsHookPeerCorrelation() {
        assertEquals("hook:user-1001:run-abc",
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
        InvokeAgentRequest cleaned = new InvokeAgentRequest(
                source.message(), source.agentId(), source.name(), source.wakeMode(),
                source.timeoutSeconds(), null, source.deliver(), source.channel(),
                source.to(), source.model(), source.thinking());
        Map<String, Object> body = OpenClawGatewayHttpClient.buildHooksAgentBody(cleaned);
        assertFalse(body.containsKey("sessionKey"));
    }

    @Test
    void agentWithStableSession_buildsExpectedBody() {
        String sessionKey = OpenClawSessionKeys.forStableSession("my-agent", "peer-1");
        InvokeAgentRequest r = new InvokeAgentRequest("hello", "my-agent", "Generation", "now",
                300, sessionKey, null, null, null, null, null);
        Map<String, Object> body = OpenClawGatewayHttpClient.buildHooksAgentBody(r);
        assertEquals("hook:my-agent:peer-1", body.get("sessionKey"));
        assertEquals("my-agent", body.get("agentId"));
    }

    @Test
    void agentOneShotForPeer_buildsExpectedBody() {
        InvokeAgentRequest r = new InvokeAgentRequest("ping", "main", "Generation", "now", 300,
                OpenClawSessionKeys.forEphemeralPeer("u1", "corr-1"), null, null, null, null, null);
        Map<String, Object> body = OpenClawGatewayHttpClient.buildHooksAgentBody(r);
        assertEquals("hook:u1:corr-1", body.get("sessionKey"));
    }

    @Test
    void agentWithStableSession_fromRequestAgentId() {
        InvokeAgentRequest source = new InvokeAgentRequest("my-agent", "hello");
        InvokeAgentRequest r = source.withSessionKey(
                OpenClawSessionKeys.forStableSession(source.agentId(), "peer-2"));
        Map<String, Object> body = OpenClawGatewayHttpClient.buildHooksAgentBody(r);
        assertEquals("hook:my-agent:peer-2", body.get("sessionKey"));
    }

    @Test
    void newCorrelationId_isUuid() {
        UUID.fromString(OpenClawSessionKeys.newCorrelationId());
    }
}
