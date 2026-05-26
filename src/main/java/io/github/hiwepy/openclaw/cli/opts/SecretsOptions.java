package io.github.hiwepy.openclaw.cli.opts;

import io.github.hiwepy.openclaw.util.OpenClawLists;
import io.github.hiwepy.openclaw.cli.args.CliSubArgs;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * {@code openclaw secrets}：管理 SecretRef、刷新网关运行时密钥快照，并对配置做审计与（交互）迁移计划应用。
 * <p>{@code reload} 走 {@code secrets.reload} RPC；{@code audit --check} 用于 CI 门槛；含 exec 提供方的计划须在 dry-run 与写入阶段都带 {@code --allow-exec}。</p>
 *
 * @see <a href="https://docs.openclaw.ai/cli/secrets">secrets CLI</a>
 */
public final class SecretsOptions implements CliSubArgs {

    /**
     * secrets 子命令：重载运行时、只读审计、交互配置或执行已保存计划。
     */
    public enum Mode {
        /** {@code secrets reload}：重新解析 SecretRef 并在全量成功时原子切换运行时快照。 */
        RELOAD,
        /** {@code secrets audit}：扫描明文、未解析引用与影子配置等。 */
        AUDIT,
        /** {@code secrets configure}：TTY 交互生成计划并可选预检后应用。 */
        CONFIGURE,
        /** {@code secrets apply}：对既有 JSON 计划做 dry-run 或写入并清理残留明文。 */
        APPLY
    }

    /** reload / audit / configure / apply 之一。 */
    private final Mode mode;
    /**
     * reload：{@code --url} 网关 WebSocket 地址（共享 gateway 查询选项语义）。
     */
    private final String gatewayUrl;
    /**
     * reload：{@code --token} 网关 token（与文档其它 gateway RPC 命令一致）。
     */
    private final String gatewayToken;
    /**
     * reload：{@code --timeout} RPC 超时。
     */
    private final String timeout;
    /**
     * 各子命令：{@code --json} 结构化输出（configure 在 TTY 前提下仍可打印计划 JSON）。
     */
    private final boolean json;
    /**
     * audit：{@code --check} 发现项时以非零退出（与未解析引用优先级见文档）。
     */
    private final boolean auditCheck;
    /**
     * configure / apply：{@code --allow-exec} 允许执行类 SecretRef 提供方在预检或写入时运行命令。
     */
    private final boolean allowExec;
    /**
     * configure：{@code --plan-out} 将计划写入路径以便后续 {@code apply --from}。
     */
    private final String planOut;
    /**
     * configure：{@code --apply} 预检后直接应用（仍可能触发额外确认，除非配合 {@code --yes}）。
     */
    private final boolean configureApply;
    /**
     * configure：{@code --yes} 跳过部分确认提示。
     */
    private final boolean yes;
    /**
     * configure：{@code --providers-only} 只配置 {@code secrets.providers}，不做凭据映射。
     */
    private final boolean providersOnly;
    /**
     * configure：{@code --skip-provider-setup} 跳过提供方新增步骤，只做映射。
     */
    private final boolean skipProviderSetup;
    /**
     * configure：{@code --agent} 限定 {@code auth-profiles.json} 的发现与写入范围。
     */
    private final String agent;
    /**
     * apply：{@code --from} 计划文件路径。
     */
    private final String applyFrom;
    /**
     * apply：{@code --dry-run} 只做校验与预检，不写文件；默认跳过 exec 探测除非 {@code --allow-exec}。
     */
    private final boolean dryRun;
    /**
     * 其它 argv。
     */
    private final List<String> extra;

