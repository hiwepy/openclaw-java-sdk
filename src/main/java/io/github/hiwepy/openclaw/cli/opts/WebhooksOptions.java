package io.github.hiwepy.openclaw.cli.opts;

import io.github.hiwepy.openclaw.cli.args.CliSubArgs;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * {@code openclaw webhooks}：围绕 Gmail Pub/Sub 等推送集成，配置 watch、订阅与 OpenClaw webhook 投递端点。
 * <p>当前建模覆盖 {@code webhooks gmail setup|run} 的常用 flag；细节见 Gmail Pub/Sub 指南链接。</p>
 *
 * @see <a href="https://docs.openclaw.ai/cli/webhooks">webhooks CLI</a>
 */
public final class WebhooksOptions implements CliSubArgs {

    /**
     * {@code webhooks gmail}：一次性配置或常驻运行 watch 与续期循环。
     */
    public enum GmailMode {
        /** {@code gmail setup}：写 watch、主题、订阅与 webhook 绑定。 */
        SETUP,
        /** {@code gmail run}：运行 {@code gog watch serve} 与自动续订循环。 */
        RUN
    }

    /** setup 或 run。 */
    private final GmailMode gmailMode;
    /**
     * {@code --account} 必填：要监听的 Gmail 账号邮箱。
     */
    private final String account;
    /**
     * {@code --project} GCP 项目 id（可选，用于 Pub/Sub 资源命名）。
     */
    private final String project;
    /**
     * {@code --topic} Cloud Pub/Sub 主题名。
     */
    private final String topic;
    /**
     * {@code --subscription} 推送订阅名。
     */
    private final String subscription;
    /**
     * {@code --label} Gmail 标签过滤。
     */
    private final String label;
    /**
     * {@code --hook-url} OpenClaw 将接收推送的 HTTPS webhook 基址。
     */
    private final String hookUrl;
    /**
     * {@code --hook-token} webhook 共享密钥或校验 token。
     */
    private final String hookToken;
    /**
     * {@code --push-token} 与 Google 推送验证相关的 token 配置。
     */
    private final String pushToken;
    /**
     * {@code --bind} 本地监听地址（如内嵌接收端）。
     */
    private final String bind;
    /**
     * {@code --port} 本地 HTTP 端口。
     */
    private final String port;
    /**
     * {@code --path} webhook HTTP 路径。
     */
    private final String path;
    /**
     * {@code --include-body} 是否在推送处理中包含邮件正文（注意体积与隐私）。
     */
    private final boolean includeBody;
    /**
     * {@code --max-bytes} 单条推送或正文截断上限。
     */
    private final String maxBytes;
    /**
     * {@code --renew-minutes} watch 自动续期间隔。
     */
    private final String renewMinutes;
    /**
     * {@code --tailscale} Tailscale 集成模式或开关（文档与 Gmail 指南一致）。
     */
    private final String tailscale;
    /**
     * {@code --tailscale-path} Serve/Funnel 路径片段。
     */
    private final String tailscalePath;
    /**
     * {@code --tailscale-target} Tailscale 目标服务描述。
     */
    private final String tailscaleTarget;
    /**
     * {@code --push-endpoint} 显式推送回调端点覆盖。
     */
    private final String pushEndpoint;
    /**
     * setup：{@code --json} 机器可读输出计划结果。
     */
    private final boolean json;
    /**
     * 其它 argv。
     */
    private final List<String> extra;

    /**
     * @param b 构建器快照
     */
    private WebhooksOptions(Builder b) {
        this.gmailMode = b.gmailMode;
        this.account = b.account;
        this.project = b.project;
        this.topic = b.topic;
        this.subscription = b.subscription;
        this.label = b.label;
        this.hookUrl = b.hookUrl;
        this.hookToken = b.hookToken;
        this.pushToken = b.pushToken;
        this.bind = b.bind;
        this.port = b.port;
        this.path = b.path;
        this.includeBody = b.includeBody;
        this.maxBytes = b.maxBytes;
        this.renewMinutes = b.renewMinutes;
        this.tailscale = b.tailscale;
        this.tailscalePath = b.tailscalePath;
        this.tailscaleTarget = b.tailscaleTarget;
        this.pushEndpoint = b.pushEndpoint;
        this.json = b.json;
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
        out.add("gmail");
        if (gmailMode == GmailMode.SETUP) {
            out.add("setup");
        } else {
            out.add("run");
        }
        OpenClawCliArgv.addIfPresent(out, "--account", account);
        OpenClawCliArgv.addIfPresent(out, "--project", project);
        OpenClawCliArgv.addIfPresent(out, "--topic", topic);
        OpenClawCliArgv.addIfPresent(out, "--subscription", subscription);
        OpenClawCliArgv.addIfPresent(out, "--label", label);
        OpenClawCliArgv.addIfPresent(out, "--hook-url", hookUrl);
        OpenClawCliArgv.addIfPresent(out, "--hook-token", hookToken);
        OpenClawCliArgv.addIfPresent(out, "--push-token", pushToken);
        OpenClawCliArgv.addIfPresent(out, "--bind", bind);
        OpenClawCliArgv.addIfPresent(out, "--port", port);
        OpenClawCliArgv.addIfPresent(out, "--path", path);
        OpenClawCliArgv.addFlag(out, "--include-body", includeBody);
        OpenClawCliArgv.addIfPresent(out, "--max-bytes", maxBytes);
        OpenClawCliArgv.addIfPresent(out, "--renew-minutes", renewMinutes);
        OpenClawCliArgv.addIfPresent(out, "--tailscale", tailscale);
        OpenClawCliArgv.addIfPresent(out, "--tailscale-path", tailscalePath);
        OpenClawCliArgv.addIfPresent(out, "--tailscale-target", tailscaleTarget);
        OpenClawCliArgv.addIfPresent(out, "--push-endpoint", pushEndpoint);
        if (gmailMode == GmailMode.SETUP) {
            OpenClawCliArgv.addFlag(out, "--json", json);
        }
        OpenClawCliArgv.addExtra(out, extra);
        return Collections.unmodifiableList(out);
    }

