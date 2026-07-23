package io.github.easy4j.openclaw.cli.opts;

import io.github.easy4j.openclaw.cli.args.CliSubArgs;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * {@code openclaw exec-policy}：显示或同步请求的 exec policy 与主机审批。
 * <p>
 * 支持 {@code show}、{@code preset <name>}、{@code set} 子命令。{@code set} 子命令要求至少指定
 * {@code --host}/{@code --security}/{@code --ask}/{@code --ask-fallback} 之一。
 * </p>
 *
 * @see <a href="https://docs.openclaw.ai/cli/exec-policy">exec-policy CLI</a>
 */
public final class ExecPolicyOptions implements CliSubArgs {

    /** 子命令模式。 */
    public enum Mode {
        /** {@code show}：显示当前 exec policy。 */
        SHOW,
        /** {@code preset <name>}：应用预设（yolo/cautious/deny-all）。 */
        PRESET,
        /** {@code set}：显式设置 exec policy 字段。 */
        SET
    }

    /** SHOW / PRESET / SET。 */
    private final Mode mode;
    /** preset：预设名（yolo/cautious/deny-all）。 */
    private final String presetName;
    /** set：{@code --host} exec host 目标（auto/sandbox/gateway/node）。 */
    private final String host;
    /** set：{@code --security} exec security 模式（deny/allowlist/full）。 */
    private final String security;
    /** set：{@code --ask} exec ask 模式（off/on-miss/always）。 */
    private final String ask;
    /** set：{@code --ask-fallback} 主机审批兜底（deny/allowlist/full）。 */
    private final String askFallback;
    /** {@code --json}：JSON 输出。 */
    private final boolean json;

    private ExecPolicyOptions(Builder b) {
        this.mode = b.mode;
        this.presetName = b.presetName;
        this.host = b.host;
        this.security = b.security;
        this.ask = b.ask;
        this.askFallback = b.askFallback;
        this.json = b.json;
    }

    /**
     * @return 新 {@link Builder}（默认 {@link Mode#SHOW}）
     */
    public static Builder builder() {
        return new Builder();
    }

    @Override
    public List<String> toSubcommandArguments() {
        List<String> out = new ArrayList<>();
        switch (mode) {
            case SHOW:
                out.add("show");
                break;
            case PRESET:
                out.add("preset");
                if (presetName != null && !presetName.isEmpty()) {
                    out.add(presetName);
                }
                break;
            case SET:
                out.add("set");
                break;
            default:
                // 未指定模式时不输出子命令 token（等价于父命令默认动作）
                break;
        }
        OpenClawCliArgv.addIfPresent(out, "--host", host);
        OpenClawCliArgv.addIfPresent(out, "--security", security);
        OpenClawCliArgv.addIfPresent(out, "--ask", ask);
        OpenClawCliArgv.addIfPresent(out, "--ask-fallback", askFallback);
        OpenClawCliArgv.addFlag(out, "--json", json);
        return Collections.unmodifiableList(out);
    }

    /**
     * {@link ExecPolicyOptions} 构建器。
     */
    public static final class Builder {
        private Mode mode = Mode.SHOW;
        private String presetName;
        private String host;
        private String security;
        private String ask;
        private String askFallback;
        private boolean json;

        /** 切换为 {@code show} 子命令。 */
        public Builder show() { this.mode = Mode.SHOW; return this; }
        /** 切换为 {@code preset <name>} 子命令。 */
        public Builder preset(String name) { this.mode = Mode.PRESET; this.presetName = name; return this; }
        /** 切换为 {@code set} 子命令。 */
        public Builder set() { this.mode = Mode.SET; return this; }
        /** 显式指定 {@link Mode}。 */
        public Builder mode(Mode mode) { this.mode = mode; return this; }
        /** set：{@code --host} exec host 目标（auto/sandbox/gateway/node）。 */
        public Builder host(String host) { this.host = host; return this; }
        /** set：{@code --security} exec security 模式（deny/allowlist/full）。 */
        public Builder security(String security) { this.security = security; return this; }
        /** set：{@code --ask} exec ask 模式（off/on-miss/always）。 */
        public Builder ask(String ask) { this.ask = ask; return this; }
        /** set：{@code --ask-fallback} 主机审批兜底（deny/allowlist/full）。 */
        public Builder askFallback(String askFallback) { this.askFallback = askFallback; return this; }
        /** {@code --json}：JSON 输出。 */
        public Builder json(boolean json) { this.json = json; return this; }

        /**
         * @return 不可变 {@link ExecPolicyOptions}
         */
        public ExecPolicyOptions build() {
            return new ExecPolicyOptions(this);
        }
    }
}
