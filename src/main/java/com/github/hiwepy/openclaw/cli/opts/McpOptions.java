package com.github.hiwepy.openclaw.cli.opts;

import com.github.hiwepy.openclaw.cli.args.CliSubArgs;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * {@code openclaw mcp}：{@code mcp serve} 让 OpenClaw 作为 stdio MCP 服务端把 Gateway 已路由的会话暴露给外部客户端；
 * {@code list|show|set|unset} 则维护配置中的 {@code mcp.servers} 注册表（不启动桥接、也不探测远端可达性）。
 *
 * @see <a href="https://docs.openclaw.ai/cli/mcp">mcp CLI</a>
 */
public final class McpOptions implements CliSubArgs {

    /**
     * {@code mcp serve --claude-channel-mode}：是否为理解 Claude 通知协议的客户端开启额外推送通道。
     */
    public enum ClaudeChannelMode {
        /** {@code off}：仅标准 MCP 工具，无 Claude 专用通知。 */
        OFF("off"),
        /** {@code on}：启用 {@code notifications/claude/channel} 等实验能力。 */
        ON("on"),
        /** {@code auto}：当前与 {@code on} 行为相同（文档：尚无客户端能力探测）。 */
        AUTO("auto");

        private final String cliValue;

        ClaudeChannelMode(String cliValue) {
            this.cliValue = cliValue;
        }

        String cliValue() {
            return cliValue;
        }
    }

    /**
     * mcp 子命令：stdio 桥接 serve，或读写配置内 MCP server 定义。
     */
    public enum Mode {
        /** {@code mcp serve}：连接 Gateway 并在 MCP 会话存活期间维护内存事件队列。 */
        SERVE,
        /** {@code mcp list}：列出 {@code mcp.servers} 名称。 */
        LIST,
        /** {@code mcp show}：打印单个或全部 server JSON。 */
        SHOW,
        /** {@code mcp set}：写入一条 server 定义（JSON 对象字符串）。 */
        SET,
        /** {@code mcp unset}：删除命名 server。 */
        UNSET
    }

    /** serve 或 registry 子命令之一。 */
    private final Mode mode;
    /**
     * serve：{@code --url} 目标 Gateway WebSocket（与 acp 一样建议显式凭据）。
     */
    private final String url;
    /**
     * serve：{@code --token} 网关 token（优先文件或环境变量以避免进程列表泄露）。
     */
    private final String token;
    /**
     * serve：{@code --token-file} 从文件读取 token。
     */
    private final String tokenFile;
    /**
     * serve：{@code --password} 网关密码。
     */
    private final String password;
    /**
     * serve：{@code --password-file} 从文件读取密码。
     */
    private final String passwordFile;
    /**
     * serve：{@code --claude-channel-mode} 见 {@link ClaudeChannelMode}。
     */
    private final ClaudeChannelMode claudeChannelMode;
    /**
     * serve：{@code --verbose} 在 stderr 打印桥接诊断日志。
     */
    private final boolean verbose;
    /**
     * show：server 名称位置参数；省略时打印完整对象（文档语义）。
     */
    private final String showName;
    /**
     * show：{@code --json}。
     */
    private final boolean showJson;
    /**
     * set：server 名称位置参数。
     */
    private final String setName;
    /**
     * set：紧随其后的 JSON 对象字面量（整条 server 配置）。
     */
    private final String setJson;
    /**
     * unset：要删除的 server 名。
     */
    private final String unsetName;
    /**
     * 其它 argv。
     */
    private final List<String> extra;

