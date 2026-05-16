package io.github.hiwepy.openclaw.cli.opts;

import io.github.hiwepy.openclaw.util.OpenClawLists;
import io.github.hiwepy.openclaw.util.OpenClawStrings;
import io.github.hiwepy.openclaw.cli.args.CliSubArgs;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * {@code openclaw backup}：将状态目录、活动配置、凭据目录、会话与可选 workspace 打成本地 {@code .tar.gz}，并支持归档校验。
 * <p>归档内含 {@code manifest.json}；默认文件名带时间戳且不覆盖已存在文件；工作区很大时可用 {@code --no-include-workspace} 或 {@code --only-config}。</p>
 *
 * @see <a href="https://docs.openclaw.ai/cli/backup">backup CLI</a>
 */
public final class BackupOptions implements CliSubArgs {

    /**
     * backup 子命令：创建归档或校验既有归档。
     */
    public enum Mode {
        /** {@code backup create}：打包当前安装可解析的数据源。 */
        CREATE,
        /** {@code backup verify}：校验 tarball 与 manifest 完整性。 */
        VERIFY
    }

    /** {@code create} 或 {@code verify}。 */
    private final Mode mode;
    /**
     * create：{@code --output} 指定目录或文件路径前缀（文档：默认在当前目录或 home 下落盘，避免自包含）。
     */
    private final String outputDir;
    /**
     * create：{@code --dry-run} 只规划来源不写盘（与 {@code --json} 组合见文档示例）。
     */
    private final boolean dryRun;
    /**
     * create：{@code --json} 机器可读输出计划或结果。
     */
    private final boolean json;
    /**
     * create：{@code --verify} 写入后立即跑与 {@code backup verify} 相同的校验。
     */
    private final boolean verifyAfterCreate;
    /**
     * create：{@code --no-include-workspace} 跳过配置推导的 workspace 树（配置无效但仍想备份状态时常用）。
     */
    private final boolean noIncludeWorkspace;
    /**
     * create：{@code --only-config} 只归档活动 JSON 配置文件本身。
     */
    private final boolean onlyConfig;
    /**
     * verify：待校验的 {@code .tar.gz} 归档路径（位置参数）。
     */
    private final String verifyArchivePath;
    /**
     * 其它未建模 argv。
     */
    private final List<String> extra;

    /**
     * @param b 构建器快照
     */
    private BackupOptions(Builder b) {
        this.mode = b.mode;
        this.outputDir = b.outputDir;
        this.dryRun = b.dryRun;
        this.json = b.json;
        this.verifyAfterCreate = b.verifyAfterCreate;
        this.noIncludeWorkspace = b.noIncludeWorkspace;
        this.onlyConfig = b.onlyConfig;
        this.verifyArchivePath = b.verifyArchivePath;
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
        if (mode == Mode.CREATE) {
            out.add("create");
            OpenClawCliArgv.addIfPresent(out, "--output", outputDir);
            OpenClawCliArgv.addFlag(out, "--dry-run", dryRun);
            OpenClawCliArgv.addFlag(out, "--json", json);
            OpenClawCliArgv.addFlag(out, "--verify", verifyAfterCreate);
            OpenClawCliArgv.addFlag(out, "--no-include-workspace", noIncludeWorkspace);
            OpenClawCliArgv.addFlag(out, "--only-config", onlyConfig);
        } else {
            out.add("verify");
            if (verifyArchivePath != null && OpenClawStrings.isNotBlank(verifyArchivePath)) {
                out.add(verifyArchivePath.trim());
            }
        }
        OpenClawCliArgv.addExtra(out, extra);
        return Collections.unmodifiableList(out);
    }

    /**
     * {@link BackupOptions} 构建器。
     */
    public static final class Builder {
        private Mode mode = Mode.CREATE;
        private String outputDir;
        private boolean dryRun;
        private boolean json;
        private boolean verifyAfterCreate;
        private boolean noIncludeWorkspace;
        private boolean onlyConfig;
        private String verifyArchivePath;
        private List<String> extra = new ArrayList<>();

        /**
         * @return {@code this}（{@code backup create}）
         */
        public Builder create() {
            this.mode = Mode.CREATE;
            return this;
        }

        /**
         * @param outputDirOrFile create：{@code --output}
         * @return {@code this}
         */
        public Builder output(String outputDirOrFile) {
            this.outputDir = outputDirOrFile;
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
         * @param json {@code --json}
         * @return {@code this}
         */
        public Builder json(boolean json) {
            this.json = json;
            return this;
        }

        /**
         * @param verify create：{@code --verify}
         * @return {@code this}
         */
        public Builder verifyAfterCreate(boolean verify) {
            this.verifyAfterCreate = verify;
            return this;
        }

        /**
         * @param noWorkspace {@code --no-include-workspace}
         * @return {@code this}
         */
        public Builder noIncludeWorkspace(boolean noWorkspace) {
            this.noIncludeWorkspace = noWorkspace;
            return this;
        }

        /**
         * @param onlyConfig {@code --only-config}
         * @return {@code this}
         */
        public Builder onlyConfig(boolean onlyConfig) {
            this.onlyConfig = onlyConfig;
            return this;
        }

        /**
         * @param archivePath verify：归档路径
         * @return {@code this}
         */
        public Builder verify(String archivePath) {
            this.mode = Mode.VERIFY;
            this.verifyArchivePath = archivePath;
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
         * @return 不可变 {@link BackupOptions}
         */
        public BackupOptions build() {
            return new BackupOptions(this);
        }
    }
}
