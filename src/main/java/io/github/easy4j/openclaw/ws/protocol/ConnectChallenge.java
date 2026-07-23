package io.github.easy4j.openclaw.ws.protocol;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Gateway WebSocket 预连接挑战（{@code connect.challenge} 事件）。
 * <p>
 * 根据 Gateway 协议规范 v4，Gateway 在接受 {@code connect} 请求前，
 * 会先推送一个 {@code connect.challenge} 事件帧，包含：
 * <ul>
 *   <li>{@code nonce} - 服务端生成的随机数，客户端需在 connect 请求中回传</li>
 *   <li>{@code ts} - 服务端时间戳（Unix epoch 毫秒）</li>
 * </ul>
 * </p>
 *
 * <h3>握手流程</h3>
 * <ol>
 *   <li>客户端建立 WebSocket 连接</li>
 *   <li>Gateway 推送 {@code connect.challenge} 事件（包含 {@code nonce} 和 {@code ts}）</li>
 *   <li>客户端使用 {@code nonce} 签名设备身份（可选），构建 {@code connect} 请求</li>
 *   <li>Gateway 验证签名后返回 {@code hello-ok} 响应</li>
 * </ol>
 *
 * <h3>签名载荷</h3>
 * <p>推荐的签名载荷版本为 v3，绑定 {@code platform} 和 {@code deviceFamily}。
 * 旧版 v2 签名仍被接受用于兼容。</p>
 *
 * @see <a href="https://docs.openclaw.ai/gateway/protocol">Gateway Protocol</a>
 */
@Getter
@Setter
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ConnectChallenge {

    /**
     * 服务端生成的随机数。
     * <p>客户端需在 {@code connect} 请求的 {@code device.nonce} 字段中回传此值。</p>
     * <p>若客户端发送的 nonce 与服务端不匹配，将收到 {@code DEVICE_AUTH_NONCE_MISMATCH} 错误。</p>
     */
    private String nonce;

    /**
     * 服务端时间戳（Unix epoch 毫秒）。
     * <p>用于签名过期检查。若签名时间戳超出允许偏差，将收到 {@code DEVICE_AUTH_SIGNATURE_EXPIRED} 错误。</p>
     */
    private Long ts;
}
