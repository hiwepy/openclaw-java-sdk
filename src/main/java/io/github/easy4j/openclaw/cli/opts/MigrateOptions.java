package io.github.easy4j.openclaw.cli.opts;

import io.github.easy4j.openclaw.util.OpenClawLists;
import io.github.easy4j.openclaw.cli.args.CliSubArgs;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * {@code openclaw migrate}：从其它 agent 系统导入状态。
 * <p>
 * 支持 {@code list}、{@code plan <provider>}、{@code apply <provider>} 子命令以及默认动作
 * {@code migrate [provider]}。共享选项通过 {@code addMigrationOptions} 注入；{@code apply} 与默认动作
 * 额外支持 {@code --yes}/{@code --backup-output}/{@code --no-backup}/{@code --force}/{@code --dry-run}。
 * </p>
 *
 * @see <a href="https://docs.openclaw.ai/cli/migrate">migrate CLI</a>
 */
public final class MigrateOptions implements CliSubArgs {

    /** 子命令模式。 */
    public enum Mode {
        /** 默认动作 {@code migrate [provider]}：预览并可选应用。 */
        DEFAULT,
        /** {@code list}：列出可用的迁移提供者。 */
        LIST,
        /** {@code plan <provider>}：仅预览，不应用。 */
        PLAN,
        /** {@code apply <provider>}：应用迁移。 */
        APPLY
    }

    private final Mode mode;
    /** 迁移提供者 ID（如 {@code hermes}）。 */
    private final String provider;
    /** {@code --from}：源目录。 */
    private final String from;
    /** {@code --include-secrets}：导入受支持的凭证与机密。 */
    private final boolean includeSecrets;
    /** {@code --no-auth-credentials}：跳过 auth 凭证迁移（Commander 否定标志）。 */
    private final boolean noAuthCredentials;
    /** {@code --overwrite}：在条目级备份后覆盖冲突的目标文件。 */
    private final boolean overwrite;
    /** {@code --dry-run}：仅预览，不应用变更。 */
    private final boolean dryRun;
    /** {@code --yes}：预览后无需提示直接应用。 */
    private final boolean yes;
    /** {@code --skill}（可重复）：按名称或条目 id 选择一个技能迁移。 */
    private final List<String> skills;
    /** {@code --plugin}（可重复）：按名称或条目 id 选择一个 Codex 插件迁移。 */
    private final List<String> plugins;
    /** {@code --backup-output}：迁移前备份归档路径或目录。 */
    private final String backupOutput;
    /** {@code --no-backup}：跳过迁移前的 OpenClaw 备份。 */
    private final boolean noBackup;
    /** {@code --force}：允许危险选项如 {@code --no-backup}。 */
    private final boolean force;
    /** {@code --verify-plugin-apps}：Codex 专用：在规划原生插件激活前用 app/list 校验源插件 app 可达性。 */
    private final boolean verifyPluginApps;
    /** {@code --json}：JSON 输出。 */
    private final boolean json;

    private MigrateOptions(Builder b) {
        this.mode = b.mode;
        this.provider = b.provider;
        this.from = b.from;
        this.includeSecrets = b.includeSecrets;
        this.noAuthCredentials = b.noAuthCredentials;
        this.overwrite = b.overwrite;
        this.dryRun = b.dryRun;
        this.yes = b.yes;
        this.skills = OpenClawLists.copyOf(b.skills);
        this.plugins = OpenClawLists.copyOf(b.plugins);
        this.backupOutput = b.backupOutput;
        this.noBackup = b.noBackup;
        this.force = b.force;
        this.verifyPluginApps = b.verifyPluginApps;
        this.json = b.json;
    }

    /**
     * @return 新 {@link Builder}（默认 {@link Mode#DEFAULT}）
     */
    public static Builder builder() {
        return new Builder();
    }

