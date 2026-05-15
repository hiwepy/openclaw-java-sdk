package io.github.hiwepy.openclaw.cli.opts;

import io.github.hiwepy.openclaw.cli.args.CliSubArgs;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * {@code openclaw channels}：管理各聊天提供商账号配置，并在 Gateway 上查看实时连通、能力与日志。
 * <p>{@code status --probe} 在网关可达时对每账号跑 {@code probeAccount} 等实时检查；不可达则退回仅配置摘要。
 * {@code add} 的 per-channel flag 很多，请用 {@link Builder#extra(String...)} 或查阅 {@code channels add --help}。</p>
 *
 * @see <a href="https://docs.openclaw.ai/cli/channels">channels CLI</a>
 */
public final class ChannelsOptions implements CliSubArgs {

    /**
     * channels 子命令：列举、探测状态、能力探针、名称解析、日志或账号增删与登录登出。
     */
    public enum Verb {
        /** {@code channels list}：打印已配置账号。 */
        LIST,
        /** {@code channels status}：运行时状态，可选 live probe。 */
        STATUS,
        /** {@code channels capabilities}：提供商能力提示与权限探测。 */
        CAPABILITIES,
        /** {@code channels resolve}：把名称解析为 id。 */
        RESOLVE,
        /** {@code channels logs}：拉取渠道相关日志尾部。 */
        LOGS,
        /** {@code channels add}：非交互或向导式添加账号。 */
        ADD,
        /** {@code channels remove}：移除账号记录。 */
        REMOVE,
        /** {@code channels login}：交互式登录（如 QR 流程）。 */
        LOGIN,
        /** {@code channels logout}：登出并清理会话态。 */
        LOGOUT
    }

    /** 当前 channels 子命令。 */
    private final Verb verb;
    /**
     * status：{@code --probe} 启用实时账号探测与审计输出。
     */
    private final boolean statusProbe;
    /**
     * status / capabilities：{@code --timeout} 单次操作超时。
     */
    private final String timeout;
    /**
     * status / capabilities / logs / resolve：{@code --json}。
     */
    private final boolean json;
    /**
     * 多数子命令：{@code --channel} 选择提供商 id。
     */
    private final String channel;
    /**
     * capabilities / resolve：{@code --account} 仅在同时指定 {@code --channel} 时有效。
     */
    private final String account;
    /**
     * capabilities：{@code --target} Discord 等渠道的探测目标描述。
     */
    private final String target;
    /**
     * resolve：{@code --kind} 强制解析为用户、群组或自动。
     */
    private final String kind;
    /**
     * resolve：待解析的名称位置参数列表。
     */
    private final List<String> resolvePositional;
    /**
     * logs：{@code --lines} 尾部行数。
     */
    private final Integer logLines;
    /**
     * remove：{@code --delete} 同时删除远端/本地持久数据（per-channel 语义）。
     */
    private final boolean removeDelete;
    /**
     * login：{@code --verbose} 更详细输出。
     */
    private final boolean loginVerbose;
    /**
     * 其它 argv。
     */
    private final List<String> extra;

    /**
     * @param b 构建器快照
     */
    private ChannelsOptions(Builder b) {
        this.verb = b.verb;
        this.statusProbe = b.statusProbe;
        this.timeout = b.timeout;
        this.json = b.json;
        this.channel = b.channel;
        this.account = b.account;
        this.target = b.target;
        this.kind = b.kind;
        this.resolvePositional = b.resolvePositional == null ? List.of() : List.copyOf(b.resolvePositional);
        this.logLines = b.logLines;
        this.removeDelete = b.removeDelete;
        this.loginVerbose = b.loginVerbose;
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
            case LIST:
                out.add("list");
                break;
            case STATUS:
                out.add("status");
                OpenClawCliArgv.addFlag(out, "--probe", statusProbe);
                OpenClawCliArgv.addIfPresent(out, "--timeout", timeout);
                OpenClawCliArgv.addFlag(out, "--json", json);
                break;
            case CAPABILITIES:
                out.add("capabilities");
                OpenClawCliArgv.addIfPresent(out, "--channel", channel);
                OpenClawCliArgv.addIfPresent(out, "--account", account);
                OpenClawCliArgv.addIfPresent(out, "--target", target);
                OpenClawCliArgv.addIfPresent(out, "--timeout", timeout);
                OpenClawCliArgv.addFlag(out, "--json", json);
                break;
            case RESOLVE:
                out.add("resolve");
                OpenClawCliArgv.addIfPresent(out, "--channel", channel);
                OpenClawCliArgv.addIfPresent(out, "--account", account);
                OpenClawCliArgv.addIfPresent(out, "--kind", kind);
                OpenClawCliArgv.addFlag(out, "--json", json);
                out.addAll(resolvePositional);
                break;
            case LOGS:
                out.add("logs");
                OpenClawCliArgv.addIfPresent(out, "--channel", channel);
                OpenClawCliArgv.addIfNotNull(out, "--lines", logLines);
                OpenClawCliArgv.addFlag(out, "--json", json);
                break;
            case ADD:
                out.add("add");
                OpenClawCliArgv.addIfPresent(out, "--channel", channel);
                break;
            case REMOVE:
                out.add("remove");
                OpenClawCliArgv.addIfPresent(out, "--channel", channel);
                OpenClawCliArgv.addFlag(out, "--delete", removeDelete);
                break;
            case LOGIN:
                out.add("login");
                OpenClawCliArgv.addIfPresent(out, "--channel", channel);
                OpenClawCliArgv.addFlag(out, "--verbose", loginVerbose);
                break;
            case LOGOUT:
                out.add("logout");
                OpenClawCliArgv.addIfPresent(out, "--channel", channel);
                break;
            default:
                break;
        }
        OpenClawCliArgv.addExtra(out, extra);
        return Collections.unmodifiableList(out);
    }

    /**
     * {@link ChannelsOptions} 构建器。
     */
    public static final class Builder {
        private Verb verb = Verb.LIST;
        private boolean statusProbe;
        private String timeout;
        private boolean json;
        private String channel;
        private String account;
        private String target;
        private String kind;
        private List<String> resolvePositional = new ArrayList<>();
        private Integer logLines;
        private boolean removeDelete;
        private boolean loginVerbose;
        private List<String> extra = new ArrayList<>();

        /**
         * @return {@code this}（{@code channels list}）
         */
        public Builder list() {
            this.verb = Verb.LIST;
            return this;
        }

        /**
         * @return {@code this}（{@code channels status}）
         */
        public Builder status() {
            this.verb = Verb.STATUS;
            return this;
        }

        /**
         * @param probe status：{@code --probe}
         * @return {@code this}
         */
        public Builder statusProbe(boolean probe) {
            this.statusProbe = probe;
            return this;
        }

        /**
         * @return {@code this}（{@code channels capabilities}）
         */
        public Builder capabilities() {
            this.verb = Verb.CAPABILITIES;
            return this;
        }

        /**
         * @param positionalNames resolve：位置参数
         * @return {@code this}
         */
        public Builder resolve(String... positionalNames) {
            this.verb = Verb.RESOLVE;
            this.resolvePositional = new ArrayList<>();
            if (positionalNames != null) {
                for (String p : positionalNames) {
                    if (p != null && !p.isBlank()) {
                        resolvePositional.add(p.trim());
                    }
                }
            }
            return this;
        }

        /**
         * @return {@code this}（{@code channels logs}）
         */
        public Builder logs() {
            this.verb = Verb.LOGS;
            return this;
        }

        /**
         * @return {@code this}（{@code channels add}）
         */
        public Builder add() {
            this.verb = Verb.ADD;
            return this;
        }

        /**
         * @return {@code this}（{@code channels remove}）
         */
        public Builder remove() {
            this.verb = Verb.REMOVE;
            return this;
        }

        /**
         * @return {@code this}（{@code channels login}）
         */
        public Builder login() {
            this.verb = Verb.LOGIN;
            return this;
        }

        /**
         * @return {@code this}（{@code channels logout}）
         */
        public Builder logout() {
            this.verb = Verb.LOGOUT;
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
         * @param kind resolve：{@code --kind}
         * @return {@code this}
         */
        public Builder kind(String kind) {
            this.kind = kind;
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
         * @param json {@code --json}
         * @return {@code this}
         */
        public Builder json(boolean json) {
            this.json = json;
            return this;
        }

        /**
         * @param lines logs：{@code --lines}
         * @return {@code this}
         */
        public Builder logLines(int lines) {
            this.logLines = lines;
            return this;
        }

        /**
         * @param delete remove：{@code --delete}
         * @return {@code this}
         */
        public Builder removeDelete(boolean delete) {
            this.removeDelete = delete;
            return this;
        }

        /**
         * @param verbose login：{@code --verbose}
         * @return {@code this}
         */
        public Builder loginVerbose(boolean verbose) {
            this.loginVerbose = verbose;
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
         * @return 不可变 {@link ChannelsOptions}
         */
        public ChannelsOptions build() {
            return new ChannelsOptions(this);
        }
    }
}
