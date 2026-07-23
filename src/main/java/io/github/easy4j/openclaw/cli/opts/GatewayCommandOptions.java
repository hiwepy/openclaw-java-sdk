package io.github.easy4j.openclaw.cli.opts;

import io.github.easy4j.openclaw.util.OpenClawLists;
import io.github.easy4j.openclaw.cli.args.CliSubArgs;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * {@code openclaw gateway} 通用子命令参数；RPC 查询场景优先使用 {@link #health} / {@link #status} / {@link #probe}，
 * 其它子命令（如 {@code run}、{@code call}）使用 {@link #add(String...)} 按文档顺序追加。
 * <p>Gateway 为 OpenClaw 的 WebSocket 服务端（渠道、节点、会话、hooks）；子命令含前台 {@code run}、服务生命周期、
 * {@code discover}、{@code call} RPC 等，详见 gateway CLI 文档。</p>
 *
 * @see <a href="https://docs.openclaw.ai/cli/gateway">gateway CLI</a>
 */
public final class GatewayCommandOptions implements CliSubArgs {

    /**
     * 紧跟在 {@code gateway} 之后的子命令与参数 token 不可变列表（不含可执行文件名与全局前缀），顺序须与官方 CLI 一致。
     */
    private final List<String> segments;

    /**
     * @param segments 非 null，将拷贝为不可变列表
     */
    private GatewayCommandOptions(List<String> segments) {
        this.segments = OpenClawLists.copyOf(segments);
    }

    /**
     * @return 新 {@link Builder}
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * 无子命令 token：对应裸 {@code openclaw gateway}（由 CLI 决定是否等价于前台启动行为，见文档 Run the Gateway 节）。
     */
    public static GatewayCommandOptions empty() {
        return new GatewayCommandOptions(OpenClawLists.empty());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> toSubcommandArguments() {
        return segments;
    }

    /**
     * {@link GatewayCommandOptions} 构建器：累积子命令片段。
     */
    public static final class Builder {

        /** 内部可变的 token 缓冲。 */
        private final List<String> s = new ArrayList<>();

        /**
         * 按顺序追加任意 CLI token（如 {@code run}、{@code call} 及参数）。
         *
         * @param tokens 可为 null（忽略）
         * @return {@code this}
         */
        public Builder add(String... tokens) {
            if (tokens != null) {
                Collections.addAll(s, tokens);
            }
            return this;
        }

        /**
         * 追加 {@code gateway health ...} 片段。
         *
         * @param rpc 非 null
         * @return {@code this}
         */
        public Builder health(GatewayRpcOptions rpc) {
            Objects.requireNonNull(rpc, "rpc");
            s.addAll(GatewayCliArgv.health(rpc));
            return this;
        }

        /**
         * 追加 {@code gateway status ...} 片段。
         *
         * @param rpc   非 null
         * @param extra 可为 null
         * @return {@code this}
         */
        public Builder status(GatewayRpcOptions rpc, GatewayCliArgv.GatewayStatusOptions extra) {
            Objects.requireNonNull(rpc, "rpc");
            s.addAll(GatewayCliArgv.status(rpc, extra));
            return this;
        }

        /**
         * 追加 {@code gateway probe ...} 片段。
         *
         * @param rpc   非 null
         * @param extra 可为 null
         * @return {@code this}
         */
        public Builder probe(GatewayRpcOptions rpc, GatewayCliArgv.GatewayProbeOptions extra) {
            Objects.requireNonNull(rpc, "rpc");
            s.addAll(GatewayCliArgv.probe(rpc, extra));
            return this;
        }

        /**
         * @return 不可变 {@link GatewayCommandOptions}
         */
        public GatewayCommandOptions build() {
            return new GatewayCommandOptions(OpenClawLists.copyOf(s));
        }
    }
}
