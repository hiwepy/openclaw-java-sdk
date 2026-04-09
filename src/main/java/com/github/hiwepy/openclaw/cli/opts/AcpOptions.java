package com.github.hiwepy.openclaw.cli.opts;

import com.github.hiwepy.openclaw.cli.args.CliSubArgs;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * {@code openclaw acp}：以 OpenClaw 作为 ACP 服务端，经 stdio 接 IDE/客户端，经 WebSocket 转发到 Gateway 会话。
 * <p>用于「编辑器要讲 ACP 给 OpenClaw」；若要让外部 MCP 客户端直连频道会话，应使用 {@code openclaw mcp serve}。
 * 设置 {@code --url} 时须显式传 {@code --token} 或 {@code --password}（或文件变体），与网关侧其它客户端一致。</p>
 *
 * @see <a href="https://docs.openclaw.ai/cli/acp">acp CLI</a>
 */
public final class AcpOptions implements CliSubArgs {

    /**
     * {@code acp} 子模式：默认 bridge，或调试用的 {@code acp client}。
     */
    public enum Mode {
        /** stdio 与 Gateway WebSocket 之间的 ACP 桥接（默认）。 */
        BRIDGE,
        /** 内置 ACP 客户端：拉起 bridge 并交互输入，用于无 IDE 时的冒烟。 */
        CLIENT
    }

    /** 当前为桥接模式还是 {@code client} 调试子命令。 */
    private final Mode mode;
    /**
     * {@code --url}：目标 Gateway WebSocket URL；未传时可走配置中的 {@code gateway.remote.url}（见 acp 文档）。
     */
    private final String url;
    /**
     * {@code --token}：网关 token；内联值可能出现在本机进程列表，生产环境优先 {@code --token-file} 或环境变量。
     */
    private final String token;
    /**
     * {@code --token-file}：从文件读取网关 token，避免内联泄露。
     */
    private final String tokenFile;
    /**
     * {@code --password}：网关密码认证；同样建议优先文件或环境变量。
     */
    private final String password;
    /**
     * {@code --password-file}：从文件读取网关密码。
     */
    private final String passwordFile;
    /**
     * {@code --session}：默认绑定的 Gateway 会话键（agent 作用域会话由键前缀区分，见 acp 文档 Selecting agents）。
     */
    private final String session;
    /**
     * {@code --session-label}：按标签解析已存在会话；与 {@code --session} 二选一语义见文档 Session mapping。
     */
    private final String sessionLabel;
    /**
     * {@code --require-existing}：若会话键或标签不存在则失败，避免静默新建。
     */
    private final boolean requireExisting;
    /**
     * {@code --reset-session}：在首次 prompt 前重置该键对应会话 id（保留键，换新 transcript）。
     */
    private final boolean resetSession;
    /**
     * {@code --no-prefix-cwd}：不在用户 prompt 前自动加上当前工作目录前缀。
     */
    private final boolean noPrefixCwd;
    /**
     * {@code --provenance}：附带 ACP 侧来源/收据类元数据（见 acp Options）。
     */
    private final String provenance;
    /**
     * {@code --verbose} / {@code -v}：向 stderr 打印更详细的桥接日志。
     */
    private final boolean verbose;
    /**
     * {@code acp client --cwd}：ACP 会话的工作目录，影响只读自动批准等调试策略的作用域。
     */
    private final String cwd;
    /**
     * {@code acp client --server}：启动 ACP 服务端子进程的命令（默认 {@code openclaw}）。
     */
    private final String server;
    /**
     * {@code acp client --server-args} 之后展开的参数列表，会原样传给服务端命令（例如附加 {@code acp --url ...}）。
     */
    private final List<String> serverArgs;
    /**
     * {@code acp client --server-verbose}：为被拉起的 ACP 服务端进程打开 verbose 日志。
     */
    private final boolean serverVerbose;
    /**
     * 文档未单独建模的 argv 片段，按顺序追加在末尾。
     */
    private final List<String> extra;

