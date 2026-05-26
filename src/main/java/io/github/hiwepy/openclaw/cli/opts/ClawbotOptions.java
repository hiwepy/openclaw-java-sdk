package io.github.hiwepy.openclaw.cli.opts;

import io.github.hiwepy.openclaw.util.OpenClawLists;
import io.github.hiwepy.openclaw.cli.args.CliSubArgs;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * {@code openclaw clawbot}：遗留别名命名空间；当前支持 {@code clawbot qr}，语义与 {@link QrOptions} / {@code openclaw qr} 一致。
 * <p>载荷仍含短期 {@code bootstrapToken}；{@code --token} 与 {@code --password} 互斥；公网场景宜 {@code wss://} 或 Tailscale Serve/Funnel。</p>
 *
 * @see <a href="https://docs.openclaw.ai/cli/clawbot">clawbot CLI</a>
 */
public final class ClawbotOptions implements CliSubArgs {

    /**
     * clawbot 子命令模式（当前仅 {@code qr}）。
     */
    public enum Mode {
        /** 生成配对二维码与 setup code 的 {@code clawbot qr} 子命令。 */
        QR
    }

    /** 当前 clawbot 子命令（仅 QR）。 */
    private final Mode mode;
    /**
     * {@code --remote}：优先用 {@code gateway.remote.url}，否则可由 {@code gateway.tailscale.mode=serve|funnel} 推导公网 URL。
     */
    private final boolean remote;
    /**
     * {@code --url}：覆盖二维码载荷中的 Gateway WebSocket 地址。
     */
    private final String url;
    /**
     * {@code --public-url}：覆盖对外可见 URL（与 {@code --url} 分工见 qr 文档）。
     */
    private final String publicUrl;
    /**
     * {@code --token}：引导流程用的网关 token（与 {@code --password} 互斥）。
     */
    private final String token;
    /**
     * {@code --password}：引导流程用的网关密码（与 {@code --token} 互斥）。
     */
    private final String password;
    /**
     * {@code --setup-code-only}：只输出 setup code，不渲染完整二维码等。
     */
    private final boolean setupCodeOnly;
    /**
     * {@code --no-ascii}：不在终端绘制 ASCII 二维码。
     */
    private final boolean noAscii;
    /**
     * {@code --json}：机器可读输出（字段如 {@code setupCode}、{@code gatewayUrl}、{@code auth} 等见 qr 文档）。
     */
    private final boolean json;
    /**
     * 文档未枚举的附加参数，按 shell 顺序透传。
     */
    private final List<String> extra;

    /**
     * @param b 构建器快照
     */
    private ClawbotOptions(Builder b) {
        this.mode = b.mode;
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
        if (mode == Mode.QR) {
            out.add("qr");
            OpenClawCliArgv.addFlag(out, "--remote", remote);
            OpenClawCliArgv.addIfPresent(out, "--url", url);
            OpenClawCliArgv.addIfPresent(out, "--public-url", publicUrl);
            OpenClawCliArgv.addIfPresent(out, "--token", token);
            OpenClawCliArgv.addIfPresent(out, "--password", password);
            OpenClawCliArgv.addFlag(out, "--setup-code-only", setupCodeOnly);
            OpenClawCliArgv.addFlag(out, "--no-ascii", noAscii);
            OpenClawCliArgv.addFlag(out, "--json", json);
        }
        OpenClawCliArgv.addExtra(out, extra);
        return Collections.unmodifiableList(out);
    }

    /**
     * {@link ClawbotOptions} 构建器。
     */
    public static final class Builder {
        private Mode mode = Mode.QR;
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
         * @return {@code this}（{@code clawbot qr}）
         */
        public Builder qr() {
            this.mode = Mode.QR;
            return this;
        }

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
         * @return 不可变 {@link ClawbotOptions}
         */
        public ClawbotOptions build() {
            return new ClawbotOptions(this);
        }
    }
}
