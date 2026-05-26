package io.github.hiwepy.openclaw.cli.opts;

import io.github.hiwepy.openclaw.util.OpenClawLists;
import io.github.hiwepy.openclaw.cli.args.CliSubArgs;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * {@code openclaw sessions}：列出各 agent 存储的会话记录，并按配置执行会话存储维护（裁剪、缺失修复等）。
 * <p>{@code cleanup} 使用 {@code session.maintenance} 设置；不会清理 cron 运行日志（见 cron 文档）。{@code --all-agents} 聚合所有已配置 agent store。</p>
 *
 * @see <a href="https://docs.openclaw.ai/cli/sessions">sessions CLI</a>
 */
public final class SessionsOptions implements CliSubArgs {

    /**
     * 默认列出会话，或进入 {@code sessions cleanup} 维护子命令。
     */
    public enum Mode {
        /**
         * 默认列出：无子命令 token，与文档示例 {@code openclaw sessions} 一致。
         */
        LIST,
        /** {@code sessions cleanup}：立即执行维护而非等待下次写入周期。 */
        CLEANUP
    }

    /** LIST 或 CLEANUP。 */
    private final Mode mode;
    /**
     * {@code --agent}：限定到单个已配置 agent 的会话 store。
     */
    private final String agent;
    /**
     * {@code --all-agents}：跨所有已配置 agent 聚合列出或清理。
     */
    private final boolean allAgents;
    /**
     * list：{@code --active} 最近若干分钟内活跃的会话过滤（分钟值）。
     */
    private final Integer activeMinutes;
    /**
     * list：{@code --verbose} 更详细日志。
     */
    private final boolean verbose;
    /**
     * list：{@code --json} 输出会话条目与 store 元数据。
     */
    private final boolean json;
    /**
     * list / cleanup：{@code --store} 显式指向某个 {@code sessions.json} 文件（不可与 {@code --agent}/{@code --all-agents} 混用，见文档）。
     */
    private final String store;
    /**
     * cleanup：{@code --dry-run} 只打印将删除或裁剪的条目，不写盘。
     */
    private final boolean cleanupDryRun;
    /**
     * cleanup：{@code --enforce} 即使 {@code session.maintenance.mode=warn} 也执行维护。
     */
    private final boolean cleanupEnforce;
    /**
     * cleanup：{@code --fix-missing} 删除 transcript 文件已缺失的僵尸索引项。
     */
    private final boolean cleanupFixMissing;
    /**
     * cleanup：{@code --active-key} 保护指定会话键不被磁盘预算驱逐。
     */
    private final String cleanupActiveKey;
    /**
     * cleanup：{@code --json} 输出每 store 前后计数等摘要。
     */
    private final boolean cleanupJson;
    /**
     * 其它 argv。
     */
    private final List<String> extra;

    /**
     * @param b 构建器快照
     */
    private SessionsOptions(Builder b) {
        this.mode = b.mode;
        this.agent = b.agent;
        this.allAgents = b.allAgents;
        this.activeMinutes = b.activeMinutes;
        this.verbose = b.verbose;
        this.json = b.json;
        this.store = b.store;
        this.cleanupDryRun = b.cleanupDryRun;
        this.cleanupEnforce = b.cleanupEnforce;
        this.cleanupFixMissing = b.cleanupFixMissing;
        this.cleanupActiveKey = b.cleanupActiveKey;
        this.cleanupJson = b.cleanupJson;
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
        if (mode == Mode.CLEANUP) {
            out.add("cleanup");
            OpenClawCliArgv.addFlag(out, "--dry-run", cleanupDryRun);
            OpenClawCliArgv.addFlag(out, "--enforce", cleanupEnforce);
            OpenClawCliArgv.addFlag(out, "--fix-missing", cleanupFixMissing);
            OpenClawCliArgv.addIfPresent(out, "--active-key", cleanupActiveKey);
            OpenClawCliArgv.addIfPresent(out, "--agent", agent);
            OpenClawCliArgv.addFlag(out, "--all-agents", allAgents);
            OpenClawCliArgv.addIfPresent(out, "--store", store);
            OpenClawCliArgv.addFlag(out, "--json", cleanupJson);
        } else {
            OpenClawCliArgv.addIfPresent(out, "--agent", agent);
            OpenClawCliArgv.addFlag(out, "--all-agents", allAgents);
            OpenClawCliArgv.addIfNotNull(out, "--active", activeMinutes);
            OpenClawCliArgv.addFlag(out, "--verbose", verbose);
            OpenClawCliArgv.addFlag(out, "--json", json);
            OpenClawCliArgv.addIfPresent(out, "--store", store);
        }
        OpenClawCliArgv.addExtra(out, extra);
        return Collections.unmodifiableList(out);
    }

