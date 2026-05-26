package io.github.hiwepy.openclaw.cli.opts;

import io.github.hiwepy.openclaw.util.OpenClawLists;
import io.github.hiwepy.openclaw.util.OpenClawStrings;
import io.github.hiwepy.openclaw.cli.args.CliSubArgs;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * {@code openclaw nodes}：管理已配对 node host（列表、审批、重命名、状态）并通过 {@code invoke} 调用其能力面。
 * <p>{@code system.run} 等 shell 执行应使用 exec 工具 {@code host=node}；{@code nodes invoke} 聚焦相机、截屏、通知等能力 RPC。
 * 共享 {@code --url}、{@code --token}、{@code --password}、{@code --timeout}、{@code --json} 与 devices 文档同类网关查询选项一致。</p>
 *
 * @see <a href="https://docs.openclaw.ai/cli/nodes">nodes CLI</a>
 */
public final class NodesOptions implements CliSubArgs {

    /**
     * nodes 子命令：列举与过滤、处理待配对、改名、查看状态或直接 invoke。
     */
    public enum Verb {
        /** {@code nodes list}：待配对与已配对表，可 {@code --connected} 或 {@code --last-connected} 过滤。 */
        LIST,
        /** {@code nodes pending}：仅列出待审批（需 pairing scope）。 */
        PENDING,
        /** {@code nodes approve}：批准请求（额外 scope 需求随请求类型变化）。 */
        APPROVE,
        /** {@code nodes reject}：拒绝请求。 */
        REJECT,
        /** {@code nodes rename}：修改显示名。 */
        RENAME,
        /** {@code nodes status}：与 list 类似的状态视图。 */
        STATUS,
        /** {@code nodes invoke}：向节点发送命名 command 与 JSON params。 */
        INVOKE
    }

    /** 当前 nodes 子命令。 */
    private final Verb verb;
    /**
     * list / status：{@code --connected} 只显示当前在线节点。
     */
    private final boolean listConnected;
    /**
     * list / status：{@code --last-connected} 过滤最近若干时间内连过的节点（如 {@code 24h}）。
     */
    private final String lastConnected;
    /**
     * approve / reject：配对请求 id。
     */
    private final String requestId;
    /**
     * rename / invoke：{@code --node} 目标选择器（id、名称或 IP，见文档）。
     */
    private final String nodeRef;
    /**
     * rename：{@code --name} 新显示名。
     */
    private final String name;
    /**
     * invoke：{@code --command} 能力命令名。
     */
    private final String command;
    /**
     * invoke：{@code --params} JSON 对象字符串，默认 {@code {}}。
     */
    private final String paramsJson;
    /**
     * invoke：{@code --invoke-timeout} 调用超时毫秒（默认约 15000）。
     */
    private final String invokeTimeout;
    /**
     * invoke：{@code --idempotency-key} 可选幂等键。
     */
    private final String idempotencyKey;
    /**
     * 全局：{@code --url} 显式 Gateway WebSocket。
     */
    private final String url;
    /**
     * 全局：{@code --token}。
     */
    private final String token;
    /**
     * 全局：{@code --password}。
     */
    private final String password;
    /**
     * 全局：{@code --timeout} RPC 预算。
     */
    private final String timeout;
    /**
     * 全局：{@code --json}。
     */
    private final boolean json;
    /**
     * 其它 argv。
     */
    private final List<String> extra;

