package io.github.hiwepy.openclaw.cli.opts;

import io.github.hiwepy.openclaw.util.OpenClawLists;
import io.github.hiwepy.openclaw.util.OpenClawStrings;
import io.github.hiwepy.openclaw.cli.args.CliSubArgs;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * {@code openclaw models}：模型发现、默认与回退链、别名列表，以及各厂商认证辅助子命令。
 * <p>{@code models status} 可展示解析后的默认模型与鉴权概况；{@code --probe} 会发起真实探测请求（可能消耗额度）。更深子命令用 {@link Builder#extra(String...)}。</p>
 *
 * @see <a href="https://docs.openclaw.ai/cli/models">models CLI</a>
 */
public final class ModelsOptions implements CliSubArgs {

    /**
     * models 子命令路径（status、list、set、scan、aliases、fallbacks、auth 变体等）。
     */
    public enum Mode {
        /** {@code models status}：解析默认/回退与鉴权概况，可选 live probe。 */
        STATUS,
        /** {@code models list}：列出已加载模型目录。 */
        LIST,
        /** {@code models set}：设置默认模型或别名目标。 */
        SET,
        /** {@code models scan}：扫描/刷新模型清单。 */
        SCAN,
        /** {@code models aliases list}。 */
        ALIASES_LIST,
        /** {@code models fallbacks list}。 */
        FALLBACKS_LIST,
        /** {@code models auth add}：交互式认证向导。 */
        AUTH_ADD,
        /** {@code models auth login}：运行某 provider 插件的 OAuth 或密钥流程。 */
        AUTH_LOGIN,
        /** {@code models auth setup-token}：TTY 下走 provider 的 token 配置方法。 */
        AUTH_SETUP_TOKEN,
        /** {@code models auth paste-token}：粘贴外部获得的 token 并写入配置。 */
        AUTH_PASTE_TOKEN
    }

    /** 当前建模到的 models 子命令。 */
    private final Mode mode;
    /**
     * status：{@code --json} 机器可读状态。
     */
    private final boolean statusJson;
    /**
     * status：{@code --plain} 简化人类可读输出。
     */
    private final boolean statusPlain;
    /**
     * status：{@code --check} 在过期/缺失鉴权时非零退出（文档：1=过期缺失，2=将过期）。
     */
    private final boolean statusCheck;
    /**
     * status：{@code --probe} 对每个配置档发起实时鉴权探测（可能触发限额）。
     */
    private final boolean probe;
    /**
     * status：{@code --probe-provider} 只探测单一 provider。
     */
    private final String probeProvider;
    /**
     * status：{@code --probe-profile} 逗号分隔或重复的 profile id 过滤探测集合。
     */
    private final String probeProfile;
    /**
     * status：{@code --probe-timeout} 单次探测超时。
     */
    private final String probeTimeout;
    /**
     * status：{@code --probe-concurrency} 并发探测上限。
     */
    private final String probeConcurrency;
    /**
     * status：{@code --probe-max-tokens} 探测请求允许的最大 token 用量上限。
     */
    private final String probeMaxTokens;
    /**
     * status：{@code --agent} 指定已配置 agent，以查看其模型与鉴权视图。
     */
    private final String agent;
    /**
     * set：位置参数 {@code provider/model}、别名或文档所述可解析形式。
     */
    private final String modelOrAlias;
    /**
     * auth 子命令：{@code --provider} 目标 provider id。
     */
    private final String authProvider;
    /**
     * auth login：{@code --set-default} 登录成功后设为默认配置。
     */
    private final boolean authSetDefault;
    /**
     * paste-token：{@code --profile-id} 写入的配置档 id（默认 {@code :manual}）。
     */
    private final String pasteProfileId;
    /**
     * paste-token：{@code --expires-in} 相对时长换算绝对过期时间（如 {@code 365d}）。
     */
    private final String pasteExpiresIn;
    /**
     * 其它 argv。
     */
    private final List<String> extra;

    /**
     * @param b 构建器快照
     */
    private ModelsOptions(Builder b) {
        this.mode = b.mode;
        this.statusJson = b.statusJson;
        this.statusPlain = b.statusPlain;
        this.statusCheck = b.statusCheck;
        this.probe = b.probe;
        this.probeProvider = b.probeProvider;
        this.probeProfile = b.probeProfile;
        this.probeTimeout = b.probeTimeout;
        this.probeConcurrency = b.probeConcurrency;
        this.probeMaxTokens = b.probeMaxTokens;
        this.agent = b.agent;
        this.modelOrAlias = b.modelOrAlias;
        this.authProvider = b.authProvider;
        this.authSetDefault = b.authSetDefault;
        this.pasteProfileId = b.pasteProfileId;
        this.pasteExpiresIn = b.pasteExpiresIn;
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
            case STATUS:
                out.add("status");
                OpenClawCliArgv.addFlag(out, "--json", statusJson);
                OpenClawCliArgv.addFlag(out, "--plain", statusPlain);
                OpenClawCliArgv.addFlag(out, "--check", statusCheck);
                OpenClawCliArgv.addFlag(out, "--probe", probe);
                OpenClawCliArgv.addIfPresent(out, "--probe-provider", probeProvider);
                OpenClawCliArgv.addIfPresent(out, "--probe-profile", probeProfile);
                OpenClawCliArgv.addIfPresent(out, "--probe-timeout", probeTimeout);
                OpenClawCliArgv.addIfPresent(out, "--probe-concurrency", probeConcurrency);
                OpenClawCliArgv.addIfPresent(out, "--probe-max-tokens", probeMaxTokens);
                OpenClawCliArgv.addIfPresent(out, "--agent", agent);
                break;
            case LIST:
                out.add("list");
                break;
            case SET:
                out.add("set");
                if (modelOrAlias != null && OpenClawStrings.isNotBlank(modelOrAlias)) {
                    out.add(modelOrAlias.trim());
                }
                break;
            case SCAN:
                out.add("scan");
                break;
            case ALIASES_LIST:
                out.add("aliases");
                out.add("list");
                break;
            case FALLBACKS_LIST:
                out.add("fallbacks");
                out.add("list");
                break;
            case AUTH_ADD:
                out.add("auth");
                out.add("add");
                break;
            case AUTH_LOGIN:
                out.add("auth");
                out.add("login");
                OpenClawCliArgv.addIfPresent(out, "--provider", authProvider);
                OpenClawCliArgv.addFlag(out, "--set-default", authSetDefault);
                break;
            case AUTH_SETUP_TOKEN:
                out.add("auth");
                out.add("setup-token");
                OpenClawCliArgv.addIfPresent(out, "--provider", authProvider);
                break;
            case AUTH_PASTE_TOKEN:
                out.add("auth");
                out.add("paste-token");
                OpenClawCliArgv.addIfPresent(out, "--provider", authProvider);
                OpenClawCliArgv.addIfPresent(out, "--profile-id", pasteProfileId);
                OpenClawCliArgv.addIfPresent(out, "--expires-in", pasteExpiresIn);
                break;
            default:
                break;
        }
        OpenClawCliArgv.addExtra(out, extra);
        return Collections.unmodifiableList(out);
    }

    /**
     * {@link ModelsOptions} 构建器。
     */
    public static final class Builder {
        private Mode mode = Mode.STATUS;
        private boolean statusJson;
        private boolean statusPlain;
        private boolean statusCheck;
        private boolean probe;
        private String probeProvider;
        private String probeProfile;
        private String probeTimeout;
        private String probeConcurrency;
        private String probeMaxTokens;
        private String agent;
        private String modelOrAlias;
        private String authProvider;
        private boolean authSetDefault;
        private String pasteProfileId;
        private String pasteExpiresIn;
        private List<String> extra = new ArrayList<>();

        /**
         * @return {@code this}（{@code models status}）
         */
        public Builder status() {
            this.mode = Mode.STATUS;
            return this;
        }

        /**
         * @param json status：{@code --json}
         * @return {@code this}
         */
        public Builder statusJson(boolean json) {
            this.statusJson = json;
            return this;
        }

        /**
         * @param plain status：{@code --plain}
         * @return {@code this}
         */
        public Builder statusPlain(boolean plain) {
            this.statusPlain = plain;
            return this;
        }

        /**
         * @param check status：{@code --check}
         * @return {@code this}
         */
        public Builder statusCheck(boolean check) {
            this.statusCheck = check;
            return this;
        }

        /**
         * @param probe status：{@code --probe}
         * @return {@code this}
         */
        public Builder probe(boolean probe) {
            this.probe = probe;
            return this;
        }

        /**
         * @param probeProvider {@code --probe-provider}
         * @return {@code this}
         */
        public Builder probeProvider(String probeProvider) {
            this.probeProvider = probeProvider;
            return this;
        }

        /**
         * @param probeProfile {@code --probe-profile}
         * @return {@code this}
         */
        public Builder probeProfile(String probeProfile) {
            this.probeProfile = probeProfile;
            return this;
        }

        /**
         * @param probeTimeout {@code --probe-timeout}
         * @return {@code this}
         */
        public Builder probeTimeout(String probeTimeout) {
            this.probeTimeout = probeTimeout;
            return this;
        }

        /**
         * @param probeConcurrency {@code --probe-concurrency}
         * @return {@code this}
         */
        public Builder probeConcurrency(String probeConcurrency) {
            this.probeConcurrency = probeConcurrency;
            return this;
        }

        /**
         * @param probeMaxTokens {@code --probe-max-tokens}
         * @return {@code this}
         */
        public Builder probeMaxTokens(String probeMaxTokens) {
            this.probeMaxTokens = probeMaxTokens;
            return this;
        }

        /**
         * @param agent {@code --agent}
         * @return {@code this}
         */
        public Builder agent(String agent) {
            this.agent = agent;
            return this;
        }

        /**
         * @return {@code this}（{@code models list}）
         */
        public Builder list() {
            this.mode = Mode.LIST;
            return this;
        }

        /**
         * @param modelOrAlias set：模型或别名
         * @return {@code this}
         */
        public Builder set(String modelOrAlias) {
            this.mode = Mode.SET;
            this.modelOrAlias = modelOrAlias;
            return this;
        }

        /**
         * @return {@code this}（{@code models scan}）
         */
        public Builder scan() {
            this.mode = Mode.SCAN;
            return this;
        }

        /**
         * @return {@code this}（{@code models aliases list}）
         */
        public Builder aliasesList() {
            this.mode = Mode.ALIASES_LIST;
            return this;
        }

        /**
         * @return {@code this}（{@code models fallbacks list}）
         */
        public Builder fallbacksList() {
            this.mode = Mode.FALLBACKS_LIST;
            return this;
        }

        /**
         * @return {@code this}（{@code models auth add}）
         */
        public Builder authAdd() {
            this.mode = Mode.AUTH_ADD;
            return this;
        }

        /**
         * @param provider {@code --provider}
         * @return {@code this}
         */
        public Builder authLogin(String provider) {
            this.mode = Mode.AUTH_LOGIN;
            this.authProvider = provider;
            return this;
        }

        /**
         * @param setDefault {@code --set-default}
         * @return {@code this}
         */
        public Builder authSetDefault(boolean setDefault) {
            this.authSetDefault = setDefault;
            return this;
        }

        /**
         * @param provider {@code --provider}
         * @return {@code this}
         */
        public Builder authSetupToken(String provider) {
            this.mode = Mode.AUTH_SETUP_TOKEN;
            this.authProvider = provider;
            return this;
        }

        /**
         * @param provider {@code --provider}
         * @return {@code this}
         */
        public Builder authPasteToken(String provider) {
            this.mode = Mode.AUTH_PASTE_TOKEN;
            this.authProvider = provider;
            return this;
        }

        /**
         * @param profileId {@code --profile-id}
         * @return {@code this}
         */
        public Builder pasteProfileId(String profileId) {
            this.pasteProfileId = profileId;
            return this;
        }

        /**
         * @param expiresIn {@code --expires-in}
         * @return {@code this}
         */
        public Builder pasteExpiresIn(String expiresIn) {
            this.pasteExpiresIn = expiresIn;
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
         * @return 不可变 {@link ModelsOptions}
         */
        public ModelsOptions build() {
            return new ModelsOptions(this);
        }
    }
}