    /**
     * @param b 构建器快照
     */
    private McpOptions(Builder b) {
        this.mode = b.mode;
        this.url = b.url;
        this.token = b.token;
        this.tokenFile = b.tokenFile;
        this.password = b.password;
        this.passwordFile = b.passwordFile;
        this.claudeChannelMode = b.claudeChannelMode;
        this.verbose = b.verbose;
        this.showName = b.showName;
        this.showJson = b.showJson;
        this.setName = b.setName;
        this.setJson = b.setJson;
        this.unsetName = b.unsetName;
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
        switch (mode) {
            case SERVE:
                out.add("serve");
                OpenClawCliArgv.addIfPresent(out, "--url", url);
                OpenClawCliArgv.addIfPresent(out, "--token", token);
                OpenClawCliArgv.addIfPresent(out, "--token-file", tokenFile);
                OpenClawCliArgv.addIfPresent(out, "--password", password);
                OpenClawCliArgv.addIfPresent(out, "--password-file", passwordFile);
                if (claudeChannelMode != null) {
                    out.add("--claude-channel-mode");
                    out.add(claudeChannelMode.cliValue());
                }
                OpenClawCliArgv.addFlag(out, "--verbose", verbose);
                break;
            case LIST:
                out.add("list");
                break;
            case SHOW:
                out.add("show");
                if (showName != null && !showName.isBlank()) {
                    out.add(showName.trim());
                }
                OpenClawCliArgv.addFlag(out, "--json", showJson);
                break;
            case SET:
                out.add("set");
                if (setName != null && !setName.isBlank()) {
                    out.add(setName.trim());
                }
                if (setJson != null && !setJson.isBlank()) {
                    out.add(setJson);
                }
                break;
            case UNSET:
                out.add("unset");
                if (unsetName != null && !unsetName.isBlank()) {
                    out.add(unsetName.trim());
                }
                break;
            default:
                break;
        }
        OpenClawCliArgv.addExtra(out, extra);
        return Collections.unmodifiableList(out);
    }

    /**
     * {@link McpOptions} 构建器。
     */
    public static final class Builder {
        private Mode mode = Mode.SERVE;
        private String url;
        private String token;
        private String tokenFile;
        private String password;
        private String passwordFile;
        private ClaudeChannelMode claudeChannelMode;
        private boolean verbose;
        private String showName;
        private boolean showJson;
        private String setName;
        private String setJson;
        private String unsetName;
        private List<String> extra = new ArrayList<>();

        /**
         * @return {@code this}（{@code mcp serve}）
         */
        public Builder serve() {
            this.mode = Mode.SERVE;
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
         * @param path {@code --token-file}
         * @return {@code this}
         */
        public Builder tokenFile(String path) {
            this.tokenFile = path;
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
         * @param path {@code --password-file}
         * @return {@code this}
         */
        public Builder passwordFile(String path) {
            this.passwordFile = path;
            return this;
        }

        /**
         * @param mode {@code --claude-channel-mode}
         * @return {@code this}
         */
        public Builder claudeChannelMode(ClaudeChannelMode mode) {
            this.claudeChannelMode = mode;
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
         * @return {@code this}（{@code mcp list}）
         */
        public Builder list() {
            this.mode = Mode.LIST;
            return this;
        }

        /**
         * {@code show}；{@code name} 为空时等价于文档「无名称」展示完整对象。
         *
         * @param name MCP 名称（可为 null）
         * @return {@code this}
         */
        public Builder show(String name) {
            this.mode = Mode.SHOW;
            this.showName = name;
            return this;
        }

        /**
         * @param json show：{@code --json}
         * @return {@code this}
         */
        public Builder showJson(boolean json) {
            this.showJson = json;
            return this;
        }

        /**
         * {@code set <name> <json>}，{@code json} 为单行 JSON 字符串。
         *
         * @param name MCP 名称
         * @param json 单行 JSON
         * @return {@code this}
         */
        public Builder set(String name, String json) {
            this.mode = Mode.SET;
            this.setName = name;
            this.setJson = json;
            return this;
        }

        /**
         * @param name unset：名称
         * @return {@code this}
         */
        public Builder unset(String name) {
            this.mode = Mode.UNSET;
            this.unsetName = name;
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
         * @return 不可变 {@link McpOptions}
         */
        public McpOptions build() {
            return new McpOptions(this);
        }
    }
}
