package io.github.easy4j.openclaw.cli.opts;

import io.github.easy4j.openclaw.util.OpenClawLists;
import io.github.easy4j.openclaw.cli.args.CliSubArgs;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * {@code openclaw tui}：打开连接 Gateway 的终端 UI。
 * <p>文档说明：启动时会尽量解析配置中的网关认证 SecretRef（token/password）；若在 agent workspace 目录下启动且未显式指定
 * {@code agent::...} 形式的 {@code --session}，会默认选中该 agent 的会话键。详见 TUI 指南链接。</p>
 *
 * @see <a href="https://docs.openclaw.ai/cli/tui">tui CLI</a>
 * @see <a href="https://docs.openclaw.ai/web/tui">TUI 指南</a>
 */
public final class TuiOptions implements CliSubArgs {

    /**
     * {@code --url}：Gateway WebSocket 地址（示例见官方文档）。
     */
    private final String url;
    /**
     * {@code --token}：网关 token，与文档示例 {@code openclaw tui --url ... --token &lt;token&gt;} 一致。
     */
    private final String token;
    /**
     * {@code --password}：网关密码认证（与 token 二选一，具体行为见网关认证文档）。
     */
    private final String password;
    /**
     * {@code --session}：会话键（如 {@code main}、{@code bugfix}）；显式 {@code agent::...} 可覆盖工作区推断。
     */
    private final String session;
    /**
     * {@code --deliver}：文档示例中与 {@code --session} 联用的投递相关行为开关。
     */
    private final boolean deliver;
    /**
     * 其余未在类型中建模的 CLI token。
     */
    private final List<String> extra;

    /**
     * @param b 构建器快照
     */
    private TuiOptions(Builder b) {
        this.url = b.url;
        this.token = b.token;
        this.password = b.password;
        this.session = b.session;
        this.deliver = b.deliver;
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
        OpenClawCliArgv.addIfPresent(out, "--url", url);
        OpenClawCliArgv.addIfPresent(out, "--token", token);
        OpenClawCliArgv.addIfPresent(out, "--password", password);
        OpenClawCliArgv.addIfPresent(out, "--session", session);
        OpenClawCliArgv.addFlag(out, "--deliver", deliver);
        OpenClawCliArgv.addExtra(out, extra);
        return Collections.unmodifiableList(out);
    }

    /**
     * {@link TuiOptions} 构建器。
     */
    public static final class Builder {
        private String url;
        private String token;
        private String password;
        private String session;
        private boolean deliver;
        private List<String> extra = new ArrayList<>();

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

        /**
         * @param session {@code --session}
         * @return {@code this}
         */
        public Builder session(String session) {
            this.session = session;
            return this;
        }

        /**
         * @param deliver {@code --deliver}
         * @return {@code this}
         */
        public Builder deliver(boolean deliver) {
            this.deliver = deliver;
            return this;
        }

        /**
         * @param tokens 额外 CLI token
         * @return {@code this}
         */
        public Builder extra(String... tokens) {
            if (tokens != null) {
                Collections.addAll(extra, tokens);
            }
            return this;
        }

        /**
         * @return 不可变 {@link TuiOptions}
         */
        public TuiOptions build() {
            return new TuiOptions(this);
        }
    }
}
