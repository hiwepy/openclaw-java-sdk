package io.github.easy4j.openclaw.cli.opts;

import io.github.easy4j.openclaw.util.OpenClawLists;
import io.github.easy4j.openclaw.cli.args.CliSubArgs;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * {@code openclaw commitments}：列出与管理推断的后续承诺（follow-up commitments）。
 * <p>
 * 默认动作等价于 {@code list}；支持 {@code dismiss <ids...>} 子命令。父命令通过
 * {@code enablePositionalOptions()} 让子命令继承 {@code --json}/{@code --agent}/
 * {@code --status}/{@code --all} 选项。
 * </p>
 *
 * @see <a href="https://docs.openclaw.ai/cli/commitments">commitments CLI</a>
 */
public final class CommitmentsOptions implements CliSubArgs {

    /** 默认列出，或进入 {@code dismiss} 子命令。 */
    public enum Mode {
        /** 默认列出：无子命令 token，与文档示例 {@code openclaw commitments} 一致。 */
        LIST,
        /** {@code dismiss <ids...>}：按 id 标记为已忽略。 */
        DISMISS
    }

    /** LIST 或 DISMISS。 */
    private final Mode mode;
    /** {@code --json}：JSON 输出。 */
    private final boolean json;
    /** {@code --agent}：限定到指定 agent id。 */
    private final String agent;
    /** {@code --status}：按状态过滤（pending/sent/dismissed/snoozed/expired）。 */
    private final String status;
    /** {@code --all}：显示所有状态。 */
    private final boolean all;
    /** dismiss：位置参数 {@code <ids...>}，承诺 id 列表。 */
    private final List<String> dismissIds;

    private CommitmentsOptions(Builder b) {
        this.mode = b.mode;
        this.json = b.json;
        this.agent = b.agent;
        this.status = b.status;
        this.all = b.all;
        this.dismissIds = OpenClawLists.copyOf(b.dismissIds);
    }

    /**
     * @return 新 {@link Builder}（默认 {@link Mode#LIST}）
     */
    public static Builder builder() {
        return new Builder();
    }

    @Override
    public List<String> toSubcommandArguments() {
        List<String> out = new ArrayList<>();
        if (mode == Mode.DISMISS) {
            out.add("dismiss");
            if (dismissIds != null) {
                out.addAll(dismissIds);
            }
        }
        if (json) {
            out.add("--json");
        }
        if (agent != null && !agent.isEmpty()) {
            out.add("--agent");
            out.add(agent);
        }
        if (status != null && !status.isEmpty()) {
            out.add("--status");
            out.add(status);
        }
        if (all) {
            out.add("--all");
        }
        return Collections.unmodifiableList(out);
    }

    /**
     * {@link CommitmentsOptions} 构建器。
     */
    public static final class Builder {
        private Mode mode = Mode.LIST;
        private boolean json;
        private String agent;
        private String status;
        private boolean all;
        private List<String> dismissIds;

        /** 切换为 {@code dismiss} 子命令模式。 */
        public Builder dismiss() { this.mode = Mode.DISMISS; return this; }
        /** 显式指定 {@link Mode}。 */
        public Builder mode(Mode mode) { this.mode = mode; return this; }
        /** {@code --json}：JSON 输出。 */
        public Builder json(boolean json) { this.json = json; return this; }
        /** {@code --agent}：限定到指定 agent id。 */
        public Builder agent(String agent) { this.agent = agent; return this; }
        /** {@code --status}：按状态过滤（pending/sent/dismissed/snoozed/expired）。 */
        public Builder status(String status) { this.status = status; return this; }
        /** {@code --all}：显示所有状态。 */
        public Builder all(boolean all) { this.all = all; return this; }
        /** dismiss：位置参数 {@code <ids...>}，承诺 id 列表。 */
        public Builder dismissIds(List<String> ids) { this.dismissIds = ids; return this; }

        /**
         * @return 不可变 {@link CommitmentsOptions}
         */
        public CommitmentsOptions build() {
            return new CommitmentsOptions(this);
        }
    }
}
