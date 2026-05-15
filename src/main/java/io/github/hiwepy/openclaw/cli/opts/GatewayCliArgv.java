package io.github.hiwepy.openclaw.cli.opts;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * 生成 {@code openclaw gateway &lt;subcommand&gt; ...} 的参数序列（不含可执行文件名与全局 {@code --dev} 等，由 {@link io.github.hiwepy.openclaw.cli.OpenClawCliRequest} 处理）。
 *
 * @see <a href="https://docs.openclaw.ai/cli/gateway">gateway CLI</a>
 */
public final class GatewayCliArgv {

    private GatewayCliArgv() {
    }

    /**
     * {@code gateway health} 参数，例如文档：
     * {@code openclaw gateway health --url ws://127.0.0.1:18789}
     *
     * @param rpc 非 null，共享 RPC 选项（URL、token 等）
     * @return 不可变参数列表，首元素为 {@code "health"}
     */
    public static List<String> health(GatewayRpcOptions rpc) {
        Objects.requireNonNull(rpc, "rpc");
        List<String> args = new ArrayList<>();
        args.add("health");
        rpc.appendSharedFlags(args);
        return Collections.unmodifiableList(args);
    }

    /**
     * {@code gateway status} 参数；额外选项见 {@link GatewayCliArgv.GatewayStatusOptions}。
     *
     * @param rpc   非 null
     * @param extra 可为 null，等价于 {@link GatewayStatusOptions#none()}
     * @return 不可变参数列表，首元素为 {@code "status"}
     */
    public static List<String> status(GatewayRpcOptions rpc, GatewayStatusOptions extra) {
        Objects.requireNonNull(rpc, "rpc");
        GatewayStatusOptions e = extra != null ? extra : GatewayStatusOptions.none();
        List<String> args = new ArrayList<>();
        args.add("status");
        rpc.appendSharedFlags(args);
        if (e.isNoProbe()) {
            args.add("--no-probe");
        }
        if (e.isDeep()) {
            args.add("--deep");
        }
        if (e.isRequireRpc()) {
            args.add("--require-rpc");
        }
        return Collections.unmodifiableList(args);
    }

    /**
     * {@code gateway probe} 参数；SSH 相关见 {@link GatewayCliArgv.GatewayProbeOptions}。
     *
     * @param rpc   非 null
     * @param extra 可为 null，等价于 {@link GatewayProbeOptions#none()}
     * @return 不可变参数列表，首元素为 {@code "probe"}
     */
    public static List<String> probe(GatewayRpcOptions rpc, GatewayProbeOptions extra) {
        Objects.requireNonNull(rpc, "rpc");
        GatewayProbeOptions p = extra != null ? extra : GatewayProbeOptions.none();
        List<String> args = new ArrayList<>();
        args.add("probe");
        rpc.appendSharedFlags(args);
        if (p.getSsh() != null && !p.getSsh().isEmpty()) {
            args.add("--ssh");
            args.add(p.getSsh());
        }
        if (p.getSshIdentity() != null && !p.getSshIdentity().isEmpty()) {
            args.add("--ssh-identity");
            args.add(p.getSshIdentity());
        }
        if (p.isSshAuto()) {
            args.add("--ssh-auto");
        }
        return Collections.unmodifiableList(args);
    }

    /**
     * {@code gateway status} 特有选项（文档：{@code --no-probe}、{@code --deep}、{@code --require-rpc}）。
     */
    public static final class GatewayStatusOptions {

        /**
         * {@code --no-probe}：只做本机 Gateway 服务（launchd/systemd 等）视图，跳过 WebSocket RPC 探测。
         */
        private final boolean noProbe;
        /**
         * {@code --deep}：扩大扫描范围，尽力发现额外的系统级安装单元（文档：多实例时人类输出会提示清理建议）。
         */
        private final boolean deep;
        /**
         * {@code --require-rpc}：若 RPC 探测失败则非零退出（脚本用：仅有监听服务不够时需 RPC 健康）。
         */
        private final boolean requireRpc;

        /**
         * @param noProbe   {@code --no-probe}
         * @param deep      {@code --deep}
         * @param requireRpc {@code --require-rpc}
         */
        private GatewayStatusOptions(boolean noProbe, boolean deep, boolean requireRpc) {
            this.noProbe = noProbe;
            this.deep = deep;
            this.requireRpc = requireRpc;
        }

