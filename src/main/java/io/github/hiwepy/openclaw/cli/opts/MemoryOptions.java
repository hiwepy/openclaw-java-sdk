package io.github.hiwepy.openclaw.cli.opts;

import io.github.hiwepy.openclaw.util.OpenClawLists;
import io.github.hiwepy.openclaw.util.OpenClawStrings;
import io.github.hiwepy.openclaw.cli.args.CliSubArgs;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * {@code openclaw memory}：由当前 memory 插件（默认 memory-core）提供语义索引、搜索与晋升/回填工具。
 * <p>子命令覆盖状态检查、强制索引、向量检索、晋升候选与 REM 回填；多数操作可按 agent 限定作用域。</p>
 *
 * @see <a href="https://docs.openclaw.ai/cli/memory">memory CLI</a>
 */
public final class MemoryOptions implements CliSubArgs {

    /**
     * memory 插件子命令：状态、索引、搜索、晋升解释与 REM 回填。
     */
    public enum Verb {
        /** {@code memory status}：展示索引健康、嵌入后端与存储占用。 */
        STATUS,
        /** {@code memory index}：对会话/文件增量或全量嵌入。 */
        INDEX,
        /** {@code memory search}：语义查询命中片段。 */
        SEARCH,
        /** {@code memory promote}：将高价值片段晋升到长期记忆。 */
        PROMOTE,
        /** {@code memory promote-explain}：解释某条晋升决策或候选。 */
        PROMOTE_EXPLAIN,
        /** {@code memory rem-harness}：REM 管线自检或 dry-run。 */
        REM_HARNESS,
        /** {@code memory rem-backfill}：从磁盘路径回填短期或长期 REM 存储。 */
        REM_BACKFILL
    }

    /** STATUS / INDEX / SEARCH / PROMOTE / PROMOTE_EXPLAIN / REM_HARNESS / REM_BACKFILL。 */
    private final Verb verb;
    /**
     * 多子命令共享：{@code --agent} 限定到指定 agent 的 memory 配置命名空间。
     */
    private final String agent;
    /**
     * status / index：{@code --verbose} 打印嵌入批次与后端细节。
     */
    private final boolean verbose;
    /**
     * status：{@code --deep} 扫描更多内部一致性检查。
     */
    private final boolean statusDeep;
    /**
     * status：{@code --index} 在状态输出中包含索引条目计数。
     */
    private final boolean statusIndex;
    /**
     * status：{@code --fix} 尝试自动修复可恢复的索引问题。
     */
    private final boolean statusFix;
    /**
     * status：{@code --json}。
     */
    private final boolean statusJson;
    /**
     * index：{@code --force} 忽略缓存时间戳全量重建。
     */
    private final boolean indexForce;
    /**
     * search：位置参数查询文本（与 {@code --query} 二选一）。
     */
    private final String searchPositional;
    /**
     * search：{@code --query} 显式查询字符串。
     */
    private final String searchQuery;
    /**
     * search / promote：{@code --max-results} 返回条数上限。
     */
    private final Integer maxResults;
    /**
     * search / promote：{@code --min-score} 相似度阈值过滤。
     */
    private final Double minScore;
    /**
     * search：{@code --json}。
     */
    private final boolean searchJson;
    /**
     * promote：{@code --apply} 将候选写入长期层；缺省为 dry-run。
     */
    private final boolean promoteApply;
    /**
     * promote：{@code --limit} 每次最多处理多少候选。
     */
    private final Integer promoteLimit;
    /**
     * promote / promote-explain / rem-harness：{@code --include-promoted} 在结果中包含已晋升条目。
     */
    private final boolean includePromoted;
    /**
     * promote：{@code --min-recall-count} 晋升所需最小被召回次数。
     */
    private final Integer minRecallCount;
    /**
     * promote：{@code --min-unique-queries} 晋升所需最小独立查询数。
     */
    private final Integer minUniqueQueries;
    /**
     * promote：{@code --json}。
     */
    private final boolean promoteJson;
    /**
     * promote-explain：选择器位置参数（条目 id 或键）。
     */
    private final String promoteExplainSelector;
    /**
     * promote-explain：{@code --json}。
     */
    private final boolean promoteExplainJson;
    /**
     * rem-harness：{@code --json} 输出管线自检结果。
     */
    private final boolean remHarnessJson;
    /**
     * rem-backfill：{@code --path} 源目录或文件 glob。
     */
    private final String remBackfillPath;
    /**
     * rem-backfill：{@code --grounded} 仅写入有锚定引用的片段。
     */
    private final boolean remBackfillGrounded;
    /**
     * rem-backfill：{@code --stage-short-term} 先写入短期层再晋升。
     */
    private final boolean remBackfillStageShortTerm;
    /**
     * rem-backfill：{@code --rollback} 撤销最近一次回填写入。
     */
    private final boolean remBackfillRollback;
    /**
     * rem-backfill：{@code --rollback-short-term} 只回滚短期层。
     */
    private final boolean remBackfillRollbackShortTerm;
    /**
     * 其它 argv。
     */
    private final List<String> extra;

