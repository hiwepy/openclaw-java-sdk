package io.github.hiwepy.openclaw.ws.protocol;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Gateway WS {@code connect} 握手请求参数。
 * <p>与 {@code src/gateway/protocol/schema/frames.ts} 中 {@code ConnectParamsSchema} 对齐。</p>
 */
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ConnectParams {

    private final int minProtocol;
    private final int maxProtocol;
    private final ClientInfo client;
    private final AuthInfo auth;

    public ConnectParams(int minProtocol, int maxProtocol, ClientInfo client, AuthInfo auth) {
        this.minProtocol = minProtocol;
        this.maxProtocol = maxProtocol;
        this.client = client;
        this.auth = auth;
    }

    /**
     * 客户端身份信息。
     */
    @Getter
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ClientInfo {
        private final String id;
        private final String displayName;
        private final String version;
        private final String platform;
        private final String mode;

        public ClientInfo(String id, String displayName, String version, String platform, String mode) {
            this.id = id;
            this.displayName = displayName;
            this.version = version;
            this.platform = platform;
            this.mode = mode;
        }
    }

    /**
     * 认证信息（token 或 password 二选一）。
     */
    @Getter
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class AuthInfo {
        private final String token;
        private final String password;

        private AuthInfo(String token, String password) {
            this.token = token;
            this.password = password;
        }

        public static AuthInfo token(String token) { return new AuthInfo(token, null); }
        public static AuthInfo password(String password) { return new AuthInfo(null, password); }
    }

    /**
     * 构建为 RPC params Map。
     */
    public Map<String, Object> toParamsMap() {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("minProtocol", minProtocol);
        m.put("maxProtocol", maxProtocol);
        Map<String, Object> clientMap = new LinkedHashMap<>();
        clientMap.put("id", client.getId());
        clientMap.put("version", client.getVersion());
        clientMap.put("platform", client.getPlatform());
        clientMap.put("mode", client.getMode());
        if (client.getDisplayName() != null) {
            clientMap.put("displayName", client.getDisplayName());
        }
        m.put("client", clientMap);
        if (auth != null) {
            Map<String, Object> authMap = new LinkedHashMap<>();
            if (auth.getToken() != null) authMap.put("token", auth.getToken());
            if (auth.getPassword() != null) authMap.put("password", auth.getPassword());
            m.put("auth", authMap);
        }
        return m;
    }
}
