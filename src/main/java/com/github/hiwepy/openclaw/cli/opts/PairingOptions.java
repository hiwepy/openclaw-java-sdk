package com.github.hiwepy.openclaw.cli.opts;

import com.github.hiwepy.openclaw.cli.args.CliSubArgs;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * {@code openclaw pairing}：列出并审批支持 DM 配对的渠道上的待处理配对请求（与 channels 配对流程配套）。
 * <p>配置多个可配对渠道时必须位置传入 channel 或使用 {@code --channel}；仅一个可配对渠道时 {@code approve} 可省略 channel。</p>
 *
 * @see <a href="https://docs.openclaw.ai/cli/pairing">pairing CLI</a>
 */
public final class PairingOptions implements CliSubArgs {

    /**
     * pairing 子命令：查看队列或批准配对码。
     */
    public enum Verb {
        /** {@code pairing list}：列出某渠道的待处理请求。 */
        LIST,
        /** {@code pairing approve}：用配对码放行发送方。 */
        APPROVE
    }

    /** list 或 approve。 */
    private final Verb verb;
    /**
     * list：channel id 位置参数（可与 {@code --channel} 二选一）；approve 时在无 {@code --channel} 时也可承载 channel。
     */
    private final String channelPositional;
    /**
     * list / approve：{@code --channel} 显式渠道 id。
     */
    private final String channel;
    /**
     * list / approve：{@code --account} 多账号渠道的 account id。
     */
    private final String account;
    /**
     * list：{@code --json}。
     */
    private final boolean json;
    /**
     * approve：配对码位置参数（与 channel 参数顺序见 {@link #toSubcommandArguments()}）。
     */
    private final String approveCode;
    /**
     * approve：{@code --notify} 在同一渠道向请求方发确认消息。
     */
    private final boolean notify;
    /**
     * 其它 argv。
     */
    private final List<String> extra;

    /**
     * @param b 构建器快照
     */
    private PairingOptions(Builder b) {
        this.verb = b.verb;
        this.channelPositional = b.channelPositional;
        this.channel = b.channel;
        this.account = b.account;
        this.json = b.json;
        this.approveCode = b.approveCode;
        this.notify = b.notify;
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
        if (verb == Verb.LIST) {
            out.add("list");
            if (channelPositional != null && !channelPositional.isBlank()) {
                out.add(channelPositional.trim());
            }
            OpenClawCliArgv.addIfPresent(out, "--channel", channel);
            OpenClawCliArgv.addIfPresent(out, "--account", account);
            OpenClawCliArgv.addFlag(out, "--json", json);
        } else {
            out.add("approve");
            OpenClawCliArgv.addIfPresent(out, "--channel", channel);
            OpenClawCliArgv.addIfPresent(out, "--account", account);
            if (channel == null && channelPositional != null && !channelPositional.isBlank()) {
                out.add(channelPositional.trim());
            }
            if (approveCode != null && !approveCode.isBlank()) {
                out.add(approveCode.trim());
            }
            OpenClawCliArgv.addFlag(out, "--notify", notify);
        }
        OpenClawCliArgv.addExtra(out, extra);
        return Collections.unmodifiableList(out);
    }

    /**
     * {@link PairingOptions} 构建器。
     */
    public static final class Builder {
        private Verb verb = Verb.LIST;
        private String channelPositional;
        private String channel;
        private String account;
        private boolean json;
        private String approveCode;
        private boolean notify;
        private List<String> extra = new ArrayList<>();

        /**
         * {@code pairing list}（无 channel 位置参数）。
         *
         * @return {@code this}
         */
        public Builder list() {
            this.verb = Verb.LIST;
            this.channelPositional = null;
            return this;
        }

        /**
         * {@code pairing list [channel]}。
         *
         * @param channelPositionalOrNull channel 位置参数（可为 null）
         * @return {@code this}
         */
        public Builder list(String channelPositionalOrNull) {
            this.verb = Verb.LIST;
            this.channelPositional = channelPositionalOrNull;
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
         * @param json list：{@code --json}
         * @return {@code this}
         */
        public Builder json(boolean json) {
            this.json = json;
            return this;
        }

        /**
         * {@code pairing approve [channel] <code>}；若仅一个可配对 channel 可省略 channel。
         *
         * @param channelPositional 可为 null
         * @param code              配对码
         * @return {@code this}
         */
        public Builder approve(String channelPositional, String code) {
            this.verb = Verb.APPROVE;
            this.channelPositional = channelPositional;
            this.approveCode = code;
            return this;
        }

        /**
         * @param notify {@code --notify}
         * @return {@code this}
         */
        public Builder notify(boolean notify) {
            this.notify = notify;
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
         * @return 不可变 {@link PairingOptions}
         */
        public PairingOptions build() {
            return new PairingOptions(this);
        }
    }
}
