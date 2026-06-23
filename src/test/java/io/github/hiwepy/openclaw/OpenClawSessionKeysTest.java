package io.github.hiwepy.openclaw;

import io.github.hiwepy.openclaw.api.model.HookRequest;
import io.github.hiwepy.openclaw.api.OpenClawGatewayHttpClient;
import io.github.hiwepy.openclaw.api.OpenClawSessionKeys;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

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
        UUID.fromString(key.substring("hook:user-1001:".length()));
    }

    @Test
    void normalizeSegment_rejectsColon() {
        assertThrows(IllegalArgumentException.class,
                () -> OpenClawSessionKeys.forStableSession("main", "bad:id"));
    }

    @Test
    void agentOneShot_omitsSessionKeyInBody() {
        HookRequest r = new HookRequest();
        r.setMessage("ping"); r.setAgentId("main");
        Map<String, Object> body = OpenClawGatewayHttpClient.buildHooksAgentBody(r);
        assertFalse(body.containsKey("sessionKey"));
    }

    @Test
    void agentWithStableSession_buildsExpectedBody() {
        HookRequest r = new HookRequest();
        r.setMessage("hello"); r.setAgentId("my-agent");
        r.setSessionKey(OpenClawSessionKeys.forStableSession("my-agent", "peer-1"));
        Map<String, Object> body = OpenClawGatewayHttpClient.buildHooksAgentBody(r);
        assertEquals("hook:my-agent:peer-1", body.get("sessionKey"));
        assertEquals("my-agent", body.get("agentId"));
    }

    @Test
    void agentOneShotForPeer_buildsExpectedBody() {
        HookRequest r = new HookRequest();
        r.setMessage("ping"); r.setAgentId("main");
        r.setSessionKey(OpenClawSessionKeys.forEphemeralPeer("u1", "corr-1"));
        Map<String, Object> body = OpenClawGatewayHttpClient.buildHooksAgentBody(r);
        assertEquals("hook:u1:corr-1", body.get("sessionKey"));
    }

    @Test
    void agentWithStableSession_fromRequestAgentId() {
        HookRequest source = new HookRequest();
        source.setMessage("hello"); source.setAgentId("my-agent");
        HookRequest copy = OpenClawClient.copyRequest(source);
        copy.setSessionKey(OpenClawSessionKeys.forStableSession(source.getAgentId(), "peer-2"));
        Map<String, Object> body = OpenClawGatewayHttpClient.buildHooksAgentBody(copy);
        assertEquals("hook:my-agent:peer-2", body.get("sessionKey"));
    }

    @Test
    void newCorrelationId_isUuid() {
        UUID.fromString(OpenClawSessionKeys.newCorrelationId());
    }
}
