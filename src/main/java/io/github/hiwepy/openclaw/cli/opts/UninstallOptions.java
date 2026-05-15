package io.github.hiwepy.openclaw.cli.opts;

import io.github.hiwepy.openclaw.cli.args.CliSubArgs;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * {@code openclaw uninstall}：卸载 Gateway 服务与本地数据（CLI 可保留）。
 * <p>下列布尔字段对应官方文档 Options 中的开关；可组合使用。文档建议：删除 state 或 workspace 前先执行
 * {@code openclaw backup create} 以便可恢复；{@code --non-interactive} 必须与 {@code --yes} 同用。</p>
 *
 * @see <a href="https://docs.openclaw.ai/cli/uninstall">uninstall CLI</a>
 */
public final class UninstallOptions implements CliSubArgs {

    /**
     * {@code --service}：移除 Gateway 托管服务（launchd/systemd 等）。
     */
    private final boolean service;
    /**
     * {@code --state}：删除状态与配置（本地 OpenClaw 状态/配置目录相关数据）。
     */
    private final boolean state;
    /**
     * {@code --workspace}：删除 agent workspace 目录。
     */
    private final boolean workspace;
    /**
     * {@code --app}：移除 macOS 应用包（仅适用 macOS 安装形态）。
     */
    private final boolean app;
    /**
     * {@code --all}：等价于同时选择 service、state、workspace、app（文档中的合并简写）。
     */
    private final boolean all;
    /**
     * {@code --yes}：跳过交互确认提示。
     */
    private final boolean yes;
    /**
     * {@code --non-interactive}：禁用一切提示；文档要求必须与 {@code --yes} 同时使用。
     */
    private final boolean nonInteractive;
    /**
     * {@code --dry-run}：仅打印将执行的操作，不实际删除文件。
     */
    private final boolean dryRun;
    /**
     * 文档未单独建模的附加 argv，按与 shell 相同的顺序追加在末尾。
     */
    private final List<String> extra;

    /**
     * @param b 构建器快照
     */
    private UninstallOptions(Builder b) {
        this.service = b.service;
        this.state = b.state;
        this.workspace = b.workspace;
        this.app = b.app;
        this.all = b.all;
        this.yes = b.yes;
        this.nonInteractive = b.nonInteractive;
        this.dryRun = b.dryRun;
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
        OpenClawCliArgv.addFlag(out, "--service", service);
        OpenClawCliArgv.addFlag(out, "--state", state);
        OpenClawCliArgv.addFlag(out, "--workspace", workspace);
        OpenClawCliArgv.addFlag(out, "--app", app);
        OpenClawCliArgv.addFlag(out, "--all", all);
        OpenClawCliArgv.addFlag(out, "--yes", yes);
        OpenClawCliArgv.addFlag(out, "--non-interactive", nonInteractive);
        OpenClawCliArgv.addFlag(out, "--dry-run", dryRun);
        OpenClawCliArgv.addExtra(out, extra);
        return Collections.unmodifiableList(out);
    }

    /**
     * {@link UninstallOptions} 构建器。
     */
    public static final class Builder {
        private boolean service;
        private boolean state;
        private boolean workspace;
        private boolean app;
        private boolean all;
        private boolean yes;
        private boolean nonInteractive;
        private boolean dryRun;
        private List<String> extra = new ArrayList<>();

        /**
         * @param service {@code --service}
         * @return {@code this}
         */
        public Builder service(boolean service) {
            this.service = service;
            return this;
        }

        /**
         * @param state {@code --state}
         * @return {@code this}
         */
        public Builder state(boolean state) {
            this.state = state;
            return this;
        }

        /**
         * @param workspace {@code --workspace}
         * @return {@code this}
         */
        public Builder workspace(boolean workspace) {
            this.workspace = workspace;
            return this;
        }

        /**
         * @param app {@code --app}
         * @return {@code this}
         */
        public Builder app(boolean app) {
            this.app = app;
            return this;
        }

        /**
         * @param all {@code --all}
         * @return {@code this}
         */
        public Builder all(boolean all) {
            this.all = all;
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
         * @param nonInteractive {@code --non-interactive}
         * @return {@code this}
         */
        public Builder nonInteractive(boolean nonInteractive) {
            this.nonInteractive = nonInteractive;
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
         * @return 不可变 {@link UninstallOptions}
         */
        public UninstallOptions build() {
            return new UninstallOptions(this);
        }
    }
}
