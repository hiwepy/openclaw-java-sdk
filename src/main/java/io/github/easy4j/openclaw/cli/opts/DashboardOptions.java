package io.github.easy4j.openclaw.cli.opts;

import io.github.easy4j.openclaw.util.OpenClawLists;
import io.github.easy4j.openclaw.cli.args.CliSubArgs;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * {@code openclaw dashboard}：用当前认证打开 Control UI。
 * <p>文档说明：会解析 {@code gateway.auth.token} 的 SecretRef；对 SecretRef 管理的 token，打印/复制/打开的 URL 为<strong>不含 token</strong>的形式，
 * 避免终端、剪贴板或浏览器启动参数泄露外部密钥。</p>
 *
 * @see <a href="https://docs.openclaw.ai/cli/dashboard">dashboard CLI</a>
 */
public final class DashboardOptions implements CliSubArgs {

    /**
     * {@code --no-open}：只输出 URL（或按文档复制），不自动拉起浏览器。
     */
    private final boolean noOpen;
    /**
     * 文档未覆盖的附加 CLI token。
     */
    private final List<String> extra;

    /**
     * @param b 构建器快照
     */
    private DashboardOptions(Builder b) {
        this.noOpen = b.noOpen;
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
        OpenClawCliArgv.addFlag(out, "--no-open", noOpen);
        OpenClawCliArgv.addExtra(out, extra);
        return Collections.unmodifiableList(out);
    }

    /**
     * {@link DashboardOptions} 构建器。
     */
    public static final class Builder {
        private boolean noOpen;
        private List<String> extra = new ArrayList<>();

        /**
         * @param noOpen {@code --no-open}
         * @return {@code this}
         */
        public Builder noOpen(boolean noOpen) {
            this.noOpen = noOpen;
            return this;
        }

        /**
         * 追加额外 CLI token。
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
         * @return 不可变 {@link DashboardOptions}
         */
        public DashboardOptions build() {
            return new DashboardOptions(this);
        }
    }
}
