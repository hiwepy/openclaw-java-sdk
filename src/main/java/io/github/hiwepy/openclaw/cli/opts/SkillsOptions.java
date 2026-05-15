package io.github.hiwepy.openclaw.cli.opts;

import io.github.hiwepy.openclaw.cli.args.CliSubArgs;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * {@code openclaw skills}：在 ClawHub/本地注册表搜索、安装与更新 agent skills，并检查工作区技能是否满足依赖。
 * <p>裸 {@code openclaw skills} 与 {@code skills list} 等价；安装流与插件生态相关，具体源与版本解析以官方 skills CLI 为准。</p>
 *
 * @see <a href="https://docs.openclaw.ai/cli/skills">skills CLI</a>
 */
public final class SkillsOptions implements CliSubArgs {

    /**
     * skills 子命令：默认列表、搜索、安装、更新、信息与依赖检查。
     */
    public enum Verb {
        /**
         * 与裸 {@code openclaw skills} 一致：不显式 {@code list} token，由 CLI 走默认 list 行为。
         */
        DEFAULT_LIST,
        /** {@code skills search}：按关键词检索 ClawHub 等目录。 */
        SEARCH,
        /** {@code skills install}：按 slug 安装到工作区技能目录。 */
        INSTALL,
        /** {@code skills update}：更新单个 skill 或 {@code --all}。 */
        UPDATE,
        /** {@code skills list}：列出已安装/可见技能。 */
        LIST,
        /** {@code skills info}：展示元数据与入口文件路径。 */
        INFO,
        /** {@code skills check}：验证当前 workspace 技能依赖是否满足。 */
        CHECK
    }

    /** search / install / update / list / info / check 或默认 list。 */
    private final Verb verb;
    /**
     * search：关键词位置参数列表。
     */
    private final List<String> searchWords;
    /**
     * search：{@code --limit} 返回条数上限。
     */
    private final Integer searchLimit;
    /**
     * search：{@code --json}。
     */
    private final boolean searchJson;
    /**
     * install：skill slug 位置参数。
     */
    private final String installSlug;
    /**
     * install：{@code --version} 固定版本或 tag。
     */
    private final String installVersion;
    /**
     * install：{@code --force} 覆盖已存在安装。
     */
    private final boolean installForce;
    /**
     * update：目标 slug；与 {@code updateAll} 二选一语义由 Builder 保证。
     */
    private final String updateSlug;
    /**
     * update：{@code --all} 更新全部已安装 skills。
     */
    private final boolean updateAll;
    /**
     * list / 默认：{@code --eligible} 只显示依赖已就绪的技能。
     */
    private final boolean listEligible;
    /**
     * list / 默认：{@code --json}。
     */
    private final boolean listJson;
    /**
     * list / 默认：{@code --verbose} 更详细条目。
     */
    private final boolean listVerbose;
    /**
     * info：skill 名称位置参数。
     */
    private final String infoName;
    /**
     * info：{@code --json}。
     */
    private final boolean infoJson;
    /**
     * check：{@code --json} 输出检查结果。
     */
    private final boolean checkJson;
    /**
     * 其它 argv。
     */
    private final List<String> extra;

    /**
     * @param b 构建器快照
     */
    private SkillsOptions(Builder b) {
        this.verb = b.verb;
        this.searchWords = b.searchWords == null ? List.of() : List.copyOf(b.searchWords);
        this.searchLimit = b.searchLimit;
        this.searchJson = b.searchJson;
        this.installSlug = b.installSlug;
        this.installVersion = b.installVersion;
        this.installForce = b.installForce;
        this.updateSlug = b.updateSlug;
        this.updateAll = b.updateAll;
        this.listEligible = b.listEligible;
        this.listJson = b.listJson;
        this.listVerbose = b.listVerbose;
        this.infoName = b.infoName;
        this.infoJson = b.infoJson;
        this.checkJson = b.checkJson;
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
        switch (verb) {
            case DEFAULT_LIST:
                break;
            case SEARCH:
                out.add("search");
                out.addAll(searchWords);
                OpenClawCliArgv.addIfNotNull(out, "--limit", searchLimit);
                OpenClawCliArgv.addFlag(out, "--json", searchJson);
                break;
            case INSTALL:
                out.add("install");
                if (installSlug != null && !installSlug.isBlank()) {
                    out.add(installSlug.trim());
                }
                OpenClawCliArgv.addIfPresent(out, "--version", installVersion);
                OpenClawCliArgv.addFlag(out, "--force", installForce);
                break;
            case UPDATE:
                out.add("update");
                if (updateAll) {
                    out.add("--all");
                } else if (updateSlug != null && !updateSlug.isBlank()) {
                    out.add(updateSlug.trim());
                }
                break;
            case LIST:
                out.add("list");
                OpenClawCliArgv.addFlag(out, "--eligible", listEligible);
                OpenClawCliArgv.addFlag(out, "--json", listJson);
                OpenClawCliArgv.addFlag(out, "--verbose", listVerbose);
                break;
            case INFO:
                out.add("info");
                if (infoName != null && !infoName.isBlank()) {
                    out.add(infoName.trim());
                }
                OpenClawCliArgv.addFlag(out, "--json", infoJson);
                break;
            case CHECK:
                out.add("check");
                OpenClawCliArgv.addFlag(out, "--json", checkJson);
                break;
            default:
                break;
        }
        if (verb == Verb.DEFAULT_LIST) {
            OpenClawCliArgv.addFlag(out, "--eligible", listEligible);
            OpenClawCliArgv.addFlag(out, "--json", listJson);
            OpenClawCliArgv.addFlag(out, "--verbose", listVerbose);
        }
        OpenClawCliArgv.addExtra(out, extra);
        return Collections.unmodifiableList(out);
    }

