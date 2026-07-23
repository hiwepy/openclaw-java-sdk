package io.github.easy4j.openclaw.cli.opts;

import io.github.easy4j.openclaw.util.OpenClawLists;
import io.github.easy4j.openclaw.cli.args.CliSubArgs;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * {@code openclaw update}：在 stable/beta/dev 通道间安全切换并同步安装方式（npm 与 git 工作区流程见文档）。
 * <p>{@code --dry-run} 仅预览计划步骤；{@code --yes} 用于跳过降级等确认；{@code openclaw --update} 为等价简写。</p>
 *
 * @see <a href="https://docs.openclaw.ai/cli/update">update CLI</a>
 */
public final class UpdateOptions implements CliSubArgs {

    /**
     * update 子命令表面：默认执行更新、{@code update status} 查看通道与可用性、{@code update wizard} 交互选通道。
     */
    public enum Mode {
        /** 顶层 {@code openclaw update}（无子命令），执行更新主流程。 */
        DEFAULT,
        /** {@code update status}：展示当前通道、git 指针或 npm 版本与是否有可用更新。 */
        STATUS,
        /** {@code update wizard}：交互选择通道并确认是否在更新后重启 Gateway。 */
        WIZARD
    }

    /** 当前为默认更新、status 还是 wizard。 */
    private final Mode mode;
    /**
     * {@code --channel}：持久化更新通道（stable/beta/dev 等，文档说明与安装方式联动）。
     */
    private final String channel;
    /**
     * {@code --tag}：仅本次更新覆盖包目标（例如 git 分支或 npm dist-tag；{@code main} 对包安装有专门映射）。
     */
    private final String tag;
    /**
     * {@code --dry-run}：预览计划更新、通道与重启路径，不写配置、不安装、不重启。
     */
    private final boolean dryRun;
    /**
     * {@code --no-restart}：更新成功后不重启 Gateway 服务。
     */
    private final boolean noRestart;
    /**
     * {@code --yes}：跳过确认（例如降级风险提示）。
     */
    private final boolean yes;
    /**
     * {@code --json}：输出机器可读的 {@code UpdateRunResult} 或 status JSON。
     */
    private final boolean json;
    /**
     * {@code --timeout}：各步骤超时（默认更新流程约 1200 秒，status 默认较短，见文档）。
     */
    private final String timeout;
    /**
     * 文档未建模的 argv 追加片段。
     */
    private final List<String> extra;

    /**
     * @param b 构建器快照
     */
    private UpdateOptions(Builder b) {
        this.mode = b.mode;
        this.channel = b.channel;
        this.tag = b.tag;
        this.dryRun = b.dryRun;
        this.noRestart = b.noRestart;
        this.yes = b.yes;
        this.json = b.json;
        this.timeout = b.timeout;
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
        switch (mode) {
            case DEFAULT:
                OpenClawCliArgv.addIfPresent(out, "--channel", channel);
                OpenClawCliArgv.addIfPresent(out, "--tag", tag);
                OpenClawCliArgv.addFlag(out, "--dry-run", dryRun);
                OpenClawCliArgv.addFlag(out, "--no-restart", noRestart);
                OpenClawCliArgv.addFlag(out, "--yes", yes);
                OpenClawCliArgv.addFlag(out, "--json", json);
                OpenClawCliArgv.addIfPresent(out, "--timeout", timeout);
                break;
            case STATUS:
                out.add("status");
                OpenClawCliArgv.addFlag(out, "--json", json);
                OpenClawCliArgv.addIfPresent(out, "--timeout", timeout);
                break;
            case WIZARD:
                out.add("wizard");
                OpenClawCliArgv.addIfPresent(out, "--timeout", timeout);
                break;
            default:
                break;
        }
        OpenClawCliArgv.addExtra(out, extra);
        return Collections.unmodifiableList(out);
    }

    /**
     * {@link UpdateOptions} 构建器。
     */
    public static final class Builder {
        private Mode mode = Mode.DEFAULT;
        private String channel;
        private String tag;
        private boolean dryRun;
        private boolean noRestart;
        private boolean yes;
        private boolean json;
        private String timeout;
        private List<String> extra = new ArrayList<>();

        /**
         * 顶层 {@code openclaw update}（无子命令）。
         *
         * @return {@code this}
         */
        public Builder update() {
            this.mode = Mode.DEFAULT;
            return this;
        }

        /**
         * @param channel {@code --channel}
         * @return {@code this}
         */
        public Builder channel(String channel) {
            this.channel = channel;
            return this;
        }

        /**
         * @param tag {@code --tag}
         * @return {@code this}
         */
        public Builder tag(String tag) {
            this.tag = tag;
            return this;
        }

        /**
         * @param dryRun {@code --dry-run}
         * @return {@code this}
         */
        public Builder dryRun(boolean dryRun) {
            this.dryRun = dryRun;
            return this;
        }

        /**
         * @param noRestart {@code --no-restart}
         * @return {@code this}
         */
        public Builder noRestart(boolean noRestart) {
            this.noRestart = noRestart;
            return this;
        }

        /**
         * @param yes {@code --yes}
         * @return {@code this}
         */
        public Builder yes(boolean yes) {
            this.yes = yes;
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
         * @param timeout {@code --timeout}
         * @return {@code this}
         */
        public Builder timeout(String timeout) {
            this.timeout = timeout;
            return this;
        }

        /**
         * @return {@code this}（{@code update status}）
         */
        public Builder status() {
            this.mode = Mode.STATUS;
            return this;
        }

        /**
         * @return {@code this}（{@code update wizard}）
         */
        public Builder wizard() {
            this.mode = Mode.WIZARD;
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
         * @return 不可变 {@link UpdateOptions}
         */
        public UpdateOptions build() {
            return new UpdateOptions(this);
        }
    }
}
