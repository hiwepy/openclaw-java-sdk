package io.github.easy4j.openclaw.cli.opts;

import io.github.easy4j.openclaw.util.OpenClawStrings;
import io.github.easy4j.openclaw.cli.args.CliSubArgs;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * {@code openclaw agent} 命令参数，与官方文档 Options 一一对应。
 * <p>通过 {@link Builder#build()} 前会校验：{@code --message} 必填；且 {@code --to}、{@code --session-id}、{@code --agent} 至少填其一。</p>
 * <p>强类型字段：{@link ThinkingLevel}、{@link VerboseLevel}、超时秒数 {@link Integer}；亦可通过 {@code thinking(String)} / {@code verbose(String)} 传入 CLI 未来可能扩展的取值。</p>
 *
 * @see <a href="https://docs.openclaw.ai/cli/agent">agent CLI</a>
 */
public final class AgentOptions implements CliSubArgs {

    /**
     * {@code -m} / {@code --message}：本轮发给 agent 的必填正文（Gateway 或嵌入式执行均需要）。
     */
    private final String message;
    /**
     * {@code -t} / {@code --to}：收件方标识，用于派生会话键（session key）；与 {@code --session-id}、{@code --agent} 至少其一配合使用。
     */
    private final String to;
    /**
     * {@code --session-id}：显式会话 id，绕过由 {@code --to} 推导的会话键。
     */
    private final String sessionId;
    /**
     * {@code --agent}：目标 agent id，覆盖路由绑定所选定的 agent。
     */
    private final String agent;
    /**
     * 非空表示已设置 {@code --thinking}：agent 思考强度档位（官方枚举见 {@link ThinkingLevel}），亦可为 CLI 未来扩展的自定义 token。
     */
    private final String thinking;
    /**
     * 非空表示已设置 {@code --verbose}：将该会话的 verbose 级别持久化为 {@code on} 或 {@code off}（见 {@link VerboseLevel}），亦可为自定义 token。
     */
    private final String verbose;
    /**
     * {@code --channel}：回复投递所用渠道；省略则使用主会话渠道（文档：不影响会话路由，只影响投递）。
     */
    private final String channel;
    /**
     * {@code --reply-to}：覆盖回复投递目标（例如频道线程或用户 id，语义依渠道而定）。
     */
    private final String replyTo;
    /**
     * {@code --reply-channel}：覆盖回复所用渠道（与 {@code --channel} 分工见 agent 文档 Notes）。
     */
    private final String replyChannel;
    /**
     * {@code --reply-account}：覆盖回复所用渠道账号（多账号场景）。
     */
    private final String replyAccount;
    /**
     * {@code --local}：在预加载插件注册表后强制走嵌入式 agent，而非优先走 Gateway（文档：仍先加载插件侧 providers/tools/channels）。
     */
    private final boolean local;
    /**
     * {@code --deliver}：将 agent 产出发回所选 channel/target（与仅跑 turn 不投递相对）。
     */
    private final boolean deliver;
    /**
     * 覆盖本轮 agent 超时（秒）；{@code null} 表示不传 {@code --timeout}（默认约 600 秒或配置值，见文档）。
     */
    private final Integer timeoutSeconds;
    /**
     * {@code --json}：以机器可读的 JSON 输出本轮结果。
     */
    private final boolean json;

    private AgentOptions(Builder b) {
        this.message = b.message;
        this.to = b.to;
        this.sessionId = b.sessionId;
        this.agent = b.agent;
        this.thinking = b.thinking;
        this.verbose = b.verbose;
        this.channel = b.channel;
        this.replyTo = b.replyTo;
        this.replyChannel = b.replyChannel;
        this.replyAccount = b.replyAccount;
        this.local = b.local;
        this.deliver = b.deliver;
        this.timeoutSeconds = b.timeoutSeconds;
        this.json = b.json;
    }

    /**
     * 创建 {@link AgentOptions} 构建器。
     *
     * @return 新的 Builder
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * openclaw agent --to +15555550123 --message "status update" --deliver
     * openclaw agent --agent ops --message "Summarize logs"
     * openclaw agent --session-id 1234 --message "Summarize inbox" --thinking medium
     * openclaw agent --to +15555550123 --message "Trace logs" --verbose on --json
     * openclaw agent --agent ops --message "Generate report" --deliver --reply-channel slack --reply-to "#reports"
     * openclaw agent --agent ops --message "Run locally" --local
     * @return 命令行参数
     */
    @Override
    public List<String> toSubcommandArguments() {
        List<String> out = new ArrayList<>();
        // --message 在 build() 已保证非空
        out.add("--message");
        out.add(message);
        if (to != null && !to.isEmpty()) {
            out.add("--to");
            out.add(to);
        }
        if (sessionId != null && !sessionId.isEmpty()) {
            out.add("--session-id");
            out.add(sessionId);
        }
        if (agent != null && !agent.isEmpty()) {
            out.add("--agent");
            out.add(agent);
        }
        if (thinking != null && !thinking.isEmpty()) {
            out.add("--thinking");
            out.add(thinking);
        }
        if (verbose != null && !verbose.isEmpty()) {
            out.add("--verbose");
            out.add(verbose);
        }
        if (channel != null && !channel.isEmpty()) {
            out.add("--channel");
            out.add(channel);
        }
        if (replyTo != null && !replyTo.isEmpty()) {
            out.add("--reply-to");
            out.add(replyTo);
        }
        if (replyChannel != null && !replyChannel.isEmpty()) {
            out.add("--reply-channel");
            out.add(replyChannel);
        }
        if (replyAccount != null && !replyAccount.isEmpty()) {
            out.add("--reply-account");
            out.add(replyAccount);
        }
        if (local) {
            out.add("--local");
        }
        if (deliver) {
            out.add("--deliver");
        }
        if (timeoutSeconds != null) {
            out.add("--timeout");
            out.add(Integer.toString(timeoutSeconds));
        }
        if (json) {
            out.add("--json");
        }
        return Collections.unmodifiableList(out);
    }

    /**
     * {@link AgentOptions} 构建器；{@link #build()} 执行文档要求的必填与会话选择校验。
     */
    public static final class Builder {

        private String message;
        private String to;
        private String sessionId;
        private String agent;
        private String thinking;
        private String verbose;
        private String channel;
        private String replyTo;
        private String replyChannel;
        private String replyAccount;
        private boolean local;
        private boolean deliver;
        private Integer timeoutSeconds;
        private boolean json;

        /**
         * {@code -m} / {@code --message}：消息正文（必填）。
         *
         * @param message 消息体
         * @return this
         */
        public Builder message(String message) {
            this.message = message;
            return this;
        }

        /**
         * {@code -t} / {@code --to}：用于派生 session 的接收方。
         *
         * @param to 接收方标识
         * @return this
         */
        public Builder to(String to) {
            this.to = to;
            return this;
        }

        /**
         * {@code --session-id}：显式会话 id。
         *
         * @param sessionId 会话 id
         * @return this
         */
        public Builder sessionId(String sessionId) {
            this.sessionId = sessionId;
            return this;
        }

        /**
         * {@code --agent}：指定 agent id，可覆盖路由绑定。
         *
         * @param agent agent 标识
         * @return this
         */
        public Builder agent(String agent) {
            this.agent = agent;
            return this;
        }

        /**
         * {@code --thinking}：使用文档列出的档位。
         *
         * @param thinking 思考强度枚举
         * @return this
         */
        public Builder thinking(ThinkingLevel thinking) {
            Objects.requireNonNull(thinking, "thinking");
            this.thinking = thinking.cliValue();
            return this;
        }

        /**
         * {@code --thinking}：自定义或未来 CLI 扩展取值（与 {@link #thinking(ThinkingLevel)} 二选一，后设者覆盖）。
         *
         * @param thinking 原始 token
         * @return this
         */
        public Builder thinking(String thinking) {
            this.thinking = thinking;
            return this;
        }

        /**
         * {@code --verbose}：会话级 verbose 持久化。
         *
         * @param verbose on / off
         * @return this
         */
        public Builder verbose(VerboseLevel verbose) {
            Objects.requireNonNull(verbose, "verbose");
            this.verbose = verbose.cliValue();
            return this;
        }

        /**
         * {@code --verbose}：自定义取值（与 {@link #verbose(VerboseLevel)} 二选一，后设者覆盖）。
         *
         * @param verbose 原始 token，通常为 {@code on} 或 {@code off}
         * @return this
         */
        public Builder verbose(String verbose) {
            this.verbose = verbose;
            return this;
        }

        /**
         * {@code --channel}：投递渠道；省略则使用主会话渠道。
         *
         * @param channel 渠道名
         * @return this
         */
        public Builder channel(String channel) {
            this.channel = channel;
            return this;
        }

        /**
         * {@code --reply-to}：投递目标覆盖。
         *
         * @param replyTo 目标
         * @return this
         */
        public Builder replyTo(String replyTo) {
            this.replyTo = replyTo;
            return this;
        }

        /**
         * {@code --reply-channel}：投递渠道覆盖。
         *
         * @param replyChannel 渠道
         * @return this
         */
        public Builder replyChannel(String replyChannel) {
            this.replyChannel = replyChannel;
            return this;
        }

        /**
         * {@code --reply-account}：投递账号覆盖。
         *
         * @param replyAccount 账号 id
         * @return this
         */
        public Builder replyAccount(String replyAccount) {
            this.replyAccount = replyAccount;
            return this;
        }

        /**
         * {@code --local}：在插件注册表预加载后直接运行嵌入式 agent。
         *
         * @param local 是否本地嵌入式
         * @return this
         */
        public Builder local(boolean local) {
            this.local = local;
            return this;
        }

        /**
         * {@code --deliver}：将回复发回所选 channel/target。
         *
         * @param deliver 是否投递
         * @return this
         */
        public Builder deliver(boolean deliver) {
            this.deliver = deliver;
            return this;
        }

        /**
         * {@code --timeout}：覆盖 agent 超时（秒）。
         *
         * @param timeoutSeconds 秒数；{@code null} 表示不传该 flag
         * @return this
         */
        public Builder timeoutSeconds(Integer timeoutSeconds) {
            this.timeoutSeconds = timeoutSeconds;
            return this;
        }

        /**
         * {@code --timeout}：覆盖 agent 超时（秒）的便捷重载。
         *
         * @param timeoutSeconds 秒数
         * @return this
         */
        public Builder timeoutSeconds(int timeoutSeconds) {
            this.timeoutSeconds = timeoutSeconds;
            return this;
        }

        /**
         * {@code --json}：以 JSON 输出。
         *
         * @param json 是否 JSON
         * @return this
         */
        public Builder json(boolean json) {
            this.json = json;
            return this;
        }

        /**
         * 构建不可变 {@link AgentOptions}。
         * <p>校验规则：{@code message} 非空白；{@code to}、{@code sessionId}、{@code agent} 至少一项非空白。</p>
         *
         * @return 配置完成的参数对象
         * @throws IllegalStateException 不满足文档必填规则时
         */
        public AgentOptions build() {
            if (message == null || OpenClawStrings.isBlank(message)) {
                throw new IllegalStateException("agent: --message is required and must be non-blank");
            }
            boolean hasSessionSelector = (to != null && OpenClawStrings.isNotBlank(to))
                    || (sessionId != null && OpenClawStrings.isNotBlank(sessionId))
                    || (agent != null && OpenClawStrings.isNotBlank(agent));
            if (!hasSessionSelector) {
                throw new IllegalStateException(
                        "agent: at least one of --to, --session-id, or --agent is required (non-blank)");
            }
            return new AgentOptions(this);
        }
    }
}
