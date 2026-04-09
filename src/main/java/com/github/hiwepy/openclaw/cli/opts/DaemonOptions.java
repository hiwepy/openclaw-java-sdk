package com.github.hiwepy.openclaw.cli.opts;

import com.github.hiwepy.openclaw.cli.args.CliSubArgs;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * {@code openclaw daemon}：<strong>遗留别名</strong>，与 {@code openclaw gateway} 的服务管理子命令等价（status/install/start/stop/restart/uninstall）。
 * <p>选项与 {@code gateway} 文档「Manage the Gateway service」一致；新集成请优先使用 {@link GatewayCommandOptions} 或官方 gateway CLI。</p>
 *
 * @see <a href="https://docs.openclaw.ai/cli/daemon">daemon CLI</a>
 */
public final class DaemonOptions implements CliSubArgs {

    /**
     * 文档列出的子命令。
     */
    public enum Subcommand {
        STATUS("status"),
        INSTALL("install"),
        UNINSTALL("uninstall"),
        START("start"),
        STOP("stop"),
        RESTART("restart");

        /** CLI 子命令名（小写）。 */
        private final String cliName;

        /**
         * @param cliName 非 null，与 openclaw 一致
         */
        Subcommand(String cliName) {
            this.cliName = cliName;
        }

        /**
         * @return 子命令 token（如 {@code "status"}）
         */
        public String cliName() {
            return cliName;
        }
    }

    /**
     * 服务子命令：文档列出的 {@code status|install|uninstall|start|stop|restart}。
     */
    private final Subcommand subcommand;
    /**
     * 仅 {@link Subcommand#STATUS}：探测用 RPC 共享选项（{@code --url}、{@code --token} 等，见 {@link GatewayRpcOptions}）。
     */
    private final GatewayRpcOptions statusRpc;
    /**
     * 仅 {@link Subcommand#STATUS}：{@code --no-probe}、{@code --deep}、{@code --require-rpc}（与 gateway status 文档一致）。
     */
    private final GatewayCliArgv.GatewayStatusOptions statusExtra;
    /**
     * 仅 {@link Subcommand#INSTALL}：{@code --port} WebSocket 监听端口。
     */
    private final String installPort;
    /**
     * 仅 {@link Subcommand#INSTALL}：{@code --runtime} Node/Bun 等运行时选择。
     */
    private final String installRuntime;
    /**
     * 仅 {@link Subcommand#INSTALL}：{@code --token} 服务安装用令牌（SecretRef 校验行为见 gateway 文档）。
     */
    private final String installToken;
    /**
     * 仅 {@link Subcommand#INSTALL}：{@code --force} 在安装前终止占用端口的旧进程。
     */
    private final boolean installForce;
    /**
     * {@code --json}：各子命令的机器可读输出（文档：生命周期命令均支持）。
     */
    private final boolean json;

    /**
     * @param b 构建器快照
     */
    private DaemonOptions(Builder b) {
        this.subcommand = Objects.requireNonNull(b.subcommand, "subcommand");
        this.statusRpc = b.statusRpc;
        this.statusExtra = b.statusExtra;
        this.installPort = b.installPort;
        this.installRuntime = b.installRuntime;
        this.installToken = b.installToken;
        this.installForce = b.installForce;
        this.json = b.json;
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
        out.add(subcommand.cliName());
        switch (subcommand) {
            case STATUS:
                if (statusRpc != null) {
                    statusRpc.appendSharedFlags(out);
                }
                GatewayCliArgv.GatewayStatusOptions se = statusExtra != null
                        ? statusExtra
                        : GatewayCliArgv.GatewayStatusOptions.none();
                if (se.isNoProbe()) {
                    out.add("--no-probe");
                }
                if (se.isDeep()) {
                    out.add("--deep");
                }
                if (se.isRequireRpc()) {
                    out.add("--require-rpc");
                }
                break;
            case INSTALL:
                if (installPort != null && !installPort.isEmpty()) {
                    out.add("--port");
                    out.add(installPort);
                }
                if (installRuntime != null && !installRuntime.isEmpty()) {
                    out.add("--runtime");
                    out.add(installRuntime);
                }
                if (installToken != null && !installToken.isEmpty()) {
                    out.add("--token");
                    out.add(installToken);
                }
                if (installForce) {
                    out.add("--force");
                }
                break;
            case START:
            case STOP:
            case RESTART:
            case UNINSTALL:
                break;
            default:
                break;
        }
        if (json) {
            out.add("--json");
        }
        return Collections.unmodifiableList(out);
    }

    /**
     * {@link DaemonOptions} 构建器。
     */
    public static final class Builder {

        private Subcommand subcommand;
        private GatewayRpcOptions statusRpc;
        private GatewayCliArgv.GatewayStatusOptions statusExtra;
        private String installPort;
        private String installRuntime;
        private String installToken;
        private boolean installForce;
        private boolean json;

        /**
         * @param subcommand 非 null
         * @return {@code this}
         */
        public Builder subcommand(Subcommand subcommand) {
            this.subcommand = subcommand;
            return this;
        }

        /** 仅 {@link Subcommand#STATUS}：探针与 RPC 共享选项。 */
        public Builder statusRpc(GatewayRpcOptions statusRpc) {
            this.statusRpc = statusRpc;
            return this;
        }

        /** 仅 {@link Subcommand#STATUS}：{@code --no-probe} / {@code --deep} / {@code --require-rpc}。 */
        public Builder statusExtra(GatewayCliArgv.GatewayStatusOptions statusExtra) {
            this.statusExtra = statusExtra;
            return this;
        }

        /**
         * @param installPort {@code daemon install --port}
         * @return {@code this}
         */
        public Builder installPort(String installPort) {
            this.installPort = installPort;
            return this;
        }

        /**
         * @param installRuntime {@code --runtime}
         * @return {@code this}
         */
        public Builder installRuntime(String installRuntime) {
            this.installRuntime = installRuntime;
            return this;
        }

        /**
         * @param installToken {@code --token}（install 场景）
         * @return {@code this}
         */
        public Builder installToken(String installToken) {
            this.installToken = installToken;
            return this;
        }

        /**
         * @param installForce {@code --force}
         * @return {@code this}
         */
        public Builder installForce(boolean installForce) {
            this.installForce = installForce;
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
         * @return 不可变 {@link DaemonOptions}
         */
        public DaemonOptions build() {
            return new DaemonOptions(this);
        }
    }
}
