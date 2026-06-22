package io.github.hiwepy.openclaw;

import lombok.Data;

/**
 * OpenClaw 客户端统一配置（纯 POJO，可与 Spring {@code @ConfigurationProperties} 映射）。
 * <p>
 * 组合 {@link OpenClawHttpClientConfig}（HTTP/Gateway 相关）与 {@link OpenClawCliConfig}（本地 CLI 相关），
 * 作为 {@link io.github.hiwepy.openclaw.OpenClawClient} 等统一入口的配置载体。
 * </p>
 *
 * @see OpenClawHttpClientConfig
 * @see OpenClawCliConfig
 * @author wandl
 * @since 1.0.0
 */
@Data
public class OpenClawClientConfig {

    /** HTTP/Gateway 相关配置 */
    private final OpenClawHttpClientConfig http = new OpenClawHttpClientConfig();

    /** 本地 CLI 相关配置 */
    private final OpenClawCliConfig cli = new OpenClawCliConfig();

}
