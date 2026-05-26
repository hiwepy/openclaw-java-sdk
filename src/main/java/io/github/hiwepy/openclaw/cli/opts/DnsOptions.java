package io.github.hiwepy.openclaw.cli.opts;

import io.github.hiwepy.openclaw.util.OpenClawLists;
import io.github.hiwepy.openclaw.cli.args.CliSubArgs;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * {@code openclaw dns}：面向广域发现的 DNS 辅助（Tailscale + CoreDNS），当前以 macOS + Homebrew CoreDNS 为主。
 * <p>{@code dns setup} 无 {@code --apply} 时仅输出规划建议；{@code --apply} 需 sudo，会安装/更新 CoreDNS 配置并重启 brew 服务。</p>
 *
 * @see <a href="https://docs.openclaw.ai/cli/dns">dns CLI</a>
 */
public final class DnsOptions implements CliSubArgs {

    /**
     * {@code --domain}：unicast DNS-SD 使用的广域发现域（文档示例 {@code openclaw.internal}）；省略时取自配置 {@code discovery.wideArea.domain}。
     */
    private final String domain;
    /**
     * {@code --apply}：实际安装或更新 CoreDNS 配置并重启服务（文档：目前仅 macOS，需 Homebrew CoreDNS）。
     */
    private final boolean apply;
    /**
     * 附加 CLI token。
     */
    private final List<String> extra;

    /**
     * @param b 构建器快照
     */
    private DnsOptions(Builder b) {
        this.domain = b.domain;
        this.apply = b.apply;
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
        out.add("setup");
        OpenClawCliArgv.addIfPresent(out, "--domain", domain);
        OpenClawCliArgv.addFlag(out, "--apply", apply);
        OpenClawCliArgv.addExtra(out, extra);
        return Collections.unmodifiableList(out);
    }

    /**
     * {@link DnsOptions} 构建器；默认生成 {@code dns setup} 前缀（见 {@link #toSubcommandArguments()}）。
     */
    public static final class Builder {
        private String domain;
        private boolean apply;
        private List<String> extra = new ArrayList<>();

        /**
         * 显式选择 setup 流程（当前实现为无操作占位，保持链式 API 可读性）。
         *
         * @return {@code this}
         */
        public Builder setup() {
            return this;
        }

        /**
         * @param domain {@code --domain}
         * @return {@code this}
         */
        public Builder domain(String domain) {
            this.domain = domain;
            return this;
        }

        /**
         * @param apply {@code --apply}
         * @return {@code this}
         */
        public Builder apply(boolean apply) {
            this.apply = apply;
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
         * @return 不可变 {@link DnsOptions}
         */
        public DnsOptions build() {
            return new DnsOptions(this);
        }
    }
}
