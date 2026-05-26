package io.github.hiwepy.openclaw.ws;

import io.github.hiwepy.openclaw.OpenClawClientConfig;
import io.github.hiwepy.openclaw.ws.protocol.*;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * WebSocket 协议帧模型与客户端配置的单元测试。
 * <p>注意：本测试不启动真实 WS 连接，仅验证模型序列化/反序列化与参数构建。</p>
 */
class OpenClawWsProtocolTest {

    private final com.fasterxml.jackson.databind.ObjectMapper mapper =
            new com.fasterxml.jackson.databind.ObjectMapper()
                    .configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    @Test
    void testConnectParamsToMap() {
        ConnectParams params = new ConnectParams(
                1, 1,
                new ConnectParams.ClientInfo("test-client", "Test", "1.0.0", "java", "operator"),
                ConnectParams.AuthInfo.token("my-token")
        );

        Map<String, Object> map = params.toParamsMap();
        assertEquals(1, map.get("minProtocol"));
        assertEquals(1, map.get("maxProtocol"));
        assertNotNull(map.get("client"));
        assertNotNull(map.get("auth"));

        @SuppressWarnings("unchecked")
        Map<String, Object> auth = (Map<String, Object>) map.get("auth");
        assertEquals("my-token", auth.get("token"));
        assertNull(auth.get("password"));
    }

    @Test
    void testConnectParamsWithPassword() {
        ConnectParams params = new ConnectParams(
                1, 1,
                new ConnectParams.ClientInfo("test-client", null, "1.0.0", "java", "operator"),
                ConnectParams.AuthInfo.password("my-password")
        );

        Map<String, Object> map = params.toParamsMap();
        @SuppressWarnings("unchecked")
        Map<String, Object> auth = (Map<String, Object>) map.get("auth");
        assertNull(auth.get("token"));
        assertEquals("my-password", auth.get("password"));
    }

    @Test
    void testChatSendParamsToMap() {
        ChatSendParams params = ChatSendParams.builder()
                .sessionKey("main")
                .message("你好")
                .thinking("off")
                .timeoutMs(30000)
                .build();

        Map<String, Object> map = params.toParamsMap();
        assertEquals("main", map.get("sessionKey"));
        assertEquals("你好", map.get("message"));
        assertEquals("off", map.get("thinking"));
        assertEquals(30000, map.get("timeoutMs"));
        assertNull(map.get("deliver")); // 未设置的不写入
    }

    @Test
    void testSessionsSendParamsToMap() {
        SessionsSendParams params = SessionsSendParams.builder()
                .key("agent:openclaw-engineer:main")
                .message("帮我写插件")
                .build();

        Map<String, Object> map = params.toParamsMap();
        assertEquals("agent:openclaw-engineer:main", map.get("key"));
        assertEquals("帮我写插件", map.get("message"));
    }

    @Test
    void testRequestFrameSerialization() throws Exception {
        RequestFrame frame = new RequestFrame("abc123", "chat.send", Map.of("message", "hello"));
        String json = mapper.writeValueAsString(frame);

        assertTrue(json.contains("\"type\":\"req\""));
        assertTrue(json.contains("\"id\":\"abc123\""));
        assertTrue(json.contains("\"method\":\"chat.send\""));
    }

    @Test
    void testResponseFrameDeserialization() throws Exception {
        String json = "{\"type\":\"res\",\"id\":\"abc123\",\"ok\":true,\"payload\":{\"type\":\"hello-ok\",\"protocol\":1}}";
        GatewayFrame frame = mapper.readValue(json, GatewayFrame.class);

        assertInstanceOf(ResponseFrame.class, frame);
        ResponseFrame res = (ResponseFrame) frame;
        assertEquals("abc123", res.getId());
        assertTrue(res.isOk());
        assertNull(res.getError());
    }

    @Test
    void testEventFrameDeserialization() throws Exception {
        String json = "{\"type\":\"event\",\"event\":\"chat\",\"payload\":{\"delta\":true,\"text\":\"你好\"},\"seq\":42}";
        GatewayFrame frame = mapper.readValue(json, GatewayFrame.class);

        assertInstanceOf(EventFrame.class, frame);
        EventFrame ev = (EventFrame) frame;
        assertEquals("chat", ev.getEvent());
        assertEquals(42, ev.getSeq());
    }

    @Test
    void testErrorFrameDeserialization() throws Exception {
        String json = "{\"type\":\"res\",\"id\":\"abc\",\"ok\":false,\"error\":{\"code\":\"INVALID_REQUEST\",\"message\":\"bad params\"}}";
        GatewayFrame frame = mapper.readValue(json, GatewayFrame.class);

        assertInstanceOf(ResponseFrame.class, frame);
        ResponseFrame res = (ResponseFrame) frame;
        assertFalse(res.isOk());
        assertNotNull(res.getError());
        assertEquals("INVALID_REQUEST", res.getError().getCode());
        assertEquals("bad params", res.getError().getMessage());
    }

    @Test
    void testHelloOkDeserialization() throws Exception {
        String json = """
            {
                "type": "hello-ok",
                "protocol": 1,
                "server": { "version": "2026.5.1", "connId": "conn-123" },
                "features": {
                    "methods": ["send", "chat.send", "chat.history", "agent"],
                    "events": ["chat", "agent", "tick"]
                },
                "auth": { "role": "operator", "scopes": ["operator.admin"] },
                "policy": { "maxPayload": 1048576, "maxBufferedBytes": 524288, "tickIntervalMs": 30000 }
            }
            """;
        HelloOk hello = mapper.readValue(json, HelloOk.class);
        assertEquals(1, hello.getProtocol());
        assertEquals("2026.5.1", hello.getServer().getVersion());
        assertEquals(4, hello.getFeatures().getMethods().size());
        assertTrue(hello.getFeatures().getMethods().contains("chat.send"));
        assertEquals("operator", hello.getAuth().getRole());
        assertEquals(30000, hello.getPolicy().getTickIntervalMs());
    }

    @Test
    void testWsUriBuilding() {
        OpenClawClientConfig config = new OpenClawClientConfig();
        config.setGatewayBaseUrl("http://localhost:18789");

        OpenClawGatewayWsClient client = new OpenClawGatewayWsClient(config);
        assertEquals("ws://localhost:18789", client.getURI().toString());
    }

    @Test
    void testWsUriBuildingHttps() {
        OpenClawClientConfig config = new OpenClawClientConfig();
        config.setGatewayBaseUrl("https://my-gateway.example.com");

        OpenClawGatewayWsClient client = new OpenClawGatewayWsClient(config);
        assertEquals("wss://my-gateway.example.com", client.getURI().toString());
    }

    @Test
    void testWsUriBuildingTrailingSlash() {
        OpenClawClientConfig config = new OpenClawClientConfig();
        config.setGatewayBaseUrl("http://localhost:18789/");

        OpenClawGatewayWsClient client = new OpenClawGatewayWsClient(config);
        assertEquals("ws://localhost:18789", client.getURI().toString());
    }
}