    /**
     * @param b 构建器快照
     */
    private MemoryOptions(Builder b) {
        this.verb = b.verb;
        this.agent = b.agent;
        this.verbose = b.verbose;
        this.statusDeep = b.statusDeep;
        this.statusIndex = b.statusIndex;
        this.statusFix = b.statusFix;
        this.statusJson = b.statusJson;
        this.indexForce = b.indexForce;
        this.searchPositional = b.searchPositional;
        this.searchQuery = b.searchQuery;
        this.maxResults = b.maxResults;
        this.minScore = b.minScore;
        this.searchJson = b.searchJson;
        this.promoteApply = b.promoteApply;
        this.promoteLimit = b.promoteLimit;
        this.includePromoted = b.includePromoted;
        this.minRecallCount = b.minRecallCount;
        this.minUniqueQueries = b.minUniqueQueries;
        this.promoteJson = b.promoteJson;
        this.promoteExplainSelector = b.promoteExplainSelector;
        this.promoteExplainJson = b.promoteExplainJson;
        this.remHarnessJson = b.remHarnessJson;
        this.remBackfillPath = b.remBackfillPath;
        this.remBackfillGrounded = b.remBackfillGrounded;
        this.remBackfillStageShortTerm = b.remBackfillStageShortTerm;
        this.remBackfillRollback = b.remBackfillRollback;
        this.remBackfillRollbackShortTerm = b.remBackfillRollbackShortTerm;
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
        switch (verb) {
            case STATUS:
                out.add("status");
                appendSharedAgentVerbose(out);
                OpenClawCliArgv.addFlag(out, "--deep", statusDeep);
                OpenClawCliArgv.addFlag(out, "--index", statusIndex);
                OpenClawCliArgv.addFlag(out, "--fix", statusFix);
                OpenClawCliArgv.addFlag(out, "--json", statusJson);
                break;
            case INDEX:
                out.add("index");
                appendSharedAgentVerbose(out);
                OpenClawCliArgv.addFlag(out, "--force", indexForce);
                break;
            case SEARCH:
                out.add("search");
                if (searchQuery != null && OpenClawStrings.isNotBlank(searchQuery)) {
                    out.add("--query");
                    out.add(searchQuery.trim());
                } else if (searchPositional != null && OpenClawStrings.isNotBlank(searchPositional)) {
                    out.add(searchPositional.trim());
                }
                OpenClawCliArgv.addIfNotNull(out, "--max-results", maxResults);
                OpenClawCliArgv.addIfNotNull(out, "--min-score", minScore);
                OpenClawCliArgv.addIfPresent(out, "--agent", agent);
                OpenClawCliArgv.addFlag(out, "--json", searchJson);
                break;
            case PROMOTE:
                out.add("promote");
                OpenClawCliArgv.addIfPresent(out, "--agent", agent);
                OpenClawCliArgv.addIfNotNull(out, "--limit", promoteLimit);
                OpenClawCliArgv.addIfNotNull(out, "--min-score", minScore);
                OpenClawCliArgv.addIfNotNull(out, "--min-recall-count", minRecallCount);
                OpenClawCliArgv.addIfNotNull(out, "--min-unique-queries", minUniqueQueries);
                OpenClawCliArgv.addFlag(out, "--apply", promoteApply);
                OpenClawCliArgv.addFlag(out, "--include-promoted", includePromoted);
                OpenClawCliArgv.addFlag(out, "--json", promoteJson);
                break;
            case PROMOTE_EXPLAIN:
                out.add("promote-explain");
                if (promoteExplainSelector != null && OpenClawStrings.isNotBlank(promoteExplainSelector)) {
                    out.add(promoteExplainSelector.trim());
                }
                OpenClawCliArgv.addIfPresent(out, "--agent", agent);
                OpenClawCliArgv.addFlag(out, "--include-promoted", includePromoted);
                OpenClawCliArgv.addFlag(out, "--json", promoteExplainJson);
                break;
            case REM_HARNESS:
                out.add("rem-harness");
                OpenClawCliArgv.addIfPresent(out, "--agent", agent);
                OpenClawCliArgv.addFlag(out, "--include-promoted", includePromoted);
                OpenClawCliArgv.addFlag(out, "--json", remHarnessJson);
                break;
            case REM_BACKFILL:
                out.add("rem-backfill");
                OpenClawCliArgv.addIfPresent(out, "--path", remBackfillPath);
                OpenClawCliArgv.addFlag(out, "--grounded", remBackfillGrounded);
                OpenClawCliArgv.addFlag(out, "--stage-short-term", remBackfillStageShortTerm);
                OpenClawCliArgv.addFlag(out, "--rollback", remBackfillRollback);
                OpenClawCliArgv.addFlag(out, "--rollback-short-term", remBackfillRollbackShortTerm);
                break;
            default:
                break;
        }
        OpenClawCliArgv.addExtra(out, extra);
        return Collections.unmodifiableList(out);
    }

