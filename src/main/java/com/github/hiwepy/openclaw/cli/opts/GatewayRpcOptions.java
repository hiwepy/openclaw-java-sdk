package com.github.hiwepy.openclaw.cli.opts;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 与官方文档「Query a running Gateway」中列出的共享 WebSocket RPC 客户端参数一致，供
 * {@code gateway health|status|probe}、{@code openclaw logs} 等命令复用。
 * <p>文档要点：指定 {@code --url} 时，CLI 不会回退到配置文件或环境变量中的凭据，须显式传入
 * {@code --token} 或 {@code --password}；缺省凭据为错误。{@code --timeout} 为各命令的超时/预算（单位因命令而异，常见为毫秒）。</p>
 *
 * @see <a href="https://docs.openclaw.ai/cli/gateway">gateway CLI</a>
 */
public final class GatewayRpcOptions {

    /**
     * {@code --url}：目标 Gateway 的 WebSocket URL（如 {@code ws://127.0.0.1:18789}）。
     */
    private final String url;
    /**
     * {@code --token}：Gateway 访问令牌；与 {@code --url} 联用时须显式提供（文档不自动合并配置/env）。
     */
    private final String token;
    /**
     * {@code --password}：Gateway 密码认证；与 token 二选一，勿与 token 同时用于同一连接语义（见网关认证文档）。
     */
    private final String password;
    /**
     * {@code --timeout}：请求超时或预算字符串（文档表述为 timeout/budget，具体默认随子命令变化，如 status 探测默认 10000ms）。
     */
    private final String timeout;
    /**
     * {@code --expect-final}：在 Gateway 调用由 agent 支撑时，等待「最终」响应而非仅中间流式事件。
     */
    private final boolean expectFinal;
    /**
     * {@code --json}：机器可读 JSON 输出（关闭着色与 spinner 等人机格式）。
     */
    private final boolean json;

    /**
     * 由 {@link Builder} 构造；外部请使用 {@link #builder()}。
     *
     * @param b 非 null 构建器快照
     */
    private GatewayRpcOptions(Builder b) {
        this.url = b.url;
        this.token = b.token;
        this.password = b.password;
        this.timeout = b.timeout;
        this.expectFinal = b.expectFinal;
        this.json = b.json;
    }

    /**
     * @return {@code --url} 值，可为 null
     */
    public String getUrl() {
        return url;
    }

    /**
     * @return {@code --token} 值，可为 null
     */
    public String getToken() {
        return token;
    }

    /**
     * @return {@code --password} 值，可为 null
     */
    public String getPassword() {
        return password;
    }

    /**
     * @return {@code --timeout} 字符串，可为 null
     */
    public String getTimeout() {
        return timeout;
    }

    /**
     * @return 是否启用 {@code --expect-final}
     */
    public boolean isExpectFinal() {
        return expectFinal;
    }

    /**
     * @return 是否启用 {@code --json}
     */
    public boolean isJson() {
        return json;
    }

    /**
     * @return 用于链式配置的新 {@link Builder}
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * 将共享选项追加到参数列表（不含子命令名 {@code health|status|probe}）。
     */
    public void appendSharedFlags(List<String> args) {
        Objects.requireNonNull(args, "args");
        if (url != null && !url.isEmpty()) {
            args.add("--url");
            args.add(url);
        }
        if (token != null && !token.isEmpty()) {
            args.add("--token");
            args.add(token);
        }
        if (password != null && !password.isEmpty()) {
            args.add("--password");
            args.add(password);
        }
        if (timeout != null && !timeout.isEmpty()) {
            args.add("--timeout");
            args.add(timeout);
        }
        if (expectFinal) {
            args.add("--expect-final");
        }
        if (json) {
            args.add("--json");
        }
    }

    /**
     * 仅包含本选项对象产生的参数片段（不含子命令前缀）。
     */
    public List<String> toFlagList() {
        List<String> args = new ArrayList<>();
        appendSharedFlags(args);
        return args;
    }

    /**
     * {@link GatewayRpcOptions} 的可变构建器；字段与 CLI 一一对应。
     */
    public static final class Builder {

        private String url;
        private String token;
        private String password;
        private String timeout;
        private boolean expectFinal;
        private boolean json;

        /**
         * @param url {@code --url}
         * @return {@code this}
         */
        public Builder url(String url) {
            this.url = url;
            return this;
        }

        /**
         * @param token {@code --token}
         * @return {@code this}
         */
        public Builder token(String token) {
            this.token = token;
            return this;
        }

        /**
         * @param password {@code --password}
         * @return {@code this}
         */
        public Builder password(String password) {
            this.password = password;
            return this;
        }

        /** 超时/预算字符串，与 CLI 一致（如毫秒或文档约定格式）。 */
        public Builder timeout(String timeout) {
            this.timeout = timeout;
            return this;
        }

        /**
         * @param expectFinal {@code --expect-final}
         * @return {@code this}
         */
        public Builder expectFinal(boolean expectFinal) {
            this.expectFinal = expectFinal;
            return this;
        }

        /**
         * @param json {@code --json}
         * @return {@code this}
         */
        public Builder json(boolean json) {
            this.json = json;
            return this;
        }

        /**
         * @return 不可变 {@link GatewayRpcOptions}
         */
        public GatewayRpcOptions build() {
            return new GatewayRpcOptions(this);
        }
    }
}
