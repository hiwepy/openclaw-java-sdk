package com.github.hiwepy.openclaw.cli.opts;

import com.github.hiwepy.openclaw.cli.args.CliSubArgs;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * {@code openclaw reset}：重置本地配置与状态目录（不卸载 CLI）；破坏性操作前建议先 {@code openclaw backup create}。
 * <p>省略 {@code --scope} 时进入交互选择清除范围；{@code --non-interactive} 必须同时提供 {@code --scope} 与 {@code --yes}。</p>
 *
 * @see <a href="https://docs.openclaw.ai/cli/reset">reset CLI</a>
 */
public final class ResetOptions implements CliSubArgs {

    /**
     * {@code --scope} 取值，与 reset 文档一致。
     */
    public enum Scope {
        /** 仅重置配置相关范围 {@code config}。 */
        CONFIG("config"),
        /** 配置加凭据与会话等 {@code config+creds+sessions}。 */
        CONFIG_CREDS_SESSIONS("config+creds+sessions"),
        /** 完整重置范围 {@code full}。 */
        FULL("full");

        private final String cliValue;

        Scope(String cliValue) {
            this.cliValue = cliValue;
        }

        String cliValue() {
            return cliValue;
        }
    }

    /**
     * {@code --scope}：非交互模式下指定清除深度；{@code null} 表示不传该 flag（走交互或默认）。
     */
    private final Scope scope;
    /**
     * {@code --yes}：跳过交互确认。
     */
    private final boolean yes;
    /**
     * {@code --non-interactive}：禁止一切提示；文档要求与 {@code --scope}、{@code --yes} 同用。
     */
    private final boolean nonInteractive;
    /**
     * {@code --dry-run}：只打印将执行的动作，不删除文件。
     */
    private final boolean dryRun;
    /**
     * 文档未单独建模的附加 argv。
     */
    private final List<String> extra;

    /**
     * @param b 构建器快照
     */
    private ResetOptions(Builder b) {
        this.scope = b.scope;
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
        if (scope != null) {
            out.add("--scope");
            out.add(scope.cliValue());
        }
        OpenClawCliArgv.addFlag(out, "--yes", yes);
        OpenClawCliArgv.addFlag(out, "--non-interactive", nonInteractive);
        OpenClawCliArgv.addFlag(out, "--dry-run", dryRun);
        OpenClawCliArgv.addExtra(out, extra);
        return Collections.unmodifiableList(out);
    }

    /**
     * {@link ResetOptions} 构建器。
     */
    public static final class Builder {
        private Scope scope;
        private boolean yes;
        private boolean nonInteractive;
        private boolean dryRun;
        private List<String> extra = new ArrayList<>();

        /**
         * @param scope {@code --scope}
         * @return {@code this}
         */
        public Builder scope(Scope scope) {
            this.scope = scope;
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
         * @return 不可变 {@link ResetOptions}
         */
        public ResetOptions build() {
            return new ResetOptions(this);
        }
    }
}
