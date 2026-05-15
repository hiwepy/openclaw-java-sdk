package io.github.hiwepy.openclaw.cli.opts;

import io.github.hiwepy.openclaw.cli.args.CliSubArgs;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * {@code openclaw sandbox}：查看隔离 agent 执行所用沙箱运行时（Docker、SSH、OpenShell 等），并在配置或镜像变更后强制重建。
 * <p>{@code recreate} 会删除旧运行时，下次 agent 使用时会按当前配置重新创建；SSH/OpenShell remote 场景会删除远端规范 workspace 根再重种子。</p>
 *
 * @see <a href="https://docs.openclaw.ai/cli/sandbox">sandbox CLI</a>
 */
public final class SandboxOptions implements CliSubArgs {

    /**
     * sandbox 子命令：解释有效策略、列出运行时、或按范围重建。
     */
    public enum Mode {
        /** {@code sandbox explain}：打印生效 sandbox 模式、作用域、工具策略与提升门控等。 */
        EXPLAIN,
        /** {@code sandbox list}：列出运行时名称、后端、空闲时间、关联 session/agent 等。 */
        LIST,
        /** {@code sandbox recreate}：删除匹配的运行时以便下次重建。 */
        RECREATE
    }

    /** explain / list / recreate 之一。 */
    private final Mode mode;
    /**
     * {@code --session}：按会话键筛选 explain 或 recreate 范围。
     */
    private final String session;
    /**
     * {@code --agent}：按 agent id 筛选 explain/list/recreate 范围。
     */
    private final String agent;
    /**
     * {@code --json}：结构化输出（explain 与 list 均支持）。
     */
    private final boolean json;
    /**
     * list：{@code --browser} 仅列出浏览器相关容器。
     */
    private final boolean listBrowser;
    /**
     * recreate：{@code --all} 重建全部沙箱容器。
     */
    private final boolean recreateAll;
    /**
     * recreate：{@code --browser} 仅重建浏览器类容器。
     */
    private final boolean recreateBrowser;
    /**
     * recreate：{@code --force} 跳过交互确认。
     */
    private final boolean force;
    /**
     * 其它 argv 透传。
     */
    private final List<String> extra;

    /**
     * @param b 构建器快照
     */
    private SandboxOptions(Builder b) {
        this.mode = b.mode;
        this.session = b.session;
        this.agent = b.agent;
        this.json = b.json;
        this.listBrowser = b.listBrowser;
        this.recreateAll = b.recreateAll;
        this.recreateBrowser = b.recreateBrowser;
        this.force = b.force;
        this.extra = b.extra == null ? List.of() : List.copyOf(b.extra);
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
        switch (mode) {
            case EXPLAIN:
                out.add("explain");
                OpenClawCliArgv.addIfPresent(out, "--session", session);
                OpenClawCliArgv.addIfPresent(out, "--agent", agent);
                OpenClawCliArgv.addFlag(out, "--json", json);
                break;
            case LIST:
                out.add("list");
                OpenClawCliArgv.addFlag(out, "--browser", listBrowser);
                OpenClawCliArgv.addFlag(out, "--json", json);
                break;
            case RECREATE:
                out.add("recreate");
                OpenClawCliArgv.addFlag(out, "--all", recreateAll);
                OpenClawCliArgv.addIfPresent(out, "--session", session);
                OpenClawCliArgv.addIfPresent(out, "--agent", agent);
                OpenClawCliArgv.addFlag(out, "--browser", recreateBrowser);
                OpenClawCliArgv.addFlag(out, "--force", force);
                break;
            default:
                break;
        }
        OpenClawCliArgv.addExtra(out, extra);
        return Collections.unmodifiableList(out);
    }

    /**
     * {@link SandboxOptions} 构建器。
     */
    public static final class Builder {
        private Mode mode = Mode.LIST;
        private String session;
        private String agent;
        private boolean json;
        private boolean listBrowser;
        private boolean recreateAll;
        private boolean recreateBrowser;
        private boolean force;
        private List<String> extra = new ArrayList<>();

        /**
         * @return {@code this}（{@code sandbox explain}）
         */
        public Builder explain() {
            this.mode = Mode.EXPLAIN;
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
         * @param agent {@code --agent}
         * @return {@code this}
         */
        public Builder agent(String agent) {
            this.agent = agent;
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
         * @return {@code this}（{@code sandbox list}）
         */
        public Builder list() {
            this.mode = Mode.LIST;
            return this;
        }

        /**
         * @param browserOnly list：{@code --browser}
         * @return {@code this}
         */
        public Builder listBrowser(boolean browserOnly) {
            this.listBrowser = browserOnly;
            return this;
        }

        /**
         * @return {@code this}（{@code sandbox recreate}）
         */
        public Builder recreate() {
            this.mode = Mode.RECREATE;
            return this;
        }

        /**
         * @param all recreate：{@code --all}
         * @return {@code this}
         */
        public Builder recreateAll(boolean all) {
            this.recreateAll = all;
            return this;
        }

        /**
         * @param browserOnly recreate：{@code --browser}
         * @return {@code this}
         */
        public Builder recreateBrowser(boolean browserOnly) {
            this.recreateBrowser = browserOnly;
            return this;
        }

        /**
         * @param force {@code --force}
         * @return {@code this}
         */
        public Builder force(boolean force) {
            this.force = force;
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
         * @return 不可变 {@link SandboxOptions}
         */
        public SandboxOptions build() {
            return new SandboxOptions(this);
        }
    }
}
