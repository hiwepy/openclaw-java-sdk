package io.github.easy4j.openclaw.cli.opts;

import io.github.easy4j.openclaw.cli.args.CliSubArgs;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * {@code openclaw exec-approvals}（别名 {@code approvals}）：管理 exec 审批（gateway 或 node host）。
 * <p>
 * 支持 {@code get}、{@code set}、{@code allowlist add <pattern>}、{@code allowlist remove <pattern>} 子命令。
 * 共享选项 {@code --node}、{@code --gateway}；{@code set} 支持 {@code --file}/{@code --stdin}；
 * {@code allowlist} 支持 {@code --agent}。
 * </p>
 *
 * @see <a href="https://docs.openclaw.ai/cli/exec-approvals">exec-approvals CLI</a>
 */
public final class ExecApprovalsOptions implements CliSubArgs {

    /** 子命令模式。 */
    public enum Mode {
        /** {@code get}：读取审批快照。 */
        GET,
        /** {@code set}：上传审批 JSON。 */
        SET,
        /** {@code allowlist add <pattern>}：添加允许模式。 */
        ALLOWLIST_ADD,
        /** {@code allowlist remove <pattern>}：移除允许模式。 */
        ALLOWLIST_REMOVE,
        /** 默认（无子命令）：列出审批（父命令默认动作）。 */
        DEFAULT
    }

    private final Mode mode;
    /** allowlist add/remove：位置参数 {@code <pattern>}。 */
    private final String pattern;
    /** {@code --node}：目标节点 id/名称/IP。 */
    private final String node;
    /** {@code --gateway}：强制 gateway 审批。 */
    private final boolean gateway;
    /** set：{@code --file} 上传的 JSON 文件路径。 */
    private final String file;
    /** set：{@code --stdin} 从标准输入读取 JSON。 */
    private final boolean stdin;
    /** allowlist：{@code --agent} agent id（默认 {@code *}）。 */
    private final String agent;

    private ExecApprovalsOptions(Builder b) {
        this.mode = b.mode;
        this.pattern = b.pattern;
        this.node = b.node;
        this.gateway = b.gateway;
        this.file = b.file;
        this.stdin = b.stdin;
        this.agent = b.agent;
    }

    /**
     * @return 新 {@link Builder}（默认 {@link Mode#DEFAULT}）
     */
    public static Builder builder() {
        return new Builder();
    }

    @Override
    public List<String> toSubcommandArguments() {
        List<String> out = new ArrayList<>();
        switch (mode) {
            case GET:
                out.add("get");
                break;
            case SET:
                out.add("set");
                break;
            case ALLOWLIST_ADD:
                out.add("allowlist");
                out.add("add");
                if (pattern != null && !pattern.isEmpty()) {
                    out.add(pattern);
                }
                break;
            case ALLOWLIST_REMOVE:
                out.add("allowlist");
                out.add("remove");
                if (pattern != null && !pattern.isEmpty()) {
                    out.add(pattern);
                }
                break;
            case DEFAULT:
            default:
                // 父命令默认动作：不输出子命令 token
                break;
        }
        OpenClawCliArgv.addIfPresent(out, "--node", node);
        OpenClawCliArgv.addFlag(out, "--gateway", gateway);
        OpenClawCliArgv.addIfPresent(out, "--file", file);
        OpenClawCliArgv.addFlag(out, "--stdin", stdin);
        OpenClawCliArgv.addIfPresent(out, "--agent", agent);
        return Collections.unmodifiableList(out);
    }

    /**
     * {@link ExecApprovalsOptions} 构建器。
     */
    public static final class Builder {
        private Mode mode = Mode.DEFAULT;
        private String pattern;
        private String node;
        private boolean gateway;
        private String file;
        private boolean stdin;
        private String agent;

        /** 切换为 {@code get} 子命令。 */
        public Builder get() { this.mode = Mode.GET; return this; }
        /** 切换为 {@code set} 子命令。 */
        public Builder set() { this.mode = Mode.SET; return this; }
        /** 切换为 {@code allowlist add <pattern>} 子命令。 */
        public Builder allowlistAdd(String pattern) { this.mode = Mode.ALLOWLIST_ADD; this.pattern = pattern; return this; }
        /** 切换为 {@code allowlist remove <pattern>} 子命令。 */
        public Builder allowlistRemove(String pattern) { this.mode = Mode.ALLOWLIST_REMOVE; this.pattern = pattern; return this; }
        /** 显式指定 {@link Mode}。 */
        public Builder mode(Mode mode) { this.mode = mode; return this; }
        /** {@code --node}：目标节点 id/名称/IP。 */
        public Builder node(String node) { this.node = node; return this; }
        /** {@code --gateway}：强制 gateway 审批。 */
        public Builder gateway(boolean gateway) { this.gateway = gateway; return this; }
        /** set：{@code --file} 上传的 JSON 文件路径。 */
        public Builder file(String file) { this.file = file; return this; }
        /** set：{@code --stdin} 从标准输入读取 JSON。 */
        public Builder stdin(boolean stdin) { this.stdin = stdin; return this; }
        /** allowlist：{@code --agent} agent id（默认 {@code *}）。 */
        public Builder agent(String agent) { this.agent = agent; return this; }

        /**
         * @return 不可变 {@link ExecApprovalsOptions}
         */
        public ExecApprovalsOptions build() {
            return new ExecApprovalsOptions(this);
        }
    }
}
