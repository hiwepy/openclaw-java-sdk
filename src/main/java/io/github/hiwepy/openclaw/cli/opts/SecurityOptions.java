package io.github.hiwepy.openclaw.cli.opts;

import io.github.hiwepy.openclaw.cli.args.CliSubArgs;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * {@code openclaw security}：安全审计工具，可选应用文档所列的确定性修复（{@code audit --fix}）。
 * <p>{@code --token}/{@code --password} 仅覆盖本次深度探测认证，不写回配置；{@code --deep} 扩大检查面；{@code --json} 适合 CI/策略门禁。</p>
 *
 * @see <a href="https://docs.openclaw.ai/cli/security">security CLI</a>
 */
public final class SecurityOptions implements CliSubArgs {

    /** 顶层子命令族（当前实现 {@code audit}）。 */
    public enum Mode {
        /** {@code security audit} */
        AUDIT
    }

    /**
     * 顶层模式；当前实现为 {@link Mode#AUDIT}（{@code security audit}）。
     */
    private final Mode mode;
    /**
     * {@code --deep}：启用更深的安全检查与探测（文档示例与 CI 过滤配合）。
     */
    private final boolean deep;
    /**
     * {@code --password}：深度探测时使用的网关密码（一次性覆盖，不修改 SecretRef/配置文件）。
     */
    private final String password;
    /**
     * {@code --token}：深度探测时使用的 token（同上，仅当次命令）。
     */
    private final String token;
    /**
     * {@code --fix}：应用文档列出的安全加固项（如 groupPolicy、日志脱敏、关键文件权限等）；不轮换密钥、不禁用工具。
     */
    private final boolean fix;
    /**
     * {@code --json}：结构化报告（可与 {@code --fix} 同时输出修复结果与最终摘要）。
     */
    private final boolean json;
    /**
     * 其它参数。
     */
    private final List<String> extra;

    /**
     * @param b 构建器快照
     */
    private SecurityOptions(Builder b) {
        this.mode = b.mode;
        this.deep = b.deep;
        this.password = b.password;
        this.token = b.token;
        this.fix = b.fix;
        this.json = b.json;
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
        if (mode == Mode.AUDIT) {
            out.add("audit");
            OpenClawCliArgv.addFlag(out, "--deep", deep);
            OpenClawCliArgv.addIfPresent(out, "--password", password);
            OpenClawCliArgv.addIfPresent(out, "--token", token);
            OpenClawCliArgv.addFlag(out, "--fix", fix);
            OpenClawCliArgv.addFlag(out, "--json", json);
        }
        OpenClawCliArgv.addExtra(out, extra);
        return Collections.unmodifiableList(out);
    }

    /**
     * {@link SecurityOptions} 构建器。
     */
    public static final class Builder {
        private Mode mode = Mode.AUDIT;
        private boolean deep;
        private String password;
        private String token;
        private boolean fix;
        private boolean json;
        private List<String> extra = new ArrayList<>();

        /**
         * @return {@code this}，选择 {@link Mode#AUDIT}
         */
        public Builder audit() {
            this.mode = Mode.AUDIT;
            return this;
        }

        /**
         * @param deep {@code --deep}
         * @return {@code this}
         */
        public Builder deep(boolean deep) {
            this.deep = deep;
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
         * @param token {@code --token}
         * @return {@code this}
         */
        public Builder token(String token) {
            this.token = token;
            return this;
        }

        /**
         * @param fix {@code --fix}
         * @return {@code this}
         */
        public Builder fix(boolean fix) {
            this.fix = fix;
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
         * @return 不可变 {@link SecurityOptions}
         */
        public SecurityOptions build() {
            return new SecurityOptions(this);
        }
    }
}
