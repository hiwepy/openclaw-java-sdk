package io.github.hiwepy.openclaw.ws.protocol;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.util.List;

/**
 * Gateway WS {@code connect} 握手成功响应（{@code hello-ok}）。
 * <p>与 {@code src/gateway/protocol/schema/frames.ts} 中 {@code HelloOkSchema} 对齐。</p>
 */
@Getter
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

    /** 服务端版本与连接 ID。 */
    @Getter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ServerInfo {
        @JsonProperty("version") private String version;
        @JsonProperty("connId") private String connId;
    }

    /** 服务端支持的 method 与 event 列表。 */
    @Getter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class FeaturesInfo {
        @JsonProperty("methods") private List<String> methods;
        @JsonProperty("events") private List<String> events;
    }

    /** 认证结果：角色与权限范围。 */
    @Getter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class AuthResult {
        @JsonProperty("role") private String role;
        @JsonProperty("scopes") private List<String> scopes;
    }

    /** 连接策略参数。 */
    @Getter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PolicyInfo {
        @JsonProperty("maxPayload") private int maxPayload;
        @JsonProperty("maxBufferedBytes") private int maxBufferedBytes;
        @JsonProperty("tickIntervalMs") private int tickIntervalMs;
    }
}