    /**
     * @param b 构建器快照
     */
    private AcpOptions(Builder b) {
        this.mode = b.mode;
        this.url = b.url;
        this.token = b.token;
        this.tokenFile = b.tokenFile;
        this.password = b.password;
        this.passwordFile = b.passwordFile;
        this.session = b.session;
        this.sessionLabel = b.sessionLabel;
        this.requireExisting = b.requireExisting;
        this.resetSession = b.resetSession;
        this.noPrefixCwd = b.noPrefixCwd;
        this.provenance = b.provenance;
        this.verbose = b.verbose;
        this.cwd = b.cwd;
        this.server = b.server;
        this.serverArgs = b.serverArgs == null ? List.of() : List.copyOf(b.serverArgs);
        this.serverVerbose = b.serverVerbose;
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
        if (mode == Mode.CLIENT) {
            out.add("client");
            OpenClawCliArgv.addIfPresent(out, "--cwd", cwd);
            OpenClawCliArgv.addIfPresent(out, "--server", server);
            if (!serverArgs.isEmpty()) {
                out.add("--server-args");
                out.addAll(serverArgs);
            }
            OpenClawCliArgv.addFlag(out, "--server-verbose", serverVerbose);
            OpenClawCliArgv.addFlag(out, "--verbose", verbose);
            OpenClawCliArgv.addExtra(out, extra);
            return Collections.unmodifiableList(out);
        }
        OpenClawCliArgv.addIfPresent(out, "--url", url);
        OpenClawCliArgv.addIfPresent(out, "--token", token);
        OpenClawCliArgv.addIfPresent(out, "--token-file", tokenFile);
        OpenClawCliArgv.addIfPresent(out, "--password", password);
        OpenClawCliArgv.addIfPresent(out, "--password-file", passwordFile);
        OpenClawCliArgv.addIfPresent(out, "--session", session);
        OpenClawCliArgv.addIfPresent(out, "--session-label", sessionLabel);
        OpenClawCliArgv.addFlag(out, "--require-existing", requireExisting);
        OpenClawCliArgv.addFlag(out, "--reset-session", resetSession);
        OpenClawCliArgv.addFlag(out, "--no-prefix-cwd", noPrefixCwd);
        OpenClawCliArgv.addIfPresent(out, "--provenance", provenance);
        OpenClawCliArgv.addFlag(out, "--verbose", verbose);
        OpenClawCliArgv.addExtra(out, extra);
        return Collections.unmodifiableList(out);
    }

    /**
     * {@link AcpOptions} 构建器。
     */
    public static final class Builder {
        private Mode mode = Mode.BRIDGE;
        private String url;
        private String token;
        private String tokenFile;
        private String password;
        private String passwordFile;
        private String session;
        private String sessionLabel;
        private boolean requireExisting;
        private boolean resetSession;
        private boolean noPrefixCwd;
        private String provenance;
        private boolean verbose;
        private String cwd;
        private String server;
        private List<String> serverArgs = new ArrayList<>();
        private boolean serverVerbose;
        private List<String> extra = new ArrayList<>();

        /**
         * @return {@code this}（bridge 模式）
         */
        public Builder bridge() {
            this.mode = Mode.BRIDGE;
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
         * @param tokenFile {@code --token-file}
         * @return {@code this}
         */
        public Builder tokenFile(String tokenFile) {
            this.tokenFile = tokenFile;
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
         * @param passwordFile {@code --password-file}
         * @return {@code this}
         */
        public Builder passwordFile(String passwordFile) {
            this.passwordFile = passwordFile;
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
         * @param sessionLabel {@code --session-label}
         * @return {@code this}
         */
        public Builder sessionLabel(String sessionLabel) {
            this.sessionLabel = sessionLabel;
            return this;
        }

        /**
         * @param requireExisting {@code --require-existing}
         * @return {@code this}
         */
        public Builder requireExisting(boolean requireExisting) {
            this.requireExisting = requireExisting;
            return this;
        }

        /**
         * @param resetSession {@code --reset-session}
         * @return {@code this}
         */
        public Builder resetSession(boolean resetSession) {
            this.resetSession = resetSession;
            return this;
        }

        /**
         * @param noPrefixCwd {@code --no-prefix-cwd}
         * @return {@code this}
         */
        public Builder noPrefixCwd(boolean noPrefixCwd) {
            this.noPrefixCwd = noPrefixCwd;
            return this;
        }

        /**
         * @param provenance {@code --provenance}
         * @return {@code this}
         */
        public Builder provenance(String provenance) {
            this.provenance = provenance;
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
         * @return {@code this}（client 子命令）
         */
        public Builder client() {
            this.mode = Mode.CLIENT;
            return this;
        }

        /**
         * @param cwd client：{@code --cwd}
         * @return {@code this}
         */
        public Builder cwd(String cwd) {
            this.cwd = cwd;
            return this;
        }

        /**
         * @param server client：{@code --server}
         * @return {@code this}
         */
        public Builder server(String server) {
            this.server = server;
            return this;
        }

        /**
         * @param token 追加到 {@code --server-args} 的单段参数
         * @return {@code this}
         */
        public Builder addServerArg(String token) {
            if (token != null && !token.isBlank()) {
                serverArgs.add(token.trim());
            }
            return this;
        }

        /**
         * @param serverVerbose client：{@code --server-verbose}
         * @return {@code this}
         */
        public Builder serverVerbose(boolean serverVerbose) {
            this.serverVerbose = serverVerbose;
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
         * @return 不可变 {@link AcpOptions}
         */
        public AcpOptions build() {
            return new AcpOptions(this);
        }
    }
}