    /**
     * {@link SkillsOptions} 构建器。
     */
    public static final class Builder {
        private Verb verb = Verb.DEFAULT_LIST;
        private List<String> searchWords = new ArrayList<>();
        private Integer searchLimit;
        private boolean searchJson;
        private String installSlug;
        private String installVersion;
        private boolean installForce;
        private String updateSlug;
        private boolean updateAll;
        private boolean listEligible;
        private boolean listJson;
        private boolean listVerbose;
        private String infoName;
        private boolean infoJson;
        private boolean checkJson;
        private List<String> extra = new ArrayList<>();

        /**
         * @return {@code this}（无子命令，等同 list flag 表面）
         */
        public Builder defaultList() {
            this.verb = Verb.DEFAULT_LIST;
            return this;
        }

        /**
         * {@code skills search [words...]}。
         *
         * @param queryWords 搜索词
         * @return {@code this}
         */
        public Builder search(String... queryWords) {
            this.verb = Verb.SEARCH;
            this.searchWords = new ArrayList<>();
            if (queryWords != null) {
                for (String w : queryWords) {
                    if (w != null && !w.isBlank()) {
                        searchWords.add(w.trim());
                    }
                }
            }
            return this;
        }

        /**
         * @param limit search：{@code --limit}
         * @return {@code this}
         */
        public Builder searchLimit(int limit) {
            this.searchLimit = limit;
            return this;
        }

        /**
         * @param json search：{@code --json}
         * @return {@code this}
         */
        public Builder searchJson(boolean json) {
            this.searchJson = json;
            return this;
        }

        /**
         * @param slug install：slug
         * @return {@code this}
         */
        public Builder install(String slug) {
            this.verb = Verb.INSTALL;
            this.installSlug = slug;
            return this;
        }

        /**
         * @param version install：{@code --version}
         * @return {@code this}
         */
        public Builder installVersion(String version) {
            this.installVersion = version;
            return this;
        }

        /**
         * @param force install：{@code --force}
         * @return {@code this}
         */
        public Builder installForce(boolean force) {
            this.installForce = force;
            return this;
        }

        /**
         * @param slug update：slug
         * @return {@code this}
         */
        public Builder update(String slug) {
            this.verb = Verb.UPDATE;
            this.updateSlug = slug;
            this.updateAll = false;
            return this;
        }

        /**
         * @param all update：{@code --all}
         * @return {@code this}
         */
        public Builder updateAll(boolean all) {
            this.verb = Verb.UPDATE;
            this.updateAll = all;
            this.updateSlug = null;
            return this;
        }

        /**
         * @return {@code this}（{@code skills list}）
         */
        public Builder list() {
            this.verb = Verb.LIST;
            return this;
        }

        /**
         * @param eligible list：{@code --eligible}
         * @return {@code this}
         */
        public Builder listEligible(boolean eligible) {
            this.listEligible = eligible;
            return this;
        }

        /**
         * @param json list：{@code --json}
         * @return {@code this}
         */
        public Builder listJson(boolean json) {
            this.listJson = json;
            return this;
        }

        /**
         * @param verbose list：{@code --verbose}
         * @return {@code this}
         */
        public Builder listVerbose(boolean verbose) {
            this.listVerbose = verbose;
            return this;
        }

        /**
         * @param name info：skill 名
         * @return {@code this}
         */
        public Builder info(String name) {
            this.verb = Verb.INFO;
            this.infoName = name;
            return this;
        }

        /**
         * @param json info：{@code --json}
         * @return {@code this}
         */
        public Builder infoJson(boolean json) {
            this.infoJson = json;
            return this;
        }

        /**
         * @return {@code this}（{@code skills check}）
         */
        public Builder check() {
            this.verb = Verb.CHECK;
            return this;
        }

        /**
         * @param json check：{@code --json}
         * @return {@code this}
         */
        public Builder checkJson(boolean json) {
            this.checkJson = json;
            return this;
        }

        /**
         * 追加原始 token。
         *
         * @param tokens argv 片段
         * @return {@code this}
         */
        public Builder extra(String... tokens) {
            if (tokens != null) {
                Collections.addAll(extra, tokens);
            }
            return this;
        }

        /**
         * @return 不可变 {@link SkillsOptions}
         */
        public SkillsOptions build() {
            return new SkillsOptions(this);
        }
    }
}
