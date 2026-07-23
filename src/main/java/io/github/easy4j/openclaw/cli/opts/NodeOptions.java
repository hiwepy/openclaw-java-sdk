package io.github.easy4j.openclaw.cli.opts;

import io.github.easy4j.openclaw.util.OpenClawLists;
import io.github.easy4j.openclaw.cli.args.CliSubArgs;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * {@code openclaw node}：无头 node host，连接 Gateway WebSocket 并在本机暴露 {@code system.run} / {@code system.which} 等执行面。
 * <p>首次连接会在 Gateway 上创建 {@code role: node} 的待配对请求，需用 {@code openclaw devices approve} 批准。
 * {@code run} 与 {@code install} 的网关认证只解析环境变量与本地/remote 配置，不接受 CLI 内联 {@code --token}（见 node 文档 Gateway auth 节）。</p>
 *
 * @see <a href="https://docs.openclaw.ai/cli/node">node CLI</a>
 */
public final class NodeOptions implements CliSubArgs {

    /**
     * node 子命令：前台运行、安装用户服务或管理服务生命周期。
     */
    public enum Verb {
        /** {@code node run}：前台 node host。 */
        RUN,
        /** {@code node install}：安装后台用户服务。 */
        INSTALL,
        /** {@code node status}：查看服务状态。 */
        STATUS,
        /** {@code node stop}：停止服务。 */
        STOP,
        /** {@code node restart}：重启服务。 */
        RESTART,
        /** {@code node uninstall}：卸载服务单元。 */
        UNINSTALL
    }

    /** run / install / 服务管理 之一。 */
    private final Verb verb;
    /**
     * run / install：{@code --host} Gateway WebSocket 主机（默认 loopback）。
     */
    private final String host;
    /**
     * run / install：{@code --port} Gateway WebSocket 端口（默认 18789）。
     */
    private final String port;
    /**
     * run / install：{@code --tls} 使用 TLS 连接网关。
     */
    private final boolean tls;
    /**
     * run / install：{@code --tls-fingerprint} 期望的服务器证书 sha256 指纹。
     */
    private final String tlsFingerprint;
    /**
     * run / install：{@code --node-id} 覆盖节点 id（会清配对 token，见文档）。
     */
    private final String nodeId;
    /**
     * run / install：{@code --display-name} 覆盖节点展示名。
     */
    private final String displayName;
    /**
     * install：{@code --runtime} 服务运行时（{@code node} 或 {@code bun}）。
     */
    private final String runtime;
    /**
     * install：{@code --force} 覆盖已存在安装。
     */
    private final boolean force;
    /**
     * status / stop / restart / uninstall：{@code --json} 机器可读输出。
     */
    private final boolean json;
    /**
     * 其它 argv。
     */
    private final List<String> extra;

    /**
     * @param b 构建器快照
     */
    private NodeOptions(Builder b) {
        this.verb = b.verb;
        this.host = b.host;
        this.port = b.port;
        this.tls = b.tls;
        this.tlsFingerprint = b.tlsFingerprint;
        this.nodeId = b.nodeId;
        this.displayName = b.displayName;
        this.runtime = b.runtime;
        this.force = b.force;
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
            case RUN:
                out.add("run");
                break;
            case INSTALL:
                out.add("install");
                break;
            case STATUS:
                out.add("status");
                break;
            case STOP:
                out.add("stop");
                break;
            case RESTART:
                out.add("restart");
                break;
            case UNINSTALL:
                out.add("uninstall");
                break;
            default:
                break;
        }
        if (verb == Verb.RUN || verb == Verb.INSTALL) {
            OpenClawCliArgv.addIfPresent(out, "--host", host);
            OpenClawCliArgv.addIfPresent(out, "--port", port);
            OpenClawCliArgv.addFlag(out, "--tls", tls);
            OpenClawCliArgv.addIfPresent(out, "--tls-fingerprint", tlsFingerprint);
            OpenClawCliArgv.addIfPresent(out, "--node-id", nodeId);
            OpenClawCliArgv.addIfPresent(out, "--display-name", displayName);
            if (verb == Verb.INSTALL) {
                OpenClawCliArgv.addIfPresent(out, "--runtime", runtime);
                OpenClawCliArgv.addFlag(out, "--force", force);
            }
        }
        if (verb == Verb.STATUS || verb == Verb.STOP || verb == Verb.RESTART || verb == Verb.UNINSTALL) {
            OpenClawCliArgv.addFlag(out, "--json", json);
        }
        OpenClawCliArgv.addExtra(out, extra);
        return Collections.unmodifiableList(out);
    }

    /**
     * {@link NodeOptions} 构建器。
     */
    public static final class Builder {
        private Verb verb = Verb.RUN;
        private String host;
        private String port;
        private boolean tls;
        private String tlsFingerprint;
        private String nodeId;
        private String displayName;
        private String runtime;
        private boolean force;
        private boolean json;
        private List<String> extra = new ArrayList<>();

        /**
         * @return {@code this}（{@code node run}）
         */
        public Builder run() {
            this.verb = Verb.RUN;
            return this;
        }

        /**
         * @return {@code this}（{@code node install}）
         */
        public Builder install() {
            this.verb = Verb.INSTALL;
            return this;
        }

        /**
         * @return {@code this}（{@code node status}）
         */
        public Builder status() {
            this.verb = Verb.STATUS;
            return this;
        }

        /**
         * @return {@code this}（{@code node stop}）
         */
        public Builder stop() {
            this.verb = Verb.STOP;
            return this;
        }

        /**
         * @return {@code this}（{@code node restart}）
         */
        public Builder restart() {
            this.verb = Verb.RESTART;
            return this;
        }

        /**
         * @return {@code this}（{@code node uninstall}）
         */
        public Builder uninstall() {
            this.verb = Verb.UNINSTALL;
            return this;
        }

        /**
         * @param host {@code --host}
         * @return {@code this}
         */
        public Builder host(String host) {
            this.host = host;
            return this;
        }

        /**
         * @param port {@code --port}
         * @return {@code this}
         */
        public Builder port(String port) {
            this.port = port;
            return this;
        }

        /**
         * @param tls {@code --tls}
         * @return {@code this}
         */
        public Builder tls(boolean tls) {
            this.tls = tls;
            return this;
        }

        /**
         * @param fingerprint {@code --tls-fingerprint}
         * @return {@code this}
         */
        public Builder tlsFingerprint(String fingerprint) {
            this.tlsFingerprint = fingerprint;
            return this;
        }

        /**
         * @param nodeId {@code --node-id}
         * @return {@code this}
         */
        public Builder nodeId(String nodeId) {
            this.nodeId = nodeId;
            return this;
        }

        /**
         * @param displayName {@code --display-name}
         * @return {@code this}
         */
        public Builder displayName(String displayName) {
            this.displayName = displayName;
            return this;
        }

        /**
         * @param runtime install：{@code --runtime}
         * @return {@code this}
         */
        public Builder runtime(String runtime) {
            this.runtime = runtime;
            return this;
        }

        /**
         * @param force install：{@code --force}
         * @return {@code this}
         */
        public Builder force(boolean force) {
            this.force = force;
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
         * @return 不可变 {@link NodeOptions}
         */
        public NodeOptions build() {
            return new NodeOptions(this);
        }
    }
}
