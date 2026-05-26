package io.github.hiwepy.openclaw.ws.protocol;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Gateway WS {@code connect} 握手成功响应（{@code hello-ok}）。
 * <p>与 {@code src/gateway/protocol/schema/frames.ts} 中 {@code HelloOkSchema} 对齐。</p>
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class HelloOk {

    @JsonProperty("type")
    private String type;

    @JsonProperty("protocol")
    private int protocol;

    @JsonProperty("server")
    private ServerInfo server;

    @JsonProperty("features")
    private FeaturesInfo features;

    @JsonProperty("auth")
    private AuthResult auth;

    @JsonProperty("policy")
    private PolicyInfo policy;

    public String getType() { return type; }
    public int getProtocol() { return protocol; }
    public ServerInfo getServer() { return server; }
    public FeaturesInfo getFeatures() { return features; }
    public AuthResult getAuth() { return auth; }
    public PolicyInfo getPolicy() { return policy; }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ServerInfo {
        @JsonProperty("version") private String version;
        @JsonProperty("connId") private String connId;
        public String getVersion() { return version; }
        public String getConnId() { return connId; }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class FeaturesInfo {
        @JsonProperty("methods") private List<String> methods;
        @JsonProperty("events") private List<String> events;
        public List<String> getMethods() { return methods; }
        public List<String> getEvents() { return events; }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class AuthResult {
        @JsonProperty("role") private String role;
        @JsonProperty("scopes") private List<String> scopes;
        public String getRole() { return role; }
        public List<String> getScopes() { return scopes; }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PolicyInfo {
        @JsonProperty("maxPayload") private int maxPayload;
        @JsonProperty("maxBufferedBytes") private int maxBufferedBytes;
        @JsonProperty("tickIntervalMs") private int tickIntervalMs;
        public int getMaxPayload() { return maxPayload; }
        public int getMaxBufferedBytes() { return maxBufferedBytes; }
        public int getTickIntervalMs() { return tickIntervalMs; }
    }
}
