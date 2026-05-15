package io.github.hiwepy.openclaw.cli.opts;

import io.github.hiwepy.openclaw.cli.args.CliSubArgs;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * {@code openclaw voicecall}：由 voice-call 插件提供的通话控制命令；仅当插件安装并启用时可用。
 * <p>常见流程：{@code call} 发起、{@code continue} 追加话术、{@code end} 结束；{@code expose} 通过 Tailscale Serve/Funnel 暴露 webhook（勿向不可信网络暴露）。</p>
 *
 * @see <a href="https://docs.openclaw.ai/cli/voicecall">voicecall CLI</a>
 */
public final class VoicecallOptions implements CliSubArgs {

    /**
     * {@code voicecall expose --mode}：选择 Tailscale 暴露方式或关闭。
     */
    public enum ExposeMode {
        /** {@code serve}：Tailscale Serve（文档建议优先于 Funnel）。 */
        SERVE("serve"),
        /** {@code funnel}：Tailscale Funnel 公网暴露（仅信任网络）。 */
        FUNNEL("funnel"),
        /** {@code off}：关闭 webhook 暴露。 */
        OFF("off");

        private final String cliValue;

        ExposeMode(String cliValue) {
            this.cliValue = cliValue;
        }

        String cliValue() {
            return cliValue;
        }
    }

    /**
     * voicecall 子命令：查询状态、发起/继续/结束通话，或配置 Tailscale 暴露。
     */
    public enum Mode {
        /** {@code voicecall status}：查看通话状态，常配合 {@code --call-id}。 */
        STATUS,
        /** {@code voicecall call}：拨出到 {@code --to} 并带首句 {@code --message}。 */
        CALL,
        /** {@code voicecall continue}：在既有通话上追加一轮 {@code --message}。 */
        CONTINUE,
        /** {@code voicecall end}：结束指定 {@code --call-id} 的通话。 */
        END,
        /** {@code voicecall expose}：切换 Serve/Funnel/off。 */
        EXPOSE
    }

    /** status / call / continue / end / expose 之一。 */
    private final Mode mode;
    /**
     * {@code --call-id}：插件分配的通话标识（status/continue/end 使用）。
     */
    private final String callId;
    /**
     * call：{@code --to} 被叫号码或插件识别的目标地址（文档示例 E.164）。
     */
    private final String to;
    /**
     * call / continue：{@code --message} 本轮要说的文本内容。
     */
    private final String message;
    /**
     * call：{@code --mode} 插件定义的通话模式（例如文档示例 {@code notify}）。
     */
    private final String callMode;
    /**
     * expose：{@code --mode} 取 {@link ExposeMode} 对应 CLI 字面量。
     */
    private final ExposeMode exposeMode;
    /**
     * 其它未建模参数。
     */
    private final List<String> extra;

    /**
     * @param b 构建器快照
     */
    private VoicecallOptions(Builder b) {
        this.mode = b.mode;
        this.callId = b.callId;
        this.to = b.to;
        this.message = b.message;
        this.callMode = b.callMode;
        this.exposeMode = b.exposeMode;
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
            case STATUS:
                out.add("status");
                OpenClawCliArgv.addIfPresent(out, "--call-id", callId);
                break;
            case CALL:
                out.add("call");
                OpenClawCliArgv.addIfPresent(out, "--to", to);
                OpenClawCliArgv.addIfPresent(out, "--message", message);
                OpenClawCliArgv.addIfPresent(out, "--mode", callMode);
                break;
            case CONTINUE:
                out.add("continue");
                OpenClawCliArgv.addIfPresent(out, "--call-id", callId);
                OpenClawCliArgv.addIfPresent(out, "--message", message);
                break;
            case END:
                out.add("end");
                OpenClawCliArgv.addIfPresent(out, "--call-id", callId);
                break;
            case EXPOSE:
                out.add("expose");
                if (exposeMode != null) {
                    out.add("--mode");
                    out.add(exposeMode.cliValue());
                }
                break;
            default:
                break;
        }
        OpenClawCliArgv.addExtra(out, extra);
        return Collections.unmodifiableList(out);
    }

    /**
     * {@link VoicecallOptions} 构建器。
     */
    public static final class Builder {
        private Mode mode = Mode.STATUS;
        private String callId;
        private String to;
        private String message;
        private String callMode;
        private ExposeMode exposeMode;
        private List<String> extra = new ArrayList<>();

        /**
         * @param callId {@code --call-id}（可为 null）
         * @return {@code this}
         */
        public Builder status(String callId) {
            this.mode = Mode.STATUS;
            this.callId = callId;
            return this;
        }

        /**
         * @param to {@code --to}
         * @param message {@code --message}
         * @param mode {@code --mode}
         * @return {@code this}
         */
        public Builder call(String to, String message, String mode) {
            this.mode = Mode.CALL;
            this.to = to;
            this.message = message;
            this.callMode = mode;
            return this;
        }

        /**
         * @param callId {@code --call-id}
         * @param message {@code --message}
         * @return {@code this}
         */
        public Builder continueCall(String callId, String message) {
            this.mode = Mode.CONTINUE;
            this.callId = callId;
            this.message = message;
            return this;
        }

        /**
         * @param callId {@code --call-id}
         * @return {@code this}
         */
        public Builder end(String callId) {
            this.mode = Mode.END;
            this.callId = callId;
            return this;
        }

        /**
         * @param exposeMode expose：{@code --mode}
         * @return {@code this}
         */
        public Builder expose(ExposeMode exposeMode) {
            this.mode = Mode.EXPOSE;
            this.exposeMode = exposeMode;
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
         * @return 不可变 {@link VoicecallOptions}
         */
        public VoicecallOptions build() {
            return new VoicecallOptions(this);
        }
    }
}
