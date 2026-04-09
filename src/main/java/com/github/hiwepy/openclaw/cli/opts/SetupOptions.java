package com.github.hiwepy.openclaw.cli.opts;

import com.github.hiwepy.openclaw.cli.args.CliSubArgs;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
/**
 * {@code openclaw setup}：初始化 {@code ~/.openclaw/openclaw.json} 与 agent workspace。
 * <p>纯 {@code setup} 不写完整 onboarding；一旦出现 {@code --wizard}、{@code --non-interactive}、{@code --mode}、{@code --remote-url}、{@code --remote-token} 等任一 onboarding 相关 flag，
 * 文档说明会自动进入 onboarding 流程。</p>
 *
 * @see <a href="https://docs.openclaw.ai/cli/setup">setup CLI</a>
 */
public final class SetupOptions implements CliSubArgs {

    /**
     * {@code --workspace}：agent workspace 目录，持久化为配置项 {@code agents.defaults.workspace}。
     */
    private final String workspace;
    /**
     * {@code --wizard}：运行交互式 onboarding。
     */
    private final boolean wizard;
    /**
     * {@code --non-interactive}：无提示 onboarding（常与 remote 参数组合）。
     */
    private final boolean nonInteractive;
    /**
     * {@code --mode}：onboarding 模式（文档 {@code local} / {@code remote}，见 {@link SetupMode}）。
     */
    private final SetupMode mode;
    /**
     * {@code --remote-url}：远程 Gateway WebSocket URL（remote 模式）。
     */
    private final String remoteUrl;
    /**
     * {@code --remote-token}：远程 Gateway 访问令牌。
     */
    private final String remoteToken;

    /**
     * @param b 构建器快照
     */
    private SetupOptions(Builder b) {
        this.workspace = b.workspace;
        this.wizard = b.wizard;
        this.nonInteractive = b.nonInteractive;
        this.mode = b.mode;
        this.remoteUrl = b.remoteUrl;
        this.remoteToken = b.remoteToken;
    }

    /**
     * @return 新 {@link Builder}
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Onboarding 模式（文档：{@code local} / {@code remote}）。
     */
    public enum SetupMode {
        /** 本地模式。 */
        LOCAL,
        /** 远程 Gateway 模式。 */
        REMOTE
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> toSubcommandArguments() {
        List<String> out = new ArrayList<>();
        if (workspace != null && !workspace.isEmpty()) {
            out.add("--workspace");
            out.add(workspace);
        }
        if (wizard) {
            out.add("--wizard");
        }
        if (nonInteractive) {
            out.add("--non-interactive");
        }
        if (mode != null) {
            out.add("--mode");
            out.add(mode == SetupMode.LOCAL ? "local" : "remote");
        }
        if (remoteUrl != null && !remoteUrl.isEmpty()) {
            out.add("--remote-url");
            out.add(remoteUrl);
        }
        if (remoteToken != null && !remoteToken.isEmpty()) {
            out.add("--remote-token");
            out.add(remoteToken);
        }
        return Collections.unmodifiableList(out);
    }

    /**
     * 构建器：仅设置需要的选项，未设置的 flag 不会出现在命令行中。
     */
    public static final class Builder {

        private String workspace;
        private boolean wizard;
        private boolean nonInteractive;
        private SetupMode mode;
        private String remoteUrl;
        private String remoteToken;

        /**
         * {@code --workspace &lt;dir&gt;}：agent workspace（写入 {@code agents.defaults.workspace}）。
         */
        public Builder workspace(String workspace) {
            this.workspace = workspace;
            return this;
        }

        /** {@code --wizard}：运行 onboarding。 */
        public Builder wizard(boolean wizard) {
            this.wizard = wizard;
            return this;
        }

        /** {@code --non-interactive}：无提示 onboarding。 */
        public Builder nonInteractive(boolean nonInteractive) {
            this.nonInteractive = nonInteractive;
            return this;
        }

        /** {@code --mode local|remote}。 */
        public Builder mode(SetupMode mode) {
            this.mode = mode;
            return this;
        }

        /** {@code --remote-url &lt;url&gt;}：远程 Gateway WebSocket URL。 */
        public Builder remoteUrl(String remoteUrl) {
            this.remoteUrl = remoteUrl;
            return this;
        }

        /** {@code --remote-token &lt;token&gt;}。 */
        public Builder remoteToken(String remoteToken) {
            this.remoteToken = remoteToken;
            return this;
        }

        /**
         * @return 不可变 {@link SetupOptions}
         */
        public SetupOptions build() {
            return new SetupOptions(this);
        }
    }
}
