package io.github.easy4j.openclaw.cli.opts;

import io.github.easy4j.openclaw.cli.args.CliSubArgs;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 顶层 {@code openclaw health}：向<strong>正在运行</strong>的 Gateway 拉取健康快照（非 {@code gateway health} RPC 子命令）。
 * <p>文档说明：默认可能返回缓存快照并在后台刷新；{@code --verbose} 会强制实时探测并展开人机输出。
 * 多 agent 配置时输出包含各 agent 的 session 存储信息。</p>
 *
 * @see <a href="https://docs.openclaw.ai/cli/health">health CLI</a>
 */
public final class HealthCommandOptions implements CliSubArgs {

    /**
     * {@code --json}：机器可读 JSON 输出。
     */
    private final boolean json;
    /**
     * {@code --timeout}：连接超时毫秒数（文档默认 {@code 10000}），此处为字符串形式以与 CLI 传参一致。
     */
    private final String timeoutMs;
    /**
     * {@code --verbose}：详细日志；强制实时探测、打印网关连接细节，并在人机模式下展开已配置账号与 agent。
     */
    private final boolean verbose;
    /**
     * {@code --debug}：文档声明为 {@code --verbose} 的别名。
     */
    private final boolean debug;

    /**
     * @param b 构建器快照
     */
    private HealthCommandOptions(Builder b) {
        this.json = b.json;
        this.timeoutMs = b.timeoutMs;
        this.verbose = b.verbose;
        this.debug = b.debug;
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
        if (json) {
            out.add("--json");
        }
        if (timeoutMs != null && !timeoutMs.isEmpty()) {
            out.add("--timeout");
            out.add(timeoutMs);
        }
        if (verbose) {
            out.add("--verbose");
        }
        if (debug) {
            out.add("--debug");
        }
        return Collections.unmodifiableList(out);
    }

    /**
     * {@link HealthCommandOptions} 构建器。
     */
    public static final class Builder {

        private boolean json;
        private String timeoutMs;
        private boolean verbose;
        private boolean debug;

        /**
         * @param json {@code --json}
         * @return {@code this}
         */
        public Builder json(boolean json) {
            this.json = json;
            return this;
        }

        /** 连接超时毫秒数（文档默认 10000）。 */
        public Builder timeoutMs(String timeoutMs) {
            this.timeoutMs = timeoutMs;
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

        /** 文档中与 {@code --verbose} 等价别名。 */
        public Builder debug(boolean debug) {
            this.debug = debug;
            return this;
        }

        /**
         * @return 不可变 {@link HealthCommandOptions}
         */
        public HealthCommandOptions build() {
            return new HealthCommandOptions(this);
        }
    }
}
