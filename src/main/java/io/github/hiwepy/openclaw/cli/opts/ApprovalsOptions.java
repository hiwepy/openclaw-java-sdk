package io.github.hiwepy.openclaw.cli.opts;

import io.github.hiwepy.openclaw.util.OpenClawLists;
import io.github.hiwepy.openclaw.util.OpenClawStrings;
import io.github.hiwepy.openclaw.cli.args.CliSubArgs;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * {@code openclaw approvals}（别名 {@code exec-approvals}）：查看或写入本机、网关或 node 上的 exec 审批文件与 glob allowlist。
 * <p>默认操作本地 {@code ~/.openclaw/exec-approvals.json}；{@code --gateway} 与 {@code --node} 切换目标主机。
 * {@code set} 接受 JSON5，可用 {@code --file} 或 {@code --stdin} 二选一。</p>
 *
 * @see <a href="https://docs.openclaw.ai/cli/approvals">approvals CLI</a>
 */
public final class ApprovalsOptions implements CliSubArgs {

    /**
     * approvals 子命令：读取策略、替换文件或维护 allowlist。
     */
    public enum Verb {
        /** {@code approvals get}：展示有效 exec 策略与来源优先级说明。 */
        GET,
        /** {@code approvals set}：从文件或标准输入写入整份审批 JSON。 */
        SET,
        /** {@code approvals allowlist add}：为某 agent 范围新增 glob。 */
        ALLOWLIST_ADD,
        /** {@code approvals allowlist remove}：删除 glob。 */
        ALLOWLIST_REMOVE
    }

    /** get / set / allowlist 之一。 */
    private final Verb verb;
    /**
     * {@code --node}：解析为与 {@code openclaw nodes} 相同语义的节点选择器。
     */
    private final String node;
    /**
     * {@code --gateway}：目标为网关主机上的审批文件。
     */
    private final boolean gateway;
    /**
     * {@code --url}：node/gateway RPC 使用的 WebSocket（与文档 Common options 一致）。
     */
    private final String url;
    /**
     * {@code --token}：网关 token。
     */
    private final String token;
    /**
     * {@code --password}：网关密码。
     */
    private final String password;
    /**
     * {@code --timeout}：RPC 超时。
     */
    private final String timeout;
    /**
     * {@code --json}：机器可读输出。
     */
    private final boolean json;
    /**
     * set：{@code --file} 审批 JSON 文件路径。
     */
    private final String file;
    /**
     * set：{@code --stdin} 从标准输入读取 JSON5。
     */
    private final boolean stdin;
    /**
     * allowlist add/remove：glob 模式位置参数。
     */
    private final String allowlistPattern;
    /**
     * allowlist：{@code --agent} 作用域（默认 {@code *} 表示全部 agent）。
     */
    private final String agent;
    /**
     * 其它 argv。
     */
    private final List<String> extra;

    /**
     * @param b 构建器快照
     */
    private ApprovalsOptions(Builder b) {
        this.verb = b.verb;
        this.node = b.node;
        this.gateway = b.gateway;
        this.url = b.url;
        this.token = b.token;
        this.password = b.password;
        this.timeout = b.timeout;
        this.json = b.json;
        this.file = b.file;
        this.stdin = b.stdin;
        this.allowlistPattern = b.allowlistPattern;
        this.agent = b.agent;
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
            case GET:
                out.add("get");
                break;
            case SET:
                out.add("set");
                OpenClawCliArgv.addIfPresent(out, "--file", file);
                if (stdin) {
                    out.add("--stdin");
                }
                break;
            case ALLOWLIST_ADD:
                out.add("allowlist");
                out.add("add");
                if (allowlistPattern != null && OpenClawStrings.isNotBlank(allowlistPattern)) {
                    out.add(allowlistPattern.trim());
                }
                break;
            case ALLOWLIST_REMOVE:
                out.add("allowlist");
                out.add("remove");
                if (allowlistPattern != null && OpenClawStrings.isNotBlank(allowlistPattern)) {
                    out.add(allowlistPattern.trim());
                }
                break;
            default:
                break;
        }
        OpenClawCliArgv.addIfPresent(out, "--node", node);
        OpenClawCliArgv.addFlag(out, "--gateway", gateway);
        OpenClawCliArgv.addIfPresent(out, "--url", url);
        OpenClawCliArgv.addIfPresent(out, "--token", token);
        OpenClawCliArgv.addIfPresent(out, "--password", password);
        OpenClawCliArgv.addIfPresent(out, "--timeout", timeout);
        OpenClawCliArgv.addIfPresent(out, "--agent", agent);
        OpenClawCliArgv.addFlag(out, "--json", json);
        OpenClawCliArgv.addExtra(out, extra);
        return Collections.unmodifiableList(out);
    }

    /**
     * {@link ApprovalsOptions} 构建器。
     */
    public static final class Builder {
        private Verb verb = Verb.GET;
        private String node;
        private boolean gateway;
        private String url;
        private String token;
        private String password;
        private String timeout;
        private boolean json;
        private String file;
        private boolean stdin;
        private String allowlistPattern;
        private String agent;
        private List<String> extra = new ArrayList<>();

        /**
         * @return {@code this}（{@code approvals get}）
         */
        public Builder get() {
            this.verb = Verb.GET;
            return this;
        }

        /**
         * @return {@code this}（{@code approvals set}）
         */
        public Builder set() {
            this.verb = Verb.SET;
            return this;
        }

        /**
         * @param path set：{@code --file}
         * @return {@code this}
         */
        public Builder file(String path) {
            this.file = path;
            return this;
        }

        /**
         * @param stdin set：{@code --stdin}
         * @return {@code this}
         */
        public Builder stdin(boolean stdin) {
            this.stdin = stdin;
            return this;
        }

        /**
         * @param pattern allowlist add：模式
         * @return {@code this}
         */
        public Builder allowlistAdd(String pattern) {
            this.verb = Verb.ALLOWLIST_ADD;
            this.allowlistPattern = pattern;
            return this;
        }

        /**
         * @param pattern allowlist remove：模式
         * @return {@code this}
         */
        public Builder allowlistRemove(String pattern) {
            this.verb = Verb.ALLOWLIST_REMOVE;
            this.allowlistPattern = pattern;
            return this;
        }

        /**
         * @param node {@code --node}
         * @return {@code this}
         */
        public Builder node(String node) {
            this.node = node;
            return this;
        }

        /**
         * @param gateway {@code --gateway}
         * @return {@code this}
         */
        public Builder gateway(boolean gateway) {
            this.gateway = gateway;
            return this;
        }

        /**
         * @param url {@code --url}
         * @return {@code this}
         */
        public Builder url(String url) {
            this.url = url;
            return this;
        }

        /**
         * @param token {@code --token}
         * @return {@code this}
         */
        public Builder token(String token) {
            this.token = token;
            return this;
        }

        /**
         * @param password {@code --password}
         * @return {@code this}
         */
        public Builder password(String password) {
            this.password = password;
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
         * @param agent {@code --agent}
         * @return {@code this}
         */
        public Builder agent(String agent) {
            this.agent = agent;
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
         * @return 不可变 {@link ApprovalsOptions}
         */
        public ApprovalsOptions build() {
            return new ApprovalsOptions(this);
        }
    }
}
