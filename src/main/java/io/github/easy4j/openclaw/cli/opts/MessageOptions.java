package io.github.easy4j.openclaw.cli.opts;

import io.github.easy4j.openclaw.util.OpenClawLists;
import io.github.easy4j.openclaw.util.OpenClawStrings;
import io.github.easy4j.openclaw.cli.args.CliSubArgs;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * {@code openclaw message}：统一出站消息与渠道动作入口（send、poll、react、thread、moderation 等），覆盖多聊天提供商。
 * <p>配置多个渠道时通常必须 {@code --channel}；{@code --target} 格式随 provider 变化（Telegram chat id、Slack {@code channel:} 等）。
 * 支持 SecretRef 的凭据会在执行前按当前 action 目标解析；未解析到所选渠道/账号会 fail closed。子命令极多，未建模部分用 {@link Builder#extra(String...)}。</p>
 *
 * @see <a href="https://docs.openclaw.ai/cli/message">message CLI</a>
 */
public final class MessageOptions implements CliSubArgs {

    /**
     * 子命令路径前缀（如 {@code send}、{@code thread}、{@code create}），按官方 CLI 顺序拼接。
     */
    private final List<String> action;
    /**
     * {@code --channel}：discord、slack、telegram 等渠道 id；多渠道配置时通常必填。
     */
    private final String channel;
    /**
     * {@code --account}：多账号渠道的账号 id。
     */
    private final String account;
    /**
     * {@code --target}：单播目标（用户、频道、会话 id 等，格式见 message 文档 Target formats）。
     */
    private final String target;
    /**
     * {@code --targets} 可重复：广播等多目标场景。
     */
    private final List<String> targets;
    /**
     * {@code --message}：文本正文（send/edit 等子命令）。
     */
    private final String message;
    /**
     * {@code --media}：附件或媒体路径/url。
     */
    private final String media;
    /**
     * {@code --message-id}：反应、编辑、删除等动作引用的消息 id。
     */
    private final String messageId;
    /**
     * {@code --emoji}：反应动作使用的 emoji。
     */
    private final String emoji;
    /**
     * {@code --json}：机器可读输出。
     */
    private final boolean json;
    /**
     * {@code --dry-run}：校验参数与路由而不实际发送（如 broadcast）。
     */
    private final boolean dryRun;
    /**
     * {@code --verbose}：更详细的 CLI 日志。
     */
    private final boolean verbose;
    /**
     * {@code --poll-question}：投票题干。
     */
    private final String pollQuestion;
    /**
     * {@code --poll-option} 可重复：投票选项列表。
     */
    private final List<String> pollOptions;
    /**
     * 其它 argv（例如各渠道专有 flag）。
     */
    private final List<String> extra;

    /**
     * @param b 构建器快照
     */
    private MessageOptions(Builder b) {
        this.action = b.action == null ? OpenClawLists.empty() : OpenClawLists.copyOf(b.action);
        this.channel = b.channel;
        this.account = b.account;
        this.target = b.target;
        this.targets = b.targets == null ? OpenClawLists.empty() : OpenClawLists.copyOf(b.targets);
        this.message = b.message;
        this.media = b.media;
        this.messageId = b.messageId;
        this.emoji = b.emoji;
        this.json = b.json;
        this.dryRun = b.dryRun;
        this.verbose = b.verbose;
        this.pollQuestion = b.pollQuestion;
        this.pollOptions = b.pollOptions == null ? OpenClawLists.empty() : OpenClawLists.copyOf(b.pollOptions);
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
        out.addAll(action);
        OpenClawCliArgv.addIfPresent(out, "--channel", channel);
        OpenClawCliArgv.addIfPresent(out, "--account", account);
        OpenClawCliArgv.addIfPresent(out, "--target", target);
        OpenClawCliArgv.addRepeatable(out, "--targets", targets);
        OpenClawCliArgv.addIfPresent(out, "--message", message);
        OpenClawCliArgv.addIfPresent(out, "--media", media);
        OpenClawCliArgv.addIfPresent(out, "--message-id", messageId);
        OpenClawCliArgv.addIfPresent(out, "--emoji", emoji);
        OpenClawCliArgv.addIfPresent(out, "--poll-question", pollQuestion);
        for (String opt : pollOptions) {
            if (opt != null && OpenClawStrings.isNotBlank(opt)) {
                out.add("--poll-option");
                out.add(opt.trim());
            }
        }
        OpenClawCliArgv.addFlag(out, "--json", json);
        OpenClawCliArgv.addFlag(out, "--dry-run", dryRun);
        OpenClawCliArgv.addFlag(out, "--verbose", verbose);
        OpenClawCliArgv.addExtra(out, extra);
        return Collections.unmodifiableList(out);
    }

    /**
     * {@link MessageOptions} 构建器。
     */
    public static final class Builder {
        private List<String> action = new ArrayList<>();
        private String channel;
        private String account;
        private String target;
        private List<String> targets = new ArrayList<>();
        private String message;
        private String media;
        private String messageId;
        private String emoji;
        private boolean json;
        private boolean dryRun;
        private boolean verbose;
        private String pollQuestion;
        private List<String> pollOptions = new ArrayList<>();
        private List<String> extra = new ArrayList<>();

        /**
         * 子命令路径，例如 {@code action("send")}、{@code action("thread", "create")}。
         *
         * @param parts 子命令片段
         * @return {@code this}
         */
        public Builder action(String... parts) {
            this.action = new ArrayList<>();
            if (parts != null) {
                for (String p : parts) {
                    if (p != null && OpenClawStrings.isNotBlank(p)) {
                        action.add(p.trim());
                    }
                }
            }
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
         * @param account {@code --account}
         * @return {@code this}
         */
        public Builder account(String account) {
            this.account = account;
            return this;
        }

        /**
         * @param target {@code --target}
         * @return {@code this}
         */
        public Builder target(String target) {
            this.target = target;
            return this;
        }

        /**
         * @param t 追加 {@code --targets}
         * @return {@code this}
         */
        public Builder addTarget(String t) {
            if (t != null && OpenClawStrings.isNotBlank(t)) {
                targets.add(t.trim());
            }
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
         * @param media {@code --media}
         * @return {@code this}
         */
        public Builder media(String media) {
            this.media = media;
            return this;
        }

        /**
         * @param messageId {@code --message-id}
         * @return {@code this}
         */
        public Builder messageId(String messageId) {
            this.messageId = messageId;
            return this;
        }

        /**
         * @param emoji {@code --emoji}
         * @return {@code this}
         */
        public Builder emoji(String emoji) {
            this.emoji = emoji;
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
         * @param dryRun {@code --dry-run}
         * @return {@code this}
         */
        public Builder dryRun(boolean dryRun) {
            this.dryRun = dryRun;
            return this;
        }

        /**
         * @param verbose {@code --verbose}
         * @return {@code this}
         */
        public Builder verbose(boolean verbose) {
            this.verbose = verbose;
            return this;
        }

        /**
         * @param q {@code --poll-question}
         * @return {@code this}
         */
        public Builder pollQuestion(String q) {
            this.pollQuestion = q;
            return this;
        }

        /**
         * @param option 追加 {@code --poll-option}
         * @return {@code this}
         */
        public Builder pollOption(String option) {
            if (option != null && OpenClawStrings.isNotBlank(option)) {
                pollOptions.add(option.trim());
            }
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
         * @return 不可变 {@link MessageOptions}
         */
        public MessageOptions build() {
            return new MessageOptions(this);
        }
    }
}
