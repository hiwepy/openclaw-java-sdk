package io.github.easy4j.openclaw.ws.protocol;

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
    private final DeviceInfo device;
    private final String role;

    public ConnectParams(int minProtocol, int maxProtocol, ClientInfo client, AuthInfo auth) {
        this(minProtocol, maxProtocol, client, auth, null, null);
    }

    /**
     * 完整构造（含设备身份和角色）。
     *
     * @param minProtocol 最低协议版本
     * @param maxProtocol 最高协议版本
     * @param client      客户端信息
     * @param auth        认证信息
     * @param device      设备身份信息（可选，用于 device token 流程）
     * @param role        连接角色（可选，如 {@code "operator"} 或 {@code "node"}）
     */
    public ConnectParams(int minProtocol, int maxProtocol, ClientInfo client, AuthInfo auth,
                         DeviceInfo device, String role) {
        this.minProtocol = minProtocol;
        this.maxProtocol = maxProtocol;
        this.client = client;
        this.auth = auth;
        this.device = device;
        this.role = role;
    }

    /**
     * 客户端身份信息。
     */
    @Getter
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ClientInfo {
        private final String id, displayName, version, platform, mode;

        public ClientInfo(String id, String displayName, String version, String platform, String mode) {
            this.id = id;
            this.displayName = displayName;
            this.version = version;
            this.platform = platform;
            this.mode = mode;
        }
        // Explicit getters (Lombok @Getter not processed in Maven build)
        public String getId() { return id; }
        public String getDisplayName() { return displayName; }
        public String getVersion() { return version; }
        public String getPlatform() { return platform; }
        public String getMode() { return mode; }
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
    /**
     * 设备身份信息。
     * <p>
     * 用于 Gateway 的设备认证和 pairing 流程。
     * 包含设备指纹、公钥、签名和挑战 nonce。
     * </p>
     *
     * <h3>签名载荷</h3>
     * <p>推荐 v3 签名载荷，绑定 {@code platform} 和 {@code deviceFamily}。
     * 旧版 v2 签名仍被接受用于兼容。</p>
     *
     * <h3>挑战流程</h3>
     * <ol>
     *   <li>Gateway 推送 {@code connect.challenge} 事件（包含 {@code nonce} 和 {@code ts}）</li>
     *   <li>客户端使用 {@code nonce} 构建设备签名</li>
     *   <li>在 connect 请求中回传 {@code nonce}</li>
     * </ol>
     */
    @Getter
    @JsonInclude(JsonInclude.Include.NON_NULL)
    class DeviceInfo {
        /** 设备指纹（基于公钥派生）。 */
        private final String id;
        /** 设备公钥。 */
        private final String publicKey;
        /** 设备签名。 */
        private final String signature;
        /** 签名时间戳（Unix epoch 毫秒）。 */
        private final Long signedAt;
        /**
         * 挑战 nonce（从 {@code connect.challenge} 事件获取）。
         * <p>Gateway v4+ 要求客户端回传此值。若 nonce 不匹配将收到
         * {@code DEVICE_AUTH_NONCE_MISMATCH} 错误。</p>
         */
        private final String nonce;

        public DeviceInfo(String id, String publicKey, String signature, Long signedAt, String nonce) {
            this.id = id;
            this.publicKey = publicKey;
            this.signature = signature;
            this.signedAt = signedAt;
            this.nonce = nonce;
        }
    }