        /**
         * @return 全为 false 的默认选项
         */
        public static GatewayStatusOptions none() {
            return new GatewayStatusOptions(false, false, false);
        }

        /**
         * @return 新 {@link Builder}
         */
        public static Builder builder() {
            return new Builder();
        }

        /**
         * @return 是否 {@code --no-probe}
         */
        public boolean isNoProbe() {
            return noProbe;
        }

        /**
         * @return 是否 {@code --deep}
         */
        public boolean isDeep() {
            return deep;
        }

        /**
         * @return 是否 {@code --require-rpc}
         */
        public boolean isRequireRpc() {
            return requireRpc;
        }

        /**
         * {@link GatewayStatusOptions} 构建器。
         */
        public static final class Builder {

            private boolean noProbe;
            private boolean deep;
            private boolean requireRpc;

            /**
             * @param noProbe {@code --no-probe}
             * @return {@code this}
             */
            public Builder noProbe(boolean noProbe) {
                this.noProbe = noProbe;
                return this;
            }

            /**
             * @param deep {@code --deep}
             * @return {@code this}
             */
            public Builder deep(boolean deep) {
                this.deep = deep;
                return this;
            }

            /**
             * @param requireRpc {@code --require-rpc}
             * @return {@code this}
             */
            public Builder requireRpc(boolean requireRpc) {
                this.requireRpc = requireRpc;
                return this;
            }

            /**
             * @return 不可变 {@link GatewayStatusOptions}
             */
            public GatewayStatusOptions build() {
                return new GatewayStatusOptions(noProbe, deep, requireRpc);
            }
        }
    }

    /**
     * {@code gateway probe} 的 SSH 相关选项（文档：{@code --ssh}、{@code --ssh-identity}、{@code --ssh-auto}）。
     */
    public static final class GatewayProbeOptions {

        /**
         * {@code --ssh}：经 SSH 本地端口转发探测远端仅监听 loopback 的 Gateway（文档 Remote over SSH 节，形如 {@code user@host}）。
         */
        private final String ssh;
        /**
         * {@code --ssh-identity}：SSH 登录使用的身份私钥文件路径。
         */
        private final String sshIdentity;
        /**
         * {@code --ssh-auto}：从解析到的发现端点自动选取第一个 gateway host 作为 SSH 目标（文档：TXT 提示单独不足以定目标）。
         */
        private final boolean sshAuto;

        /**
         * @param ssh         {@code --ssh}
         * @param sshIdentity {@code --ssh-identity}
         * @param sshAuto     {@code --ssh-auto}
         */
        private GatewayProbeOptions(String ssh, String sshIdentity, boolean sshAuto) {
            this.ssh = ssh;
            this.sshIdentity = sshIdentity;
            this.sshAuto = sshAuto;
        }

        /**
         * @return 无 SSH 选项的默认值
         */
        public static GatewayProbeOptions none() {
            return new GatewayProbeOptions(null, null, false);
        }

        /**
         * @return 新 {@link Builder}
         */
        public static Builder builder() {
            return new Builder();
        }

        /**
         * @return {@code --ssh}，可为 null
         */
        public String getSsh() {
            return ssh;
        }

        /**
         * @return {@code --ssh-identity}，可为 null
         */
        public String getSshIdentity() {
            return sshIdentity;
        }

        /**
         * @return 是否 {@code --ssh-auto}
         */
        public boolean isSshAuto() {
            return sshAuto;
        }

        /**
         * {@link GatewayProbeOptions} 构建器。
         */
        public static final class Builder {

            private String ssh;
            private String sshIdentity;
            private boolean sshAuto;

            /**
             * @param ssh {@code --ssh}
             * @return {@code this}
             */
            public Builder ssh(String ssh) {
                this.ssh = ssh;
                return this;
            }

            /**
             * @param sshIdentity {@code --ssh-identity}
             * @return {@code this}
             */
            public Builder sshIdentity(String sshIdentity) {
                this.sshIdentity = sshIdentity;
                return this;
            }

            /**
             * @param sshAuto {@code --ssh-auto}
             * @return {@code this}
             */
            public Builder sshAuto(boolean sshAuto) {
                this.sshAuto = sshAuto;
                return this;
            }

            /**
             * @return 不可变 {@link GatewayProbeOptions}
             */
            public GatewayProbeOptions build() {
                return new GatewayProbeOptions(ssh, sshIdentity, sshAuto);
            }
        }
    }
}
