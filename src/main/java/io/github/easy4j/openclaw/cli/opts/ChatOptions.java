package io.github.easy4j.openclaw.cli.opts;

import io.github.easy4j.openclaw.util.OpenClawLists;
import io.github.easy4j.openclaw.cli.args.CliSubArgs;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * {@code openclaw chat}：打开本地终端 UI（{@code tui --local} 的别名）。
 * <p>
 * 在 openclaw 源码（{@code src/cli/tui-cli.ts}）中，{@code chat} 通过 Commander {@code .alias("chat")}
 * 注册为 {@code tui} 的别名；调用 {@code openclaw chat} 会自动启用 {@code --local} 语义。
 * 选项集合与 {@link TuiOptions} 完全一致。
 * </p>
 * <p>
 * 约束：{@code --local} 不能与 {@code --url}、{@code --token}、{@code --password} 同时使用。
 * </p>
 *
 * @see TuiOptions
 * @see <a href="https://docs.openclaw.ai/cli/chat">chat CLI</a>
 */
public final class ChatOptions implements CliSubArgs {

    /** {@code --local}：运行于本地嵌入式 agent 运行时（chat 别名调用时强制为 true）。 */
    private final boolean local;
    /** {@code --url}：Gateway WebSocket URL。 */
    private final String url;
    /** {@code --token}：Gateway 令牌（如需）。 */
    private final String token;
    /** {@code --password}：Gateway 密码（如需）。 */
    private final String password;
    /** {@code --session}：会话键（默认 {@code main}，{@code scope=global} 时为 {@code global}）。 */
    private final String session;
    /** {@code --deliver}：投递助手回复。 */
    private final boolean deliver;
    /** {@code --thinking}：思考级别覆盖。 */
    private final String thinking;
    /** {@code --message}：连接后发送初始消息。 */
    private final String message;
    /** {@code --timeout-ms}：Agent 超时毫秒数。 */
    private final Integer timeoutMs;
    /** {@code --history-limit}：加载的历史条目数（默认 {@code 200}）。 */
    private final Integer historyLimit;

    private ChatOptions(Builder b) {
        this.local = b.local;
        this.url = b.url;
        this.token = b.token;
        this.password = b.password;
        this.session = b.session;
        this.deliver = b.deliver;
        this.thinking = b.thinking;
        this.message = b.message;
        this.timeoutMs = b.timeoutMs;
        this.historyLimit = b.historyLimit;
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
        OpenClawCliArgv.addFlag(out, "--local", local);
        OpenClawCliArgv.addIfPresent(out, "--url", url);
        OpenClawCliArgv.addIfPresent(out, "--token", token);
        OpenClawCliArgv.addIfPresent(out, "--password", password);
        OpenClawCliArgv.addIfPresent(out, "--session", session);
        OpenClawCliArgv.addFlag(out, "--deliver", deliver);
        OpenClawCliArgv.addIfPresent(out, "--thinking", thinking);
        OpenClawCliArgv.addIfPresent(out, "--message", message);
        OpenClawCliArgv.addIfNotNull(out, "--timeout-ms", timeoutMs);
        OpenClawCliArgv.addIfNotNull(out, "--history-limit", historyLimit);
        return Collections.unmodifiableList(out);
    }

    /**
     * {@link ChatOptions} 构建器。
     */
    public static final class Builder {
        private boolean local;
        private String url;
        private String token;
        private String password;
        private String session;
        private boolean deliver;
        private String thinking;
        private String message;
        private Integer timeoutMs;
        private Integer historyLimit;

        /** {@code --local}：本地嵌入式 agent 运行时（chat 别名默认为 true）。 */
        public Builder local(boolean local) { this.local = local; return this; }
        /** {@code --url}：Gateway WebSocket URL。 */
        public Builder url(String url) { this.url = url; return this; }
        /** {@code --token}：Gateway 令牌。 */
        public Builder token(String token) { this.token = token; return this; }
        /** {@code --password}：Gateway 密码。 */
        public Builder password(String password) { this.password = password; return this; }
        /** {@code --session}：会话键。 */
        public Builder session(String session) { this.session = session; return this; }
        /** {@code --deliver}：投递助手回复。 */
        public Builder deliver(boolean deliver) { this.deliver = deliver; return this; }
        /** {@code --thinking}：思考级别覆盖。 */
        public Builder thinking(String thinking) { this.thinking = thinking; return this; }
        /** {@code --message}：连接后发送初始消息。 */
        public Builder message(String message) { this.message = message; return this; }
        /** {@code --timeout-ms}：Agent 超时毫秒数。 */
        public Builder timeoutMs(Integer timeoutMs) { this.timeoutMs = timeoutMs; return this; }
        /** {@code --history-limit}：加载的历史条目数。 */
        public Builder historyLimit(Integer historyLimit) { this.historyLimit = historyLimit; return this; }

        /**
         * @return 不可变 {@link ChatOptions}
         */
        public ChatOptions build() {
            return new ChatOptions(this);
        }
    }
}