    /** status / index 共享的 agent 与 verbose。 */
    private void appendSharedAgentVerbose(List<String> out) {
        OpenClawCliArgv.addIfPresent(out, "--agent", agent);
        OpenClawCliArgv.addFlag(out, "--verbose", verbose);
    }

    /**
     * {@link MemoryOptions} 构建器。
     */
    public static final class Builder {
        private Verb verb = Verb.STATUS;
        private String agent;
        private boolean verbose;
        private boolean statusDeep;
        private boolean statusIndex;
        private boolean statusFix;
        private boolean statusJson;
        private boolean indexForce;
        private String searchPositional;
        private String searchQuery;
        private Integer maxResults;
        private Double minScore;
        private boolean searchJson;
        private boolean promoteApply;
        private Integer promoteLimit;
        private boolean includePromoted;
        private Integer minRecallCount;
        private Integer minUniqueQueries;
        private boolean promoteJson;
        private String promoteExplainSelector;
        private boolean promoteExplainJson;
        private boolean remHarnessJson;
        private String remBackfillPath;
        private boolean remBackfillGrounded;
        private boolean remBackfillStageShortTerm;
        private boolean remBackfillRollback;
        private boolean remBackfillRollbackShortTerm;
        private List<String> extra = new ArrayList<>();

        /**
         * @return {@code this}（{@code memory status}）
         */
        public Builder status() {
            this.verb = Verb.STATUS;
            return this;
        }

        /**
         * @return {@code this}（{@code memory index}）
         */
        public Builder index() {
            this.verb = Verb.INDEX;
            return this;
        }

        /**
         * @return {@code this}（{@code memory search}）
         */
        public Builder search() {
            this.verb = Verb.SEARCH;
            return this;
        }

        /**
         * @return {@code this}（{@code memory promote}）
         */
        public Builder promote() {
            this.verb = Verb.PROMOTE;
            return this;
        }

        /**
         * @param selector promote-explain：选择器
         * @return {@code this}
         */
        public Builder promoteExplain(String selector) {
            this.verb = Verb.PROMOTE_EXPLAIN;
            this.promoteExplainSelector = selector;
            return this;
        }

        /**
         * @return {@code this}（{@code memory rem-harness}）
         */
        public Builder remHarness() {
            this.verb = Verb.REM_HARNESS;
            return this;
        }

