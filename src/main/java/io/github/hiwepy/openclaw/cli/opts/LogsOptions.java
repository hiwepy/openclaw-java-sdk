package io.github.hiwepy.openclaw.cli.opts;

import io.github.hiwepy.openclaw.cli.args.CliSubArgs;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * {@code openclaw logs}：通过 RPC 跟踪 Gateway 文件日志（远程模式可用）。
 * <p>日志专有选项见文档「Options」；另接受标准 Gateway 客户端 flag（{@link GatewayRpcOptions}），
 * 其中 {@code --timeout} 默认 30000ms，{@code --expect-final} 用于 agent 支撑的调用。</p>
 *
 * @see <a href="https://docs.openclaw.ai/cli/logs">logs CLI</a>
 */
public final class LogsOptions implements CliSubArgs {

    /**
     * 共享 RPC 选项：{@code --url}、{@code --token}、{@code --password}、{@code --timeout}、{@code --expect-final}、{@code --json} 等（与 gateway 文档「Shared options」一致）。
     */
    private final GatewayRpcOptions rpc;
    /**
     * {@code --limit}：返回的最大日志行数（文档默认 {@code 200}）。
     */
    private final String limit;
    /**
     * {@code --max-bytes}：从日志文件读取的最大字节数（文档默认 {@code 250000}）。
     */
    private final String maxBytes;
    /**
     * {@code --follow}：持续跟随日志流（轮询由 {@code --interval} 控制）。
     */
    private final boolean follow;
    /**
     * {@code --interval}：follow 模式下的轮询间隔毫秒（文档默认 {@code 1000}）。
     */
    private final String intervalMs;
    /**
     * {@code --json}：每行一条 JSON 事件输出。
     */
    private final boolean json;
    /**
     * {@code --plain}：纯文本，不带样式化排版。
     */
    private final boolean plain;
    /**
     * {@code --no-color}：禁用 ANSI 颜色。
     */
    private final boolean noColor;
    /**
     * {@code --local-time}：时间戳按本机时区渲染。
     */
    private final boolean localTime;

    /**
     * @param b 构建器；{@code rpc} 缺省时使用空 {@link GatewayRpcOptions}
     */
    private LogsOptions(Builder b) {
        this.rpc = b.rpc != null ? b.rpc : GatewayRpcOptions.builder().build();
        this.limit = b.limit;
        this.maxBytes = b.maxBytes;
        this.follow = b.follow;
        this.intervalMs = b.intervalMs;
        this.json = b.json;
        this.plain = b.plain;
        this.noColor = b.noColor;
        this.localTime = b.localTime;
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
        rpc.appendSharedFlags(out);
        if (limit != null && !limit.isEmpty()) {
            out.add("--limit");
            out.add(limit);
        }
        if (maxBytes != null && !maxBytes.isEmpty()) {
            out.add("--max-bytes");
            out.add(maxBytes);
        }
        if (follow) {
            out.add("--follow");
        }
        if (intervalMs != null && !intervalMs.isEmpty()) {
            out.add("--interval");
            out.add(intervalMs);
        }
        if (json) {
            out.add("--json");
        }
        if (plain) {
            out.add("--plain");
        }
        if (noColor) {
            out.add("--no-color");
        }
        if (localTime) {
            out.add("--local-time");
        }
        return Collections.unmodifiableList(out);
    }

    /**
     * {@link LogsOptions} 构建器。
     */
    public static final class Builder {

        private GatewayRpcOptions rpc;
        private String limit;
        private String maxBytes;
        private boolean follow;
        private String intervalMs;
        private boolean json;
        private boolean plain;
        private boolean noColor;
        private boolean localTime;

        /** 共享 {@code --url} / {@code --token} / {@code --timeout} / {@code --expect-final} 等。 */
        public Builder rpc(GatewayRpcOptions rpc) {
            this.rpc = Objects.requireNonNull(rpc, "rpc");
            return this;
        }

        /**
         * @param limit {@code --limit}
         * @return {@code this}
         */
        public Builder limit(String limit) {
            this.limit = limit;
            return this;
        }

        /**
         * @param maxBytes {@code --max-bytes}
         * @return {@code this}
         */
        public Builder maxBytes(String maxBytes) {
            this.maxBytes = maxBytes;
            return this;
        }

        /**
         * @param follow {@code --follow}
         * @return {@code this}
         */
        public Builder follow(boolean follow) {
            this.follow = follow;
            return this;
        }

        /**
         * @param intervalMs {@code --interval}（毫秒字符串）
         * @return {@code this}
         */
        public Builder intervalMs(String intervalMs) {
            this.intervalMs = intervalMs;
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
         * @param plain {@code --plain}
         * @return {@code this}
         */
        public Builder plain(boolean plain) {
            this.plain = plain;
            return this;
        }

        /**
         * @param noColor {@code --no-color}
         * @return {@code this}
         */
        public Builder noColor(boolean noColor) {
            this.noColor = noColor;
            return this;
        }

        /**
         * @param localTime {@code --local-time}
         * @return {@code this}
         */
        public Builder localTime(boolean localTime) {
            this.localTime = localTime;
            return this;
        }

        /**
         * @return 不可变 {@link LogsOptions}
         */
        public LogsOptions build() {
            return new LogsOptions(this);
        }
    }
}