    @Override
    public List<String> toSubcommandArguments() {
        List<String> out = new ArrayList<>();
        switch (mode) {
            case LIST:
                out.add("list");
                break;
            case PLAN:
                out.add("plan");
                if (provider != null && !provider.isEmpty()) {
                    out.add(provider);
                }
                break;
            case APPLY:
                out.add("apply");
                if (provider != null && !provider.isEmpty()) {
                    out.add(provider);
                }
                break;
            case DEFAULT:
            default:
                // 默认动作：可选后接 provider 位置参数
                if (provider != null && !provider.isEmpty()) {
                    out.add(provider);
                }
                break;
        }
        OpenClawCliArgv.addIfPresent(out, "--from", from);
        OpenClawCliArgv.addFlag(out, "--include-secrets", includeSecrets);
        OpenClawCliArgv.addFlag(out, "--no-auth-credentials", noAuthCredentials);
        OpenClawCliArgv.addFlag(out, "--overwrite", overwrite);
        // --dry-run / --yes / --backup-output / --no-backup / --force 仅在 DEFAULT 与 APPLY 下生效
        if (mode == Mode.DEFAULT || mode == Mode.APPLY) {
            OpenClawCliArgv.addFlag(out, "--dry-run", dryRun);
            OpenClawCliArgv.addFlag(out, "--yes", yes);
            OpenClawCliArgv.addIfPresent(out, "--backup-output", backupOutput);
            OpenClawCliArgv.addFlag(out, "--no-backup", noBackup);
            OpenClawCliArgv.addFlag(out, "--force", force);
        }
        OpenClawCliArgv.addRepeatable(out, "--skill", skills);
        OpenClawCliArgv.addRepeatable(out, "--plugin", plugins);
        OpenClawCliArgv.addFlag(out, "--verify-plugin-apps", verifyPluginApps);
        OpenClawCliArgv.addFlag(out, "--json", json);
        return Collections.unmodifiableList(out);
    }

    /**
     * {@link MigrateOptions} 构建器。
     */
    public static final class Builder {
        private Mode mode = Mode.DEFAULT;
        private String provider;
        private String from;
        private boolean includeSecrets;
        private boolean noAuthCredentials;
        private boolean overwrite;
        private boolean dryRun;
        private boolean yes;
        private List<String> skills;
        private List<String> plugins;
        private String backupOutput;
        private boolean noBackup;
        private boolean force;
        private boolean verifyPluginApps;
        private boolean json;

        /** 切换为 {@code list} 子命令。 */
        public Builder list() { this.mode = Mode.LIST; return this; }
        /** 切换为 {@code plan <provider>} 子命令。 */
        public Builder plan(String provider) { this.mode = Mode.PLAN; this.provider = provider; return this; }
        /** 切换为 {@code apply <provider>} 子命令。 */
        public Builder apply(String provider) { this.mode = Mode.APPLY; this.provider = provider; return this; }
        /** 默认动作 {@code migrate [provider]}。 */
        public Builder defaultAction(String provider) { this.mode = Mode.DEFAULT; this.provider = provider; return this; }
        /** 显式指定 {@link Mode}。 */
        public Builder mode(Mode mode) { this.mode = mode; return this; }
        /** 迁移提供者 ID（如 {@code hermes}）。 */
        public Builder provider(String provider) { this.provider = provider; return this; }
        /** {@code --from}：源目录。 */
        public Builder from(String from) { this.from = from; return this; }
        /** {@code --include-secrets}：导入受支持的凭证与机密。 */
        public Builder includeSecrets(boolean includeSecrets) { this.includeSecrets = includeSecrets; return this; }
        /** {@code --no-auth-credentials}：跳过 auth 凭证迁移。 */
        public Builder noAuthCredentials(boolean noAuthCredentials) { this.noAuthCredentials = noAuthCredentials; return this; }
        /** {@code --overwrite}：覆盖冲突的目标文件。 */
        public Builder overwrite(boolean overwrite) { this.overwrite = overwrite; return this; }
        /** {@code --dry-run}：仅预览。 */
        public Builder dryRun(boolean dryRun) { this.dryRun = dryRun; return this; }
        /** {@code --yes}：预览后无需提示直接应用。 */
        public Builder yes(boolean yes) { this.yes = yes; return this; }
        /** {@code --skill}（可重复）：技能名称或条目 id。 */
        public Builder skills(List<String> skills) { this.skills = skills; return this; }
        /** {@code --plugin}（可重复）：Codex 插件名称或条目 id。 */
        public Builder plugins(List<String> plugins) { this.plugins = plugins; return this; }
        /** {@code --backup-output}：迁移前备份归档路径。 */
        public Builder backupOutput(String backupOutput) { this.backupOutput = backupOutput; return this; }
        /** {@code --no-backup}：跳过迁移前的 OpenClaw 备份。 */
        public Builder noBackup(boolean noBackup) { this.noBackup = noBackup; return this; }
        /** {@code --force}：允许危险选项。 */
        public Builder force(boolean force) { this.force = force; return this; }
        /** {@code --verify-plugin-apps}：Codex 专用，校验源插件 app 可达性。 */
        public Builder verifyPluginApps(boolean verifyPluginApps) { this.verifyPluginApps = verifyPluginApps; return this; }
        /** {@code --json}：JSON 输出。 */
        public Builder json(boolean json) { this.json = json; return this; }

        /**
         * @return 不可变 {@link MigrateOptions}
         */
        public MigrateOptions build() {
            return new MigrateOptions(this);
        }
    }
}
