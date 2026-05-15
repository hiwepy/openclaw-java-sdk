package io.github.hiwepy.openclaw.cli.opts;

import io.github.hiwepy.openclaw.cli.args.CliSubArgs;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * {@code openclaw completion}：生成 shell 补全脚本，可选安装到 profile 或写入 state 目录。
 * <p>无 {@code --install} 且无 {@code --write-state} 时脚本打印到 stdout；{@code --install} 会在 profile 中写入指向缓存脚本的 source 块。</p>
 *
 * @see <a href="https://docs.openclaw.ai/cli/completion">completion CLI</a>
 */
public final class CompletionOptions implements CliSubArgs {

    /**
     * {@code --shell} 目标（文档列出的取值）。
     */
    public enum Shell {
        ZSH("zsh"),
        BASH("bash"),
        POWERSHELL("powershell"),
        FISH("fish");

        /** 传给 {@code --shell} 的 CLI 字面量。 */
        private final String cliValue;

        /**
         * @param cliValue 非 null shell 名
         */
        Shell(String cliValue) {
            this.cliValue = cliValue;
        }

        /**
         * @return 与 CLI 一致的 shell token
         */
        String cliValue() {
            return cliValue;
        }
    }

    /**
     * {@code -s} / {@code --shell}：目标 shell（文档：{@code zsh}、{@code bash}、{@code powershell}、{@code fish}；默认 {@code zsh}）。
     */
    private final Shell shell;
    /**
     * {@code -i} / {@code --install}：向 shell profile 追加 source 行以启用补全。
     */
    private final boolean install;
    /**
     * {@code --write-state}：将补全脚本写入 {@code $OPENCLAW_STATE_DIR/completions}，不打印到 stdout。
     */
    private final boolean writeState;
    /**
     * {@code -y} / {@code --yes}：安装时跳过确认提示。
     */
    private final boolean yes;
    /**
     * 其它 argv 片段。
     */
    private final List<String> extra;

    /**
     * @param b 构建器快照
     */
    private CompletionOptions(Builder b) {
        this.shell = b.shell;
        this.install = b.install;
        this.writeState = b.writeState;
        this.yes = b.yes;
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
        if (shell != null) {
            out.add("--shell");
            out.add(shell.cliValue());
        }
        OpenClawCliArgv.addFlag(out, "--install", install);
        OpenClawCliArgv.addFlag(out, "--write-state", writeState);
        OpenClawCliArgv.addFlag(out, "--yes", yes);
        OpenClawCliArgv.addExtra(out, extra);
        return Collections.unmodifiableList(out);
    }

    /**
     * {@link CompletionOptions} 构建器。
     */
    public static final class Builder {
        private Shell shell;
        private boolean install;
        private boolean writeState;
        private boolean yes;
        private List<String> extra = new ArrayList<>();

        /**
         * @param shell {@code --shell}
         * @return {@code this}
         */
        public Builder shell(Shell shell) {
            this.shell = shell;
            return this;
        }

        /**
         * @param install {@code --install}
         * @return {@code this}
         */
        public Builder install(boolean install) {
            this.install = install;
            return this;
        }

        /**
         * @param writeState {@code --write-state}
         * @return {@code this}
         */
        public Builder writeState(boolean writeState) {
            this.writeState = writeState;
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
         * @return 不可变 {@link CompletionOptions}
         */
        public CompletionOptions build() {
            return new CompletionOptions(this);
        }
    }
}
