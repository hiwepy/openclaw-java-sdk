package io.github.easy4j.openclaw.cli.opts;

import io.github.easy4j.openclaw.util.OpenClawLists;
import io.github.easy4j.openclaw.cli.args.CliSubArgs;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * {@code openclaw qr}：根据当前 Gateway 配置生成移动端配对二维码与 setup code（载荷中含短期 {@code bootstrapToken}，非共享网关口令）。
 * <p>文档注意：{@code --token} 与 {@code --password} 互斥；{@code --remote} 需配置 {@code gateway.remote.url} 或
 * {@code gateway.tailscale.mode=serve|funnel}；公网/Tailscale 场景对 {@code ws://} 可能失败，宜 {@code wss://} 或 Tailscale Serve/Funnel。</p>
 *
 * @see <a href="https://docs.openclaw.ai/cli/qr">qr CLI</a>
 */
public final class QrOptions implements CliSubArgs {

    /**
     * {@code --remote}：优先使用 {@code gateway.remote.url}；未设置时仍可由 {@code gateway.tailscale.mode=serve|funnel} 提供公网 URL。
     */
    private final boolean remote;
    /**
     * {@code --url}：覆盖载荷中使用的 Gateway WebSocket URL。
     */
    private final String url;
    /**
     * {@code --public-url}：覆盖载荷中的对外可见 URL（与 {@code --url} 分工见文档）。
     */
    private final String publicUrl;
    /**
     * {@code --token}：覆盖引导流程认证所用的网关 token（与 {@code --password} 互斥）。
     */
    private final String token;
    /**
     * {@code --password}：覆盖引导流程认证所用的网关密码（与 {@code --token} 互斥）。
     */
    private final String password;
    /**
     * {@code --setup-code-only}：仅打印 setup code，不输出完整 QR 等。
     */
    private final boolean setupCodeOnly;
    /**
     * {@code --no-ascii}：跳过终端 ASCII 二维码绘制。
     */
    private final boolean noAscii;
    /**
     * {@code --json}：输出 JSON（含 {@code setupCode}、{@code gatewayUrl}、{@code auth}、{@code urlSource} 等字段，见文档）。
     */
    private final boolean json;
    /**
     * 文档未枚举的附加参数，按 shell 顺序透传。
     */
    private final List<String> extra;

    /**
     * @param b 构建器快照
     */
    private QrOptions(Builder b) {
        this.remote = b.remote;
        this.url = b.url;
        this.publicUrl = b.publicUrl;
        this.token = b.token;
        this.password = b.password;
        this.setupCodeOnly = b.setupCodeOnly;
        this.noAscii = b.noAscii;
        this.json = b.json;
        this.extra = b.extra == null ? OpenClawLists.empty() : OpenClawLists.copyOf(b.extra);
    }

    /**
     * @return 新 {@link Builder}
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> toSubcommandArguments() {
        List<String> out = new ArrayList<>();
        OpenClawCliArgv.addFlag(out, "--remote", remote);
        OpenClawCliArgv.addIfPresent(out, "--url", url);
        OpenClawCliArgv.addIfPresent(out, "--public-url", publicUrl);
        OpenClawCliArgv.addIfPresent(out, "--token", token);
        OpenClawCliArgv.addIfPresent(out, "--password", password);
        OpenClawCliArgv.addFlag(out, "--setup-code-only", setupCodeOnly);
        OpenClawCliArgv.addFlag(out, "--no-ascii", noAscii);
        OpenClawCliArgv.addFlag(out, "--json", json);
        OpenClawCliArgv.addExtra(out, extra);
        return Collections.unmodifiableList(out);
    }

    /**
     * {@link QrOptions} 构建器。
     */
    public static final class Builder {
        private boolean remote;
        private String url;
        private String publicUrl;
        private String token;
        private String password;
        private boolean setupCodeOnly;
        private boolean noAscii;
        private boolean json;
        private List<String> extra = new ArrayList<>();

        /**
         * @param remote {@code --remote}
         * @return {@code this}
         */
        public Builder remote(boolean remote) {
            this.remote = remote;
            return this;
        }

        /**
         * @param url {@code --url}
         * @return {@code this}
         */
        public Builder url(String url) {
            this.url = url;
            return this;
        }

        /**
         * @param publicUrl {@code --public-url}
         * @return {@code this}
         */
        public Builder publicUrl(String publicUrl) {
            this.publicUrl = publicUrl;
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

        /**
         * @param setupCodeOnly {@code --setup-code-only}
         * @return {@code this}
         */
        public Builder setupCodeOnly(boolean setupCodeOnly) {
            this.setupCodeOnly = setupCodeOnly;
            return this;
        }

        /**
         * @param noAscii {@code --no-ascii}
         * @return {@code this}
         */
        public Builder noAscii(boolean noAscii) {
            this.noAscii = noAscii;
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
         * 追加额外 argv token。
         *
         * @param tokens 可为 null（忽略）
         * @return {@code this}
         */
        public Builder extra(String... tokens) {
            if (tokens != null) {
                Collections.addAll(extra, tokens);
            }
            return this;
        }

        /**
         * @return 不可变 {@link QrOptions}
         */
        public QrOptions build() {
            return new QrOptions(this);
        }
    }
}
