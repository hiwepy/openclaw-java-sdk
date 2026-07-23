package io.github.easy4j.openclaw.cli.opts;

import io.github.easy4j.openclaw.util.OpenClawLists;
import io.github.easy4j.openclaw.util.OpenClawStrings;
import io.github.easy4j.openclaw.cli.args.CliSubArgs;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Task Flow 子命令参数：对应 {@code openclaw tasks flow list|show|cancel}（多步流程编排，状态持久化，取消会写入 sticky cancel 并停止子任务）。
 * <p>请配合 {@link io.github.easy4j.openclaw.cli.OpenClawCli#flows(FlowsOptions)} 使用；其实现向 CLI 投递 {@code tasks} 前缀。</p>
 *
 * @see <a href="https://docs.openclaw.ai/cli/flows">flows CLI（重定向说明）</a>
 * @see <a href="https://docs.openclaw.ai/automation/taskflow">Task Flow</a>
 */
public final class FlowsOptions implements CliSubArgs {

    /** {@code flow list|show|cancel} 变体。 */
    public enum Mode {
        /** {@code flow list} */
        LIST,
        /** {@code flow show &lt;id&gt;} */
        SHOW,
        /** {@code flow cancel &lt;id&gt;} */
        CANCEL
    }

    /**
     * {@code flow list|show|cancel} 之一（与 Task Flow 文档 CLI 表一致）。
     */
    private final Mode mode;
    /**
     * {@code flow list --json}：列出流程时输出机器可读 JSON。
     */
    private final boolean listJson;
    /**
     * {@code flow show|cancel} 的位置参数：flow id 或 lookup key（官方文档以 {@code lookup} 占位符表示）。
     */
    private final String lookup;
    /**
     * 其它 {@code openclaw tasks} 级 token。
     */
    private final List<String> extra;

    /**
     * @param b 构建器快照
     */
    private FlowsOptions(Builder b) {
        this.mode = b.mode;
        this.listJson = b.listJson;
        this.lookup = b.lookup;
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
        out.add("flow");
        switch (mode) {
            case LIST:
                out.add("list");
                OpenClawCliArgv.addFlag(out, "--json", listJson);
                break;
            case SHOW:
                out.add("show");
                if (lookup != null && OpenClawStrings.isNotBlank(lookup)) {
                    out.add(lookup.trim());
                }
                break;
            case CANCEL:
                out.add("cancel");
                if (lookup != null && OpenClawStrings.isNotBlank(lookup)) {
                    out.add(lookup.trim());
                }
                break;
            default:
                break;
        }
        OpenClawCliArgv.addExtra(out, extra);
        return Collections.unmodifiableList(out);
    }

    /**
     * {@link FlowsOptions} 构建器。
     */
    public static final class Builder {
        private Mode mode = Mode.LIST;
        private boolean listJson;
        private String lookup;
        private List<String> extra = new ArrayList<>();

        /**
         * @return {@code this}，{@link Mode#LIST}
         */
        public Builder list() {
            this.mode = Mode.LIST;
            return this;
        }

        /**
         * @param json {@code flow list --json}
         * @return {@code this}
         */
        public Builder listJson(boolean json) {
            this.listJson = json;
            return this;
        }

        /**
         * @param lookup flow 标识（show）
         * @return {@code this}
         */
        public Builder show(String lookup) {
            this.mode = Mode.SHOW;
            this.lookup = lookup;
            return this;
        }

        /**
         * @param lookup flow 标识（cancel）
         * @return {@code this}
         */
        public Builder cancel(String lookup) {
            this.mode = Mode.CANCEL;
            this.lookup = lookup;
            return this;
        }

        /**
         * @param tokens 额外 CLI token
         * @return {@code this}
         */
        public Builder extra(String... tokens) {
            if (tokens != null) {
                Collections.addAll(extra, tokens);
            }
            return this;
        }

        /**
         * @return 不可变 {@link FlowsOptions}
         */
        public FlowsOptions build() {
            return new FlowsOptions(this);
        }
    }
}
