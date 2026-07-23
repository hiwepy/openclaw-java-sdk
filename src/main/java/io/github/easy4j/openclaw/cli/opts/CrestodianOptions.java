package io.github.easy4j.openclaw.cli.opts;

import io.github.easy4j.openclaw.cli.args.CliSubArgs;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * {@code openclaw crestodian}：打开 ring-zero 设置与修复助手。
 * <p>
 * 在 openclaw 源码（{@code src/cli/program/register.crestodian.ts}）中注册为独立助手命令，
 * 目录策略为 {@code bypassConfigGuard, loadPlugins "never", ensureCliPath false}。
 * </p>
 *
 * @see <a href="https://docs.openclaw.ai/cli/crestodian">crestodian CLI</a>
 */
public final class CrestodianOptions implements CliSubArgs {

    /** {@code -m, --message}：运行一次 Crestodian 请求。 */
    private final String message;
    /** {@code --yes}：批准本次请求的持久化配置写入。 */
    private final boolean yes;
    /** {@code --json}：以 JSON 输出启动概览。 */
    private final boolean json;

    private CrestodianOptions(Builder b) {
        this.message = b.message;
        this.yes = b.yes;
        this.json = b.json;
    }

    /**
     * @return 新 {@link Builder}
     */
    public static Builder builder() {
        return new Builder();
    }

    @Override
    public List<String> toSubcommandArguments() {
        List<String> out = new ArrayList<>();
        if (message != null && !message.isEmpty()) {
            out.add("--message");
            out.add(message);
        }
        if (yes) {
            out.add("--yes");
        }
        if (json) {
            out.add("--json");
        }
        return Collections.unmodifiableList(out);
    }

    /**
     * {@link CrestodianOptions} 构建器。
     */
    public static final class Builder {
        private String message;
        private boolean yes;
        private boolean json;

        /** {@code -m, --message}：运行一次 Crestodian 请求。 */
        public Builder message(String message) { this.message = message; return this; }
        /** {@code --yes}：批准持久化配置写入。 */
        public Builder yes(boolean yes) { this.yes = yes; return this; }
        /** {@code --json}：以 JSON 输出启动概览。 */
        public Builder json(boolean json) { this.json = json; return this; }

        /**
         * @return 不可变 {@link CrestodianOptions}
         */
        public CrestodianOptions build() {
            return new CrestodianOptions(this);
        }
    }
}