    /**
     * {@link SessionsOptions} 构建器。
     */
    public static final class Builder {
        private Mode mode = Mode.LIST;
        private String agent;
        private boolean allAgents;
        private Integer activeMinutes;
        private boolean verbose;
        private boolean json;
        private String store;
        private boolean cleanupDryRun;
        private boolean cleanupEnforce;
        private boolean cleanupFixMissing;
        private String cleanupActiveKey;
        private boolean cleanupJson;
        private List<String> extra = new ArrayList<>();

        /**
         * 列出会话（默认，无子命令 token）。
         *
         * @return {@code this}
         */
        public Builder list() {
            this.mode = Mode.LIST;
            return this;
        }

        /**
         * {@code sessions cleanup}。
         *
         * @return {@code this}
         */
        public Builder cleanup() {
            this.mode = Mode.CLEANUP;
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
         * @param allAgents {@code --all-agents}
         * @return {@code this}
         */
        public Builder allAgents(boolean allAgents) {
            this.allAgents = allAgents;
            return this;
        }

        /**
         * {@code --active}：最近活跃分钟数筛选。
         *
         * @param minutes 分钟数
         * @return {@code this}
         */
        public Builder activeMinutes(int minutes) {
            this.activeMinutes = minutes;
            return this;
        }

        /**
         * @param minutes {@code --active}（可为 null）
         * @return {@code this}
         */
        public Builder activeMinutes(Integer minutes) {
            this.activeMinutes = minutes;
            return this;
        }

        /**
         * @param verbose list：{@code --verbose}
         * @return {@code this}
         */
        public Builder verbose(boolean verbose) {
            this.verbose = verbose;
            return this;
        }

        /**
         * @param json list：{@code --json}
         * @return {@code this}
         */
        public Builder json(boolean json) {
            this.json = json;
            return this;
        }

        /**
         * @param storePath {@code --store}
         * @return {@code this}
         */
        public Builder store(String storePath) {
            this.store = storePath;
            return this;
        }

        /**
         * @param dryRun cleanup：{@code --dry-run}
         * @return {@code this}
         */
        public Builder cleanupDryRun(boolean dryRun) {
            this.cleanupDryRun = dryRun;
            return this;
        }

        /**
         * @param enforce cleanup：{@code --enforce}
         * @return {@code this}
         */
        public Builder cleanupEnforce(boolean enforce) {
            this.cleanupEnforce = enforce;
            return this;
        }

        /**
         * @param fixMissing cleanup：{@code --fix-missing}
         * @return {@code this}
         */
        public Builder cleanupFixMissing(boolean fixMissing) {
            this.cleanupFixMissing = fixMissing;
            return this;
        }

        /**
         * @param sessionKey cleanup：{@code --active-key}
         * @return {@code this}
         */
        public Builder cleanupActiveKey(String sessionKey) {
            this.cleanupActiveKey = sessionKey;
            return this;
        }

        /**
         * @param json cleanup：{@code --json}
         * @return {@code this}
         */
        public Builder cleanupJson(boolean json) {
            this.cleanupJson = json;
            return this;
        }

        /**
         * 追加未建模的 CLI token。
         *
         * @param tokens argv 片段
         * @return {@code this}
         */
        public Builder extra(String... tokens) {
            if (tokens != null) {
                Collections.addAll(extra, tokens);
            }
            return this;
        }

        /**
         * @return 不可变 {@link SessionsOptions}
         */
        public SessionsOptions build() {
            return new SessionsOptions(this);
        }
    }
}
