package io.github.easy4j.openclaw.cli.opts;

import io.github.easy4j.openclaw.util.OpenClawLists;
import io.github.easy4j.openclaw.cli.args.CliSubArgs;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * {@code openclaw proxy}：运行 OpenClaw 调试代理并检查捕获的流量。
 * <p>
 * 支持 {@code start}、{@code run [cmd...]}、{@code validate}、{@code coverage}、{@code sessions}、
 * {@code query}、{@code blob}、{@code purge} 子命令。
 * </p>
 *
 * @see <a href="https://docs.openclaw.ai/cli/proxy">proxy CLI</a>
 */
public final class ProxyOptions implements CliSubArgs {

    /** 子命令模式。 */
    public enum Mode {
        START,
        RUN,
        VALIDATE,
        COVERAGE,
        SESSIONS,
        QUERY,
        BLOB,
        PURGE
    }

    private final Mode mode;
    /** run：位置参数 {@code [cmd...]}，要运行的命令片段。 */
    private final List<String> runCommand;
    /** start/run：{@code --host} 绑定主机（默认 {@code 127.0.0.1}）。 */
    private final String host;
    /** start/run：{@code --port} 绑定端口。 */
    private final Integer port;
    /** validate：{@code --json} 机器可读输出。 */
    private final boolean json;
    /** validate：{@code --proxy-url} 待校验的代理 URL。 */
    private final String proxyUrl;
    /** validate：{@code --proxy-ca-file} 校验 HTTPS 代理端点的 CA bundle 文件。 */
    private final String proxyCaFile;
    /** validate：{@code --allowed-url}（可重复）预期成功的目标 URL。 */
    private final List<String> allowedUrls;
    /** validate：{@code --denied-url}（可重复）预期被代理阻断的目标 URL。 */
    private final List<String> deniedUrls;
    /** validate：{@code --apns-reachable} 同时校验沙箱 APNs HTTP/2 可达性。 */
    private final boolean apnsReachable;
    /** validate：{@code --apns-authority} 配合 {@code --apns-reachable} 探测的 APNs authority。 */
    private final String apnsAuthority;
    /** validate：{@code --timeout-ms} 每请求超时毫秒数。 */
    private final Integer timeoutMs;
    /** sessions：{@code --limit} 最大显示会话数。 */
    private final Integer limit;
    /** query：{@code --preset}（必选）查询预设。 */
    private final String preset;
    /** query：{@code --session} 限定到捕获会话 id。 */
    private final String session;
    /** blob：{@code --id}（必选）Blob id。 */
    private final String blobId;

    private ProxyOptions(Builder b) {
        this.mode = b.mode;
        this.runCommand = OpenClawLists.copyOf(b.runCommand);
        this.host = b.host;
        this.port = b.port;
        this.json = b.json;
        this.proxyUrl = b.proxyUrl;
        this.proxyCaFile = b.proxyCaFile;
        this.allowedUrls = OpenClawLists.copyOf(b.allowedUrls);
        this.deniedUrls = OpenClawLists.copyOf(b.deniedUrls);
        this.apnsReachable = b.apnsReachable;
        this.apnsAuthority = b.apnsAuthority;
        this.timeoutMs = b.timeoutMs;
        this.limit = b.limit;
        this.preset = b.preset;
        this.session = b.session;
        this.blobId = b.blobId;
    }

    /**
     * @return 新 {@link Builder}（默认 {@link Mode#START}）
     */
    public static Builder builder() {
        return new Builder();
    }

    @Override
    public List<String> toSubcommandArguments() {
        List<String> out = new ArrayList<>();
        if (mode != null) {
            out.add(mode.name().toLowerCase());
        }
        if (runCommand != null && !runCommand.isEmpty()) {
            out.addAll(runCommand);
        }
        OpenClawCliArgv.addIfPresent(out, "--host", host);
        OpenClawCliArgv.addIfNotNull(out, "--port", port);
        OpenClawCliArgv.addFlag(out, "--json", json);
        OpenClawCliArgv.addIfPresent(out, "--proxy-url", proxyUrl);
        OpenClawCliArgv.addIfPresent(out, "--proxy-ca-file", proxyCaFile);
        OpenClawCliArgv.addRepeatable(out, "--allowed-url", allowedUrls);
        OpenClawCliArgv.addRepeatable(out, "--denied-url", deniedUrls);
        OpenClawCliArgv.addFlag(out, "--apns-reachable", apnsReachable);
        OpenClawCliArgv.addIfPresent(out, "--apns-authority", apnsAuthority);
        OpenClawCliArgv.addIfNotNull(out, "--timeout-ms", timeoutMs);
        OpenClawCliArgv.addIfNotNull(out, "--limit", limit);
        OpenClawCliArgv.addIfPresent(out, "--preset", preset);
        OpenClawCliArgv.addIfPresent(out, "--session", session);
        OpenClawCliArgv.addIfPresent(out, "--id", blobId);
        return Collections.unmodifiableList(out);
    }