        /**
         * @return {@code this}（{@code memory rem-backfill}）
         */
        public Builder remBackfill() {
            this.verb = Verb.REM_BACKFILL;
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
         * @param verbose status / index：{@code --verbose}
         * @return {@code this}
         */
        public Builder verbose(boolean verbose) {
            this.verbose = verbose;
            return this;
        }

        /**
         * @param deep status：{@code --deep}
         * @return {@code this}
         */
        public Builder statusDeep(boolean deep) {
            this.statusDeep = deep;
            return this;
        }

        /**
         * @param index status：{@code --index}
         * @return {@code this}
         */
        public Builder statusIndex(boolean index) {
            this.statusIndex = index;
            return this;
        }

        /**
         * @param fix status：{@code --fix}
         * @return {@code this}
         */
        public Builder statusFix(boolean fix) {
            this.statusFix = fix;
            return this;
        }

        /**
         * @param json status：{@code --json}
         * @return {@code this}
         */
        public Builder statusJson(boolean json) {
            this.statusJson = json;
            return this;
        }

        /**
         * @param force index：{@code --force}
         * @return {@code this}
         */
        public Builder indexForce(boolean force) {
            this.indexForce = force;
            return this;
        }

        /**
         * {@code search} 位置参数（与 {@link #searchQuery(String)} 二选一时 {@code --query} 优先）。
         *
         * @param query 位置参数查询
         * @return {@code this}
         */
        public Builder searchPositional(String query) {
            this.searchPositional = query;
            return this;
        }

        /**
         * @param query search：{@code --query}
         * @return {@code this}
         */
        public Builder searchQuery(String query) {
            this.searchQuery = query;
            return this;
        }

        /**
         * @param max {@code --max-results}
         * @return {@code this}
         */
        public Builder maxResults(int max) {
            this.maxResults = max;
            return this;
        }

        /**
         * @param score {@code --min-score}
         * @return {@code this}
         */
        public Builder minScore(double score) {
            this.minScore = score;
            return this;
        }

        /**
         * @param json search：{@code --json}
         * @return {@code this}
         */
        public Builder searchJson(boolean json) {
            this.searchJson = json;
            return this;
        }

        /**
         * @param apply promote：{@code --apply}
         * @return {@code this}
         */
        public Builder promoteApply(boolean apply) {
            this.promoteApply = apply;
            return this;
        }

        /**
         * @param limit promote：{@code --limit}
         * @return {@code this}
         */
        public Builder promoteLimit(int limit) {
            this.promoteLimit = limit;
            return this;
        }

        /**
         * @param include {@code --include-promoted}
         * @return {@code this}
         */
        public Builder includePromoted(boolean include) {
            this.includePromoted = include;
            return this;
        }

        /**
         * @param n {@code --min-recall-count}
         * @return {@code this}
         */
        public Builder minRecallCount(int n) {
            this.minRecallCount = n;
            return this;
        }

        /**
         * @param n {@code --min-unique-queries}
         * @return {@code this}
         */
        public Builder minUniqueQueries(int n) {
            this.minUniqueQueries = n;
            return this;
        }

        /**
         * @param json promote：{@code --json}
         * @return {@code this}
         */
        public Builder promoteJson(boolean json) {
            this.promoteJson = json;
            return this;
        }

        /**
         * @param json promote-explain：{@code --json}
         * @return {@code this}
         */
        public Builder promoteExplainJson(boolean json) {
            this.promoteExplainJson = json;
            return this;
        }

        /**
         * @param json rem-harness：{@code --json}
         * @return {@code this}
         */
        public Builder remHarnessJson(boolean json) {
            this.remHarnessJson = json;
            return this;
        }

        /**
         * @param path rem-backfill：{@code --path}
         * @return {@code this}
         */
        public Builder remBackfillPath(String path) {
            this.remBackfillPath = path;
            return this;
        }

        /**
         * @param grounded {@code --grounded}
         * @return {@code this}
         */
        public Builder remBackfillGrounded(boolean grounded) {
            this.remBackfillGrounded = grounded;
            return this;
        }

        /**
         * @param stage {@code --stage-short-term}
         * @return {@code this}
         */
        public Builder remBackfillStageShortTerm(boolean stage) {
            this.remBackfillStageShortTerm = stage;
            return this;
        }

        /**
         * @param rollback {@code --rollback}
         * @return {@code this}
         */
        public Builder remBackfillRollback(boolean rollback) {
            this.remBackfillRollback = rollback;
            return this;
        }

        /**
         * @param rollback {@code --rollback-short-term}
         * @return {@code this}
         */
        public Builder remBackfillRollbackShortTerm(boolean rollback) {
            this.remBackfillRollbackShortTerm = rollback;
            return this;
        }

        /**
         * 追加未建模 token。
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
         * @return 不可变 {@link MemoryOptions}
         */
        public MemoryOptions build() {
            return new MemoryOptions(this);
        }
    }
}