    /**
     * @param b 构建器快照
     */
    private SecretsOptions(Builder b) {
        this.mode = b.mode;
        this.gatewayUrl = b.gatewayUrl;
        this.gatewayToken = b.gatewayToken;
        this.timeout = b.timeout;
        this.json = b.json;
        this.auditCheck = b.auditCheck;
        this.allowExec = b.allowExec;
        this.planOut = b.planOut;
        this.configureApply = b.configureApply;
        this.yes = b.yes;
        this.providersOnly = b.providersOnly;
        this.skipProviderSetup = b.skipProviderSetup;
        this.agent = b.agent;
        this.applyFrom = b.applyFrom;
        this.dryRun = b.dryRun;
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
            case RELOAD:
                out.add("reload");
                OpenClawCliArgv.addIfPresent(out, "--url", gatewayUrl);
                OpenClawCliArgv.addIfPresent(out, "--token", gatewayToken);
                OpenClawCliArgv.addIfPresent(out, "--timeout", timeout);
                OpenClawCliArgv.addFlag(out, "--json", json);
                break;
            case AUDIT:
                out.add("audit");
                OpenClawCliArgv.addFlag(out, "--check", auditCheck);
                OpenClawCliArgv.addFlag(out, "--json", json);
                OpenClawCliArgv.addFlag(out, "--allow-exec", allowExec);
                break;
            case CONFIGURE:
                out.add("configure");
                OpenClawCliArgv.addIfPresent(out, "--plan-out", planOut);
                OpenClawCliArgv.addFlag(out, "--apply", configureApply);
                OpenClawCliArgv.addFlag(out, "--yes", yes);
                OpenClawCliArgv.addFlag(out, "--providers-only", providersOnly);
                OpenClawCliArgv.addFlag(out, "--skip-provider-setup", skipProviderSetup);
                OpenClawCliArgv.addIfPresent(out, "--agent", agent);
                OpenClawCliArgv.addFlag(out, "--json", json);
                OpenClawCliArgv.addFlag(out, "--allow-exec", allowExec);
                break;
            case APPLY:
                out.add("apply");
                OpenClawCliArgv.addIfPresent(out, "--from", applyFrom);
                OpenClawCliArgv.addFlag(out, "--dry-run", dryRun);
                OpenClawCliArgv.addFlag(out, "--allow-exec", allowExec);
                OpenClawCliArgv.addFlag(out, "--json", json);
                break;
            default:
                break;
        }
        OpenClawCliArgv.addExtra(out, extra);
        return Collections.unmodifiableList(out);
    }

    /**
     * {@link SecretsOptions} 构建器。
     */
    public static final class Builder {
        private Mode mode = Mode.AUDIT;
        private String gatewayUrl;
        private String gatewayToken;
        private String timeout;
        private boolean json;
        private boolean auditCheck;
        private boolean allowExec;
        private String planOut;
        private boolean configureApply;
        private boolean yes;
        private boolean providersOnly;
        private boolean skipProviderSetup;
        private String agent;
        private String applyFrom;
        private boolean dryRun;
        private List<String> extra = new ArrayList<>();

        /**
         * @return {@code this}（{@code secrets reload}）
         */
        public Builder reload() {
            this.mode = Mode.RELOAD;
            return this;
        }

        /**
         * @param url reload：{@code --url}
         * @return {@code this}
         */
        public Builder gatewayUrl(String url) {
            this.gatewayUrl = url;
            return this;
        }

        /**
         * @param token reload：{@code --token}
         * @return {@code this}
         */
        public Builder gatewayToken(String token) {
            this.gatewayToken = token;
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
         * @return {@code this}（{@code secrets audit}）
         */
        public Builder audit() {
            this.mode = Mode.AUDIT;
            return this;
        }

        /**
         * @param check audit：{@code --check}
         * @return {@code this}
         */
        public Builder auditCheck(boolean check) {
            this.auditCheck = check;
            return this;
        }

        /**
         * @return {@code this}（{@code secrets configure}）
         */
        public Builder configure() {
            this.mode = Mode.CONFIGURE;
            return this;
        }

        /**
         * @param path configure：{@code --plan-out}
         * @return {@code this}
         */
        public Builder planOut(String path) {
            this.planOut = path;
            return this;
        }

        /**
         * @param apply configure：{@code --apply}
         * @return {@code this}
         */
        public Builder configureApply(boolean apply) {
            this.configureApply = apply;
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
         * @param providersOnly {@code --providers-only}
         * @return {@code this}
         */
        public Builder providersOnly(boolean providersOnly) {
            this.providersOnly = providersOnly;
            return this;
        }

        /**
         * @param skip {@code --skip-provider-setup}
         * @return {@code this}
         */
        public Builder skipProviderSetup(boolean skip) {
            this.skipProviderSetup = skip;
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
         * @param planPath apply：{@code --from}（计划路径）
         * @return {@code this}
         */
        public Builder apply(String planPath) {
            this.mode = Mode.APPLY;
            this.applyFrom = planPath;
            return this;
        }

        /**
         * @param dryRun apply：{@code --dry-run}
         * @return {@code this}
         */
        public Builder dryRun(boolean dryRun) {
            this.dryRun = dryRun;
            return this;
        }

        /**
         * @param allowExec {@code --allow-exec}
         * @return {@code this}
         */
        public Builder allowExec(boolean allowExec) {
            this.allowExec = allowExec;
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
         * @return 不可变 {@link SecretsOptions}
         */
        public SecretsOptions build() {
            return new SecretsOptions(this);
        }
    }
}
