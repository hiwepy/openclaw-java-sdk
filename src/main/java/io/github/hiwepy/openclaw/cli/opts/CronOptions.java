package io.github.hiwepy.openclaw.cli.opts;

import io.github.hiwepy.openclaw.cli.args.CliSubArgs;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * {@code openclaw cron}：Gateway 侧定时与一次性任务（isolated 与主会话等模式），含投递、失败通知与运行历史。
 * <p>文档注意：isolated 任务默认 {@code --announce} 对外投递；{@code --no-deliver} 保持内部；{@code --at} 一次性任务成功后可自动删除除非 {@code --keep-after-run}。
 * 完整子命令以 {@code openclaw cron --help} 为准，未建模部分用 {@link Builder#extra(String...)}。</p>
 *
 * @see <a href="https://docs.openclaw.ai/cli/cron">cron CLI</a>
 */
public final class CronOptions implements CliSubArgs {

    /**
     * cron 子命令：手动运行、查看运行日志、增删改查任务定义等。
     */
    public enum Verb {
        /** {@code cron run}：默认强制排队执行；{@code --due} 保留仅到期才跑的旧行为。 */
        RUN,
        /** {@code cron runs}：按 job id 拉取最近运行记录。 */
        RUNS,
        /** {@code cron add}：新建计划任务。 */
        ADD,
        /** {@code cron edit}：就地修改既有任务。 */
        EDIT,
        /** {@code cron list}：列出任务。 */
        LIST,
        /** {@code cron delete}：删除任务。 */
        DELETE
    }

    /** 当前 cron 子命令。 */
    private final Verb verb;
    /**
     * run / edit / delete：任务 id 位置参数。
     */
    private final String jobId;
    /**
     * run：{@code --due} 仅在计划到期时才执行（与默认 force-run 相对）。
     */
    private final boolean runDue;
    /**
     * runs：{@code --id} 过滤到单个 job。
     */
    private final String runsId;
    /**
     * runs：{@code --limit} 返回条数上限。
     */
    private final Integer runsLimit;
    /**
     * add / edit：{@code --name} 人类可读任务名。
     */
    private final String name;
    /**
     * add / edit：{@code --cron} 标准 cron 表达式（循环任务）。
     */
    private final String cronExpr;
    /**
     * add / edit：{@code --session} 绑定会话键（{@code main}、{@code isolated}、{@code current}、{@code session:...} 等，见文档）。
     */
    private final String session;
    /**
     * add / edit：{@code --message} 发给 agent 的提示正文。
     */
    private final String message;
    /**
     * add / edit：{@code --at} 一次性触发时间；无偏移时按 UTC 解释除非同时提供 {@code --tz}。
     */
    private final String at;
    /**
     * add / edit：{@code --tz} 将 {@code --at} 解释为此时区的本地墙钟时间。
     */
    private final String tz;
    /**
     * add / edit：{@code --keep-after-run} 一次性任务成功后仍保留记录。
     */
    private final boolean keepAfterRun;
    /**
     * add / edit：{@code --announce} 通过渠道/webhook 等对外播报最终结果（isolated 任务默认倾向开启，见文档）。
     */
    private final boolean announce;
    /**
     * add / edit：{@code --no-deliver} 不对外投递，运行留在内部（不等价于把投递交回消息工具）。
     */
    private final boolean noDeliver;
    /**
     * add / edit：{@code --light-context} isolated agent 任务使用轻量 bootstrap（空注入而非完整 workspace 集合）。
     */
    private final boolean lightContext;
    /**
     * add / edit：{@code --announce} 搭配使用的 {@code --channel}。
     */
    private final String channel;
    /**
     * add / edit：{@code --announce} 搭配使用的 {@code --to} 目标。
     */
    private final String to;
    /**
     * add / edit：{@code --model} 覆盖任务允许使用的模型（不在 allowlist 时会警告并回退，见文档）。
     */
    private final String model;
    /**
     * add / edit：{@code --agent} 指定运行所属 agent。
     */
    private final String agent;
    /**
     * add / edit：{@code --clear-agent} 清除任务级 agent 覆盖。
     */
    private final boolean clearAgent;
    /**
     * add / edit：显式 {@code true} 时追加 {@code --best-effort-deliver}；{@code null} 表示不传该 flag。
     */
    private final Boolean bestEffortDeliver;
    /**
     * add / edit：显式 {@code true} 时追加 {@code --no-best-effort-deliver}；{@code null} 表示不传。
     */
    private final Boolean noBestEffortDeliver;
    /**
     * 其它 argv。
     */
    private final List<String> extra;

    /**
     * @param b 构建器快照
     */
    private CronOptions(Builder b) {
        this.verb = b.verb;
        this.jobId = b.jobId;
        this.runDue = b.runDue;
        this.runsId = b.runsId;
        this.runsLimit = b.runsLimit;
        this.name = b.name;
        this.cronExpr = b.cronExpr;
        this.session = b.session;
        this.message = b.message;
        this.at = b.at;
        this.tz = b.tz;
        this.keepAfterRun = b.keepAfterRun;
        this.announce = b.announce;
        this.noDeliver = b.noDeliver;
        this.lightContext = b.lightContext;
        this.channel = b.channel;
        this.to = b.to;
        this.model = b.model;
        this.agent = b.agent;
        this.clearAgent = b.clearAgent;
        this.bestEffortDeliver = b.bestEffortDeliver;
        this.noBestEffortDeliver = b.noBestEffortDeliver;
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
        switch (verb) {
            case RUN:
                out.add("run");
                if (jobId != null && !jobId.isBlank()) {
                    out.add(jobId.trim());
                }
                OpenClawCliArgv.addFlag(out, "--due", runDue);
                break;
            case RUNS:
                out.add("runs");
                OpenClawCliArgv.addIfPresent(out, "--id", runsId);
                OpenClawCliArgv.addIfNotNull(out, "--limit", runsLimit);
                break;
            case ADD:
                out.add("add");
                appendAddEditFlags(out, false);
                break;
            case EDIT:
                out.add("edit");
                if (jobId != null && !jobId.isBlank()) {
                    out.add(jobId.trim());
                }
                appendAddEditFlags(out, true);
                break;
            case LIST:
                out.add("list");
                break;
            case DELETE:
                out.add("delete");
                if (jobId != null && !jobId.isBlank()) {
                    out.add(jobId.trim());
                }
                break;
            default:
                break;
        }
        OpenClawCliArgv.addExtra(out, extra);
        return Collections.unmodifiableList(out);
    }

    private void appendAddEditFlags(List<String> out, boolean edit) {
        OpenClawCliArgv.addIfPresent(out, "--name", name);
        OpenClawCliArgv.addIfPresent(out, "--cron", cronExpr);
        OpenClawCliArgv.addIfPresent(out, "--session", session);
        OpenClawCliArgv.addIfPresent(out, "--message", message);
        OpenClawCliArgv.addIfPresent(out, "--at", at);
        OpenClawCliArgv.addIfPresent(out, "--tz", tz);
        OpenClawCliArgv.addFlag(out, "--keep-after-run", keepAfterRun);
        OpenClawCliArgv.addFlag(out, "--announce", announce);
        OpenClawCliArgv.addFlag(out, "--light-context", lightContext);
        OpenClawCliArgv.addFlag(out, "--no-deliver", noDeliver);
        OpenClawCliArgv.addIfPresent(out, "--channel", channel);
        OpenClawCliArgv.addIfPresent(out, "--to", to);
        OpenClawCliArgv.addIfPresent(out, "--model", model);
        OpenClawCliArgv.addIfPresent(out, "--agent", agent);
        OpenClawCliArgv.addFlag(out, "--clear-agent", clearAgent);
        if (Boolean.TRUE.equals(bestEffortDeliver)) {
            out.add("--best-effort-deliver");
        }
        if (Boolean.TRUE.equals(noBestEffortDeliver)) {
            out.add("--no-best-effort-deliver");
        }
    }

    /**
     * {@link CronOptions} 构建器。
     */
    public static final class Builder {
        private Verb verb = Verb.LIST;
        private String jobId;
        private boolean runDue;
        private String runsId;
        private Integer runsLimit;
        private String name;
        private String cronExpr;
        private String session;
        private String message;
        private String at;
        private String tz;
        private boolean keepAfterRun;
        private boolean announce;
        private boolean noDeliver;
        private boolean lightContext;
        private String channel;
        private String to;
        private String model;
        private String agent;
        private boolean clearAgent;
        private Boolean bestEffortDeliver;
        private Boolean noBestEffortDeliver;
        private List<String> extra = new ArrayList<>();

        /**
         * @param jobId 任务 ID（可为 null）
         * @return {@code this}
         */
        public Builder run(String jobId) {
            this.verb = Verb.RUN;
            this.jobId = jobId;
            return this;
        }

        /**
         * @param due run：{@code --due}
         * @return {@code this}
         */
        public Builder runDue(boolean due) {
            this.runDue = due;
            return this;
        }

        /**
         * @param jobId runs：{@code --id}
         * @return {@code this}
         */
        public Builder runs(String jobId) {
            this.verb = Verb.RUNS;
            this.runsId = jobId;
            return this;
        }

        /**
         * @param limit runs：{@code --limit}
         * @return {@code this}
         */
        public Builder runsLimit(int limit) {
            this.runsLimit = limit;
            return this;
        }

        /**
         * @return {@code this}（{@code cron add}）
         */
        public Builder add() {
            this.verb = Verb.ADD;
            return this;
        }

        /**
         * @param jobId 任务 ID
         * @return {@code this}
         */
        public Builder edit(String jobId) {
            this.verb = Verb.EDIT;
            this.jobId = jobId;
            return this;
        }

        /**
         * @return {@code this}（{@code cron list}）
         */
        public Builder list() {
            this.verb = Verb.LIST;
            return this;
        }

        /**
         * @param jobId 任务 ID
         * @return {@code this}
         */
        public Builder delete(String jobId) {
            this.verb = Verb.DELETE;
            this.jobId = jobId;
            return this;
        }

        /**
         * @param name {@code --name}
         * @return {@code this}
         */
        public Builder name(String name) {
            this.name = name;
            return this;
        }

        /**
         * @param expr {@code --cron}
         * @return {@code this}
         */
        public Builder cronExpr(String expr) {
            this.cronExpr = expr;
            return this;
        }

        /**
         * @param session {@code --session}
         * @return {@code this}
         */
        public Builder session(String session) {
            this.session = session;
            return this;
        }

        /**
         * @param message {@code --message}
         * @return {@code this}
         */
        public Builder message(String message) {
            this.message = message;
            return this;
        }

        /**
         * @param at {@code --at}
         * @return {@code this}
         */
        public Builder at(String at) {
            this.at = at;
            return this;
        }

        /**
         * @param tz {@code --tz}
         * @return {@code this}
         */
        public Builder tz(String tz) {
            this.tz = tz;
            return this;
        }

        /**
         * @param keep {@code --keep-after-run}
         * @return {@code this}
         */
        public Builder keepAfterRun(boolean keep) {
            this.keepAfterRun = keep;
            return this;
        }

        /**
         * @param announce {@code --announce}
         * @return {@code this}
         */
        public Builder announce(boolean announce) {
            this.announce = announce;
            return this;
        }

        /**
         * @param noDeliver {@code --no-deliver}
         * @return {@code this}
         */
        public Builder noDeliver(boolean noDeliver) {
            this.noDeliver = noDeliver;
            return this;
        }

        /**
         * @param lightContext {@code --light-context}
         * @return {@code this}
         */
        public Builder lightContext(boolean lightContext) {
            this.lightContext = lightContext;
            return this;
        }

        /**
         * @param channel {@code --channel}
         * @return {@code this}
         */
        public Builder channel(String channel) {
            this.channel = channel;
            return this;
        }

        /**
         * @param to {@code --to}
         * @return {@code this}
         */
        public Builder to(String to) {
            this.to = to;
            return this;
        }

        /**
         * @param model {@code --model}
         * @return {@code this}
         */
        public Builder model(String model) {
            this.model = model;
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
         * @param clear {@code --clear-agent}
         * @return {@code this}
         */
        public Builder clearAgent(boolean clear) {
            this.clearAgent = clear;
            return this;
        }

        /**
         * @param v 为 true 时输出 {@code --best-effort-deliver}
         * @return {@code this}
         */
        public Builder bestEffortDeliver(boolean v) {
            this.bestEffortDeliver = v ? Boolean.TRUE : null;
            return this;
        }

        /**
         * @param v 为 true 时输出 {@code --no-best-effort-deliver}
         * @return {@code this}
         */
        public Builder noBestEffortDeliver(boolean v) {
            this.noBestEffortDeliver = v ? Boolean.TRUE : null;
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
         * @return 不可变 {@link CronOptions}
         */
        public CronOptions build() {
            return new CronOptions(this);
        }
    }
}
