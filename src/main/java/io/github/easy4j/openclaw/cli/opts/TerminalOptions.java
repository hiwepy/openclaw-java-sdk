package io.github.easy4j.openclaw.cli.opts;

import io.github.easy4j.openclaw.cli.args.CliSubArgs;

import java.util.Collections;
import java.util.List;

/**
 * {@code openclaw terminal}：打开本地终端 UI（{@code tui --local} 的别名）。
 * <p>
 * 在 openclaw 源码（{@code src/cli/tui-cli.ts}）中，{@code terminal} 通过 Commander
 * {@code .alias("terminal")} 注册为 {@code tui} 的别名；调用 {@code openclaw terminal} 会自动启用
 * {@code --local} 语义。选项集合与 {@link TuiOptions} / {@link ChatOptions} 完全一致。
 * </p>
 * <p>
 * 本类复用 {@link ChatOptions} 的字段定义与构建逻辑（语义等价），仅类名/命令名不同。
 * </p>
 *
 * @see ChatOptions
 * @see TuiOptions
 * @see <a href="https://docs.openclaw.ai/cli/terminal">terminal CLI</a>
 */
public final class TerminalOptions implements CliSubArgs {

    /** 委托给同构 {@link ChatOptions}，避免重复实现 argv 拼接逻辑。 */
    private final ChatOptions delegate;

    private TerminalOptions(ChatOptions delegate) {
        this.delegate = delegate;
    }

    /**
     * @return 新 {@link Builder}
     */
    public static Builder builder() {
        return new Builder();
    }

    @Override
    public List<String> toSubcommandArguments() {
        return delegate.toSubcommandArguments();
    }

    /**
     * {@link TerminalOptions} 构建器：与 {@link ChatOptions.Builder} 同构。
     */
    public static final class Builder {
        private final ChatOptions.Builder b = ChatOptions.builder();

        /** {@code --local}：本地嵌入式 agent 运行时（terminal 别名默认为 true）。 */
        public Builder local(boolean local) { b.local(local); return this; }
        /** {@code --url}：Gateway WebSocket URL。 */
        public Builder url(String url) { b.url(url); return this; }
        /** {@code --token}：Gateway 令牌。 */
        public Builder token(String token) { b.token(token); return this; }
        /** {@code --password}：Gateway 密码。 */
        public Builder password(String password) { b.password(password); return this; }
        /** {@code --session}：会话键。 */
        public Builder session(String session) { b.session(session); return this; }
        /** {@code --deliver}：投递助手回复。 */
        public Builder deliver(boolean deliver) { b.deliver(deliver); return this; }
        /** {@code --thinking}：思考级别覆盖。 */
        public Builder thinking(String thinking) { b.thinking(thinking); return this; }
        /** {@code --message}：连接后发送初始消息。 */
        public Builder message(String message) { b.message(message); return this; }
        /** {@code --timeout-ms}：Agent 超时毫秒数。 */
        public Builder timeoutMs(Integer timeoutMs) { b.timeoutMs(timeoutMs); return this; }
        /** {@code --history-limit}：加载的历史条目数。 */
        public Builder historyLimit(Integer historyLimit) { b.historyLimit(historyLimit); return this; }

        /**
         * @return 不可变 {@link TerminalOptions}
         */
        public TerminalOptions build() {
            return new TerminalOptions(b.build());
        }
    }
}
