package io.github.easy4j.openclaw.cli.opts;

import io.github.easy4j.openclaw.util.OpenClawLists;
import io.github.easy4j.openclaw.cli.args.CliSubArgs;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * {@code openclaw system}：Gateway 级系统能力——入队系统事件、控制心跳、查看 presence；子命令均走 Gateway RPC。
 * <p>共享客户端参数：{@code --url}、{@code --token}、{@code --timeout}、{@code --expect-final}。系统事件为临时数据，Gateway 重启后不保留。</p>
 *
 * @see <a href="https://docs.openclaw.ai/cli/system">system CLI</a>
 */
public final class SystemOptions implements CliSubArgs {

    /** {@code system heartbeat} 子路径。 */
    public enum HeartbeatSub {
        /** {@code heartbeat last} */
        LAST,
        /** {@code heartbeat enable} */
        ENABLE,
        /** {@code heartbeat disable} */
        DISABLE
    }

    /** 顶层 {@code system} 子命令族。 */
    public enum Mode {
        /** {@code system event} */
        EVENT,
        /** {@code system heartbeat} */
        HEARTBEAT,
        /** {@code system presence} */
        PRESENCE
    }

    /**
     * 子命令族：{@code event}（入队系统事件）、{@code heartbeat}、{@code presence}。
     */
    private final Mode mode;
    /**
     * {@code heartbeat} 子路径：{@code last}（最近事件）、{@code enable}、{@code disable}（文档：暂停/恢复心跳调度）。
     */
    private final HeartbeatSub heartbeatSub;
    /**
     * {@code --url}：Gateway WebSocket（与其它 RPC 子命令共享）。
     */
    private final String gatewayUrl;
    /**
     * {@code --token}：RPC 认证 token。
     */
    private final String gatewayToken;
    /**
     * {@code --timeout}：RPC 超时。
     */
    private final String timeout;
    /**
     * {@code --expect-final}：等待 RPC 最终响应。
     */
    private final boolean expectFinal;
    /**
     * {@code --text}：系统事件正文（{@code system event} 必需）；随下次心跳以 {@code System:} 行注入主会话提示。
     */
    private final String eventText;
    /**
     * {@code --mode}：{@code now} 立即触发心跳，或 {@code next-heartbeat}（默认）等待下一次调度。
     */
    private final String eventMode;
    /**
     * {@code --json}：机器可读输出。
     */
    private final boolean json;
    /**
     * 其它 argv。
     */
    private final List<String> extra;

    /**
     * @param b 构建器快照
     */
    private SystemOptions(Builder b) {
        this.mode = b.mode;
        this.heartbeatSub = b.heartbeatSub;
        this.gatewayUrl = b.gatewayUrl;
        this.gatewayToken = b.gatewayToken;
        this.timeout = b.timeout;
        this.expectFinal = b.expectFinal;
        this.eventText = b.eventText;
        this.eventMode = b.eventMode;
        this.json = b.json;
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
        switch (mode) {
            case EVENT:
                out.add("event");
                OpenClawCliArgv.addIfPresent(out, "--text", eventText);
                OpenClawCliArgv.addIfPresent(out, "--mode", eventMode);
                break;
            case HEARTBEAT:
                out.add("heartbeat");
                if (heartbeatSub == HeartbeatSub.LAST) {
                    out.add("last");
                } else if (heartbeatSub == HeartbeatSub.ENABLE) {
                    out.add("enable");
                } else if (heartbeatSub == HeartbeatSub.DISABLE) {
                    out.add("disable");
                }
                break;
            case PRESENCE:
                out.add("presence");
                break;
            default:
                break;
        }
        OpenClawCliArgv.addIfPresent(out, "--url", gatewayUrl);
        OpenClawCliArgv.addIfPresent(out, "--token", gatewayToken);
        OpenClawCliArgv.addIfPresent(out, "--timeout", timeout);
        OpenClawCliArgv.addFlag(out, "--expect-final", expectFinal);
        OpenClawCliArgv.addFlag(out, "--json", json);
        OpenClawCliArgv.addExtra(out, extra);
        return Collections.unmodifiableList(out);
    }

    /**
     * {@link SystemOptions} 构建器。
     */
    public static final class Builder {
        private Mode mode = Mode.PRESENCE;
        private HeartbeatSub heartbeatSub = HeartbeatSub.LAST;
        private String gatewayUrl;
        private String gatewayToken;
        private String timeout;
        private boolean expectFinal;
        private String eventText;
        private String eventMode;
        private boolean json;
        private List<String> extra = new ArrayList<>();

        /**
         * 设为 {@link Mode#EVENT} 并设置事件文本。
         *
         * @param text {@code --text}
         * @return {@code this}
         */
        public Builder event(String text) {
            this.mode = Mode.EVENT;
            this.eventText = text;
            return this;
        }

        /**
         * @param eventMode {@code --mode}（event 子命令）
         * @return {@code this}
         */
        public Builder eventMode(String eventMode) {
            this.eventMode = eventMode;
            return this;
        }

        /**
         * @return {@code this}，heartbeat last
         */
        public Builder heartbeatLast() {
            this.mode = Mode.HEARTBEAT;
            this.heartbeatSub = HeartbeatSub.LAST;
            return this;
        }

        /**
         * @return {@code this}，heartbeat enable
         */
        public Builder heartbeatEnable() {
            this.mode = Mode.HEARTBEAT;
            this.heartbeatSub = HeartbeatSub.ENABLE;
            return this;
        }

        /**
         * @return {@code this}，heartbeat disable
         */
        public Builder heartbeatDisable() {
            this.mode = Mode.HEARTBEAT;
            this.heartbeatSub = HeartbeatSub.DISABLE;
            return this;
        }

        /**
         * @return {@code this}，presence 子命令
         */
        public Builder presence() {
            this.mode = Mode.PRESENCE;
            return this;
        }

        /**
         * @param url {@code --url}
         * @return {@code this}
         */
        public Builder gatewayUrl(String url) {
            this.gatewayUrl = url;
            return this;
        }

        /**
         * @param token {@code --token}
         * @return {@code this}
         */
        public Builder gatewayToken(String token) {
            this.gatewayToken = token;
            return this;
        }

        /**
         * @param timeout {@code --timeout}
         * @return {@code this}
         */
        public Builder timeout(String timeout) {
            this.timeout = timeout;
            return this;
        }

        /**
         * @param expectFinal {@code --expect-final}
         * @return {@code this}
         */
        public Builder expectFinal(boolean expectFinal) {
            this.expectFinal = expectFinal;
            return this;
        }

        /**
         * @param json {@code --json}
         * @return {@code this}
         */
        public Builder json(boolean json) {
            this.json = json;
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
         * @return 不可变 {@link SystemOptions}
         */
        public SystemOptions build() {
            return new SystemOptions(this);
        }
    }
}