    /**
     * {@link WebhooksOptions} 构建器。
     */
    public static final class Builder {
        private GmailMode gmailMode = GmailMode.SETUP;
        private String account;
        private String project;
        private String topic;
        private String subscription;
        private String label;
        private String hookUrl;
        private String hookToken;
        private String pushToken;
        private String bind;
        private String port;
        private String path;
        private boolean includeBody;
        private String maxBytes;
        private String renewMinutes;
        private String tailscale;
        private String tailscalePath;
        private String tailscaleTarget;
        private String pushEndpoint;
        private boolean json;
        private List<String> extra = new ArrayList<>();

        /**
         * @param accountEmail {@code --account}
         * @return {@code this}
         */
        public Builder gmailSetup(String accountEmail) {
            this.gmailMode = GmailMode.SETUP;
            this.account = accountEmail;
            return this;
        }

        /**
         * @param accountEmail {@code --account}
         * @return {@code this}
         */
        public Builder gmailRun(String accountEmail) {
            this.gmailMode = GmailMode.RUN;
            this.account = accountEmail;
            return this;
        }

        /**
         * @param project {@code --project}
         * @return {@code this}
         */
        public Builder project(String project) {
            this.project = project;
            return this;
        }

        /**
         * @param topic {@code --topic}
         * @return {@code this}
         */
        public Builder topic(String topic) {
            this.topic = topic;
            return this;
        }

        /**
         * @param subscription {@code --subscription}
         * @return {@code this}
         */
        public Builder subscription(String subscription) {
            this.subscription = subscription;
            return this;
        }

        /**
         * @param label {@code --label}
         * @return {@code this}
         */
        public Builder label(String label) {
            this.label = label;
            return this;
        }

        /**
         * @param hookUrl {@code --hook-url}
         * @return {@code this}
         */
        public Builder hookUrl(String hookUrl) {
            this.hookUrl = hookUrl;
            return this;
        }

        /**
         * @param hookToken {@code --hook-token}
         * @return {@code this}
         */
        public Builder hookToken(String hookToken) {
            this.hookToken = hookToken;
            return this;
        }

        /**
         * @param pushToken {@code --push-token}
         * @return {@code this}
         */
        public Builder pushToken(String pushToken) {
            this.pushToken = pushToken;
            return this;
        }

        /**
         * @param bind {@code --bind}
         * @return {@code this}
         */
        public Builder bind(String bind) {
            this.bind = bind;
            return this;
        }

        /**
         * @param port {@code --port}
         * @return {@code this}
         */
        public Builder port(String port) {
            this.port = port;
            return this;
        }

        /**
         * @param path {@code --path}
         * @return {@code this}
         */
        public Builder path(String path) {
            this.path = path;
            return this;
        }

        /**
         * @param includeBody {@code --include-body}
         * @return {@code this}
         */
        public Builder includeBody(boolean includeBody) {
            this.includeBody = includeBody;
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
         * @param renewMinutes {@code --renew-minutes}
         * @return {@code this}
         */
        public Builder renewMinutes(String renewMinutes) {
            this.renewMinutes = renewMinutes;
            return this;
        }

        /**
         * @param tailscale {@code --tailscale}
         * @return {@code this}
         */
        public Builder tailscale(String tailscale) {
            this.tailscale = tailscale;
            return this;
        }

        /**
         * @param tailscalePath {@code --tailscale-path}
         * @return {@code this}
         */
        public Builder tailscalePath(String tailscalePath) {
            this.tailscalePath = tailscalePath;
            return this;
        }

        /**
         * @param tailscaleTarget {@code --tailscale-target}
         * @return {@code this}
         */
        public Builder tailscaleTarget(String tailscaleTarget) {
            this.tailscaleTarget = tailscaleTarget;
            return this;
        }

        /**
         * @param pushEndpoint {@code --push-endpoint}
         * @return {@code this}
         */
        public Builder pushEndpoint(String pushEndpoint) {
            this.pushEndpoint = pushEndpoint;
            return this;
        }

        /**
         * @param json setup：{@code --json}
         * @return {@code this}
         */
        public Builder json(boolean json) {
            this.json = json;
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
         * @return 不可变 {@link WebhooksOptions}
         */
        public WebhooksOptions build() {
            return new WebhooksOptions(this);
        }
    }
}