    /**
     * {@link ProxyOptions} 构建器。
     */
    public static final class Builder {
        private Mode mode = Mode.START;
        private List<String> runCommand;
        private String host;
        private Integer port;
        private boolean json;
        private String proxyUrl;
        private String proxyCaFile;
        private List<String> allowedUrls;
        private List<String> deniedUrls;
        private boolean apnsReachable;
        private String apnsAuthority;
        private Integer timeoutMs;
        private Integer limit;
        private String preset;
        private String session;
        private String blobId;

        /** 显式指定 {@link Mode}。 */
        public Builder mode(Mode mode) { this.mode = mode; return this; }
        /** 切换为 {@code start} 子命令。 */
        public Builder start() { this.mode = Mode.START; return this; }
        /** 切换为 {@code run [cmd...]} 子命令。 */
        public Builder run(List<String> cmd) { this.mode = Mode.RUN; this.runCommand = cmd; return this; }
        /** 切换为 {@code validate} 子命令。 */
        public Builder validate() { this.mode = Mode.VALIDATE; return this; }
        /** 切换为 {@code coverage} 子命令。 */
        public Builder coverage() { this.mode = Mode.COVERAGE; return this; }
        /** 切换为 {@code sessions} 子命令。 */
        public Builder sessions() { this.mode = Mode.SESSIONS; return this; }
        /** 切换为 {@code query} 子命令。 */
        public Builder query() { this.mode = Mode.QUERY; return this; }
        /** 切换为 {@code blob} 子命令。 */
        public Builder blob() { this.mode = Mode.BLOB; return this; }
        /** 切换为 {@code purge} 子命令。 */
        public Builder purge() { this.mode = Mode.PURGE; return this; }
        /** start/run：{@code --host} 绑定主机。 */
        public Builder host(String host) { this.host = host; return this; }
        /** start/run：{@code --port} 绑定端口。 */
        public Builder port(Integer port) { this.port = port; return this; }
        /** validate：{@code --json} 机器可读输出。 */
        public Builder json(boolean json) { this.json = json; return this; }
        /** validate：{@code --proxy-url} 待校验的代理 URL。 */
        public Builder proxyUrl(String proxyUrl) { this.proxyUrl = proxyUrl; return this; }
        /** validate：{@code --proxy-ca-file} CA bundle 文件。 */
        public Builder proxyCaFile(String proxyCaFile) { this.proxyCaFile = proxyCaFile; return this; }
        /** validate：{@code --allowed-url}（可重复）预期成功的目标 URL。 */
        public Builder allowedUrls(List<String> urls) { this.allowedUrls = urls; return this; }
        /** validate：{@code --denied-url}（可重复）预期被阻断的目标 URL。 */
        public Builder deniedUrls(List<String> urls) { this.deniedUrls = urls; return this; }
        /** validate：{@code --apns-reachable} 同时校验 APNs 可达性。 */
        public Builder apnsReachable(boolean apnsReachable) { this.apnsReachable = apnsReachable; return this; }
        /** validate：{@code --apns-authority} APNs authority。 */
        public Builder apnsAuthority(String apnsAuthority) { this.apnsAuthority = apnsAuthority; return this; }
        /** validate：{@code --timeout-ms} 每请求超时毫秒数。 */
        public Builder timeoutMs(Integer timeoutMs) { this.timeoutMs = timeoutMs; return this; }
        /** sessions：{@code --limit} 最大显示会话数。 */
        public Builder limit(Integer limit) { this.limit = limit; return this; }
        /** query：{@code --preset}（必选）查询预设。 */
        public Builder preset(String preset) { this.preset = preset; return this; }
        /** query：{@code --session} 限定到捕获会话 id。 */
        public Builder session(String session) { this.session = session; return this; }
        /** blob：{@code --id}（必选）Blob id。 */
        public Builder blobId(String blobId) { this.blobId = blobId; return this; }

        /**
         * @return 不可变 {@link ProxyOptions}
         */
        public ProxyOptions build() {
            return new ProxyOptions(this);
        }
    }
}