    /**
     * @param b 构建器快照
     */
    private NodesOptions(Builder b) {
        this.verb = b.verb;
        this.listConnected = b.listConnected;
        this.lastConnected = b.lastConnected;
        this.requestId = b.requestId;
        this.nodeRef = b.nodeRef;
        this.name = b.name;
        this.command = b.command;
        this.paramsJson = b.paramsJson;
        this.invokeTimeout = b.invokeTimeout;
        this.idempotencyKey = b.idempotencyKey;
        this.url = b.url;
        this.token = b.token;
        this.password = b.password;
        this.timeout = b.timeout;
        this.json = b.json;
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
            case LIST:
                out.add("list");
                OpenClawCliArgv.addFlag(out, "--connected", listConnected);
                OpenClawCliArgv.addIfPresent(out, "--last-connected", lastConnected);
                break;
            case PENDING:
                out.add("pending");
                break;
            case APPROVE:
                out.add("approve");
                if (requestId != null && OpenClawStrings.isNotBlank(requestId)) {
                    out.add(requestId.trim());
                }
                break;
            case REJECT:
                out.add("reject");
                if (requestId != null && OpenClawStrings.isNotBlank(requestId)) {
                    out.add(requestId.trim());
                }
                break;
            case RENAME:
                out.add("rename");
                OpenClawCliArgv.addIfPresent(out, "--node", nodeRef);
                OpenClawCliArgv.addIfPresent(out, "--name", name);
                break;
            case STATUS:
                out.add("status");
                OpenClawCliArgv.addFlag(out, "--connected", listConnected);
                OpenClawCliArgv.addIfPresent(out, "--last-connected", lastConnected);
                break;
            case INVOKE:
                out.add("invoke");
                OpenClawCliArgv.addIfPresent(out, "--node", nodeRef);
                OpenClawCliArgv.addIfPresent(out, "--command", command);
                OpenClawCliArgv.addIfPresent(out, "--params", paramsJson);
                OpenClawCliArgv.addIfPresent(out, "--invoke-timeout", invokeTimeout);
                OpenClawCliArgv.addIfPresent(out, "--idempotency-key", idempotencyKey);
                break;
            default:
                break;
        }
        OpenClawCliArgv.addIfPresent(out, "--url", url);
        OpenClawCliArgv.addIfPresent(out, "--token", token);
        OpenClawCliArgv.addIfPresent(out, "--password", password);
        OpenClawCliArgv.addIfPresent(out, "--timeout", timeout);
        OpenClawCliArgv.addFlag(out, "--json", json);
        OpenClawCliArgv.addExtra(out, extra);
        return Collections.unmodifiableList(out);
    }

    /**
     * {@link NodesOptions} 构建器。
     */
    public static final class Builder {
        private Verb verb = Verb.LIST;
        private boolean listConnected;
        private String lastConnected;
        private String requestId;
        private String nodeRef;
        private String name;
        private String command;
        private String paramsJson;
        private String invokeTimeout;
        private String idempotencyKey;
        private String url;
        private String token;
        private String password;
        private String timeout;
        private boolean json;
        private List<String> extra = new ArrayList<>();

        /**
         * @return {@code this}（{@code nodes list}）
         */
        public Builder list() {
            this.verb = Verb.LIST;
            return this;
        }

        /**
         * @param connected list：{@code --connected}
         * @return {@code this}
         */
        public Builder listConnected(boolean connected) {
            this.listConnected = connected;
            return this;
        }

        /**
         * @param duration list：{@code --last-connected}
         * @return {@code this}
         */
        public Builder lastConnected(String duration) {
            this.lastConnected = duration;
            return this;
        }

        /**
         * @return {@code this}（{@code nodes pending}）
         */
        public Builder pending() {
            this.verb = Verb.PENDING;
            return this;
        }

        /**
         * @param requestId 请求 ID
         * @return {@code this}
         */
        public Builder approve(String requestId) {
            this.verb = Verb.APPROVE;
            this.requestId = requestId;
            return this;
        }

        /**
         * @param requestId 请求 ID
         * @return {@code this}
         */
        public Builder reject(String requestId) {
            this.verb = Verb.REJECT;
            this.requestId = requestId;
            return this;
        }

        /**
         * @param nodeRef {@code --node}
         * @param displayName {@code --name}
         * @return {@code this}
         */
        public Builder rename(String nodeRef, String displayName) {
            this.verb = Verb.RENAME;
            this.nodeRef = nodeRef;
            this.name = displayName;
            return this;
        }

        /**
         * @return {@code this}（{@code nodes status}）
         */
        public Builder status() {
            this.verb = Verb.STATUS;
            return this;
        }

        /**
         * @param nodeRef {@code --node}
         * @param command {@code --command}
         * @return {@code this}
         */
        public Builder invoke(String nodeRef, String command) {
            this.verb = Verb.INVOKE;
            this.nodeRef = nodeRef;
            this.command = command;
            return this;
        }

        /**
         * @param json {@code --params}
         * @return {@code this}
         */
        public Builder paramsJson(String json) {
            this.paramsJson = json;
            return this;
        }

        /**
         * @param ms invoke：{@code --invoke-timeout}
         * @return {@code this}
         */
        public Builder invokeTimeout(String ms) {
            this.invokeTimeout = ms;
            return this;
        }

        /**
         * @param key {@code --idempotency-key}
         * @return {@code this}
         */
        public Builder idempotencyKey(String key) {
            this.idempotencyKey = key;
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
         * @return 不可变 {@link NodesOptions}
         */
        public NodesOptions build() {
            return new NodesOptions(this);
        }
    }
}
