package com.github.hiwepy.openclaw.cli.opts;

import com.github.hiwepy.openclaw.cli.args.CliSubArgs;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * {@code openclaw plugins}：安装、启用、检查与更新 Gateway 插件、hook 包与兼容 bundle（Codex/Claude/Cursor）。
 * <p>安装等同运行第三方代码：优先固定版本；{@code --dangerously-force-unsafe-install} 仅绕过内置危险扫描误报，不绕过 {@code before_install} 策略阻止。</p>
 *
 * @see <a href="https://docs.openclaw.ai/cli/plugins">plugins CLI</a>
 */
public final class PluginsOptions implements CliSubArgs {

    /**
     * plugins 子命令：清单、安装、深度检查、开关、卸载、诊断、更新与 marketplace 列举。
     */
    public enum Mode {
        /** {@code plugins list}：展示已发现插件与格式（openclaw 或 bundle）。 */
        LIST,
        /** {@code plugins install}：从 ClawHub、npm、本地路径或 marketplace 安装。 */
        INSTALL,
        /** {@code plugins inspect}：运行时注册面、工具、hook、路由等深度自省。 */
        INSPECT,
        /** {@code plugins info}：{@code inspect} 的别名。 */
        INFO,
        /** {@code plugins enable}：打开某插件 id。 */
        ENABLE,
        /** {@code plugins disable}：关闭某插件 id。 */
        DISABLE,
        /** {@code plugins uninstall}：移除配置记录并默认删除安装目录。 */
        UNINSTALL,
        /** {@code plugins doctor}：汇总加载错误与兼容性提示。 */
        DOCTOR,
        /** {@code plugins update}：按 {@code plugins.installs} 记录升级单个或全部。 */
        UPDATE,
        /** {@code plugins marketplace list}：解析并列出 marketplace 清单中的插件条目。 */
        MARKETPLACE_LIST
    }

    /** 当前 plugins 子命令。 */
    private final Mode mode;
    /**
     * list：{@code --enabled} 仅显示已加载插件。
     */
    private final boolean listEnabled;
    /**
     * list：{@code --verbose} 逐条详细行而非表格摘要。
     */
    private final boolean listVerbose;
    /**
     * list：{@code --json} 机器可读库存加固态诊断。
     */
    private final boolean listJson;
    /**
     * install：包 spec、路径或 {@code clawhub:} 定位子的位置参数。
     */
    private final String installSpec;
    /**
     * install：{@code --force} 覆盖已存在同名安装目标。
     */
    private final boolean installForce;
    /**
     * install：{@code --pin} npm 安装时写入精确解析版本到 {@code plugins.installs}。
     */
    private final boolean installPin;
    /**
     * install：{@code --dangerously-force-unsafe-install} 在扫描报 critical 时仍继续（break-glass）。
     */
    private final boolean dangerouslyForceUnsafeInstall;
    /**
     * install：{@code --marketplace} 显式 marketplace 源（名称、owner/repo 或 URL）。
     */
    private final String marketplace;
    /**
     * install：{@code --link} 将本地目录加入 {@code plugins.load.paths} 而不复制。
     */
    private final boolean installLink;
    /**
     * inspect / info：插件 id，或配合 {@code inspectAll} 使用。
     */
    private final String inspectId;
    /**
     * inspect / info：{@code --json} 输出完整报告。
     */
    private final boolean inspectJson;
    /**
     * inspect：{@code --all}  fleet 级表格视图。
     */
    private final boolean inspectAll;
    /**
     * enable / disable / uninstall / update：目标插件 id 或 npm spec（update 文档语义）。
     */
    private final String pluginId;
    /**
     * uninstall：{@code --dry-run} 只展示将删除的配置项。
     */
    private final boolean uninstallDryRun;
    /**
     * uninstall：{@code --keep-files} 保留磁盘上的插件目录。
     */
    private final boolean uninstallKeepFiles;
    /**
     * update：{@code --all} 更新所有已跟踪安装。
     */
    private final boolean updateAll;
    /**
     * update：{@code --dry-run} 预览变更。
     */
    private final boolean updateDryRun;
    /**
     * update：{@code --yes} 在完整性哈希变化等场景跳过交互确认。
     */
    private final boolean yes;
    /**
     * marketplace list：marketplace 源位置参数（路径、{@code owner/repo}、git URL 等）。
     */
    private final String marketplaceSource;
    /**
     * marketplace list：{@code --json} 输出 manifest 解析结果。
     */
    private final boolean marketplaceJson;
    /**
     * 其它 argv。
     */
    private final List<String> extra;

    /**
     * @param b 构建器快照
     */
    private PluginsOptions(Builder b) {
        this.mode = b.mode;
        this.listEnabled = b.listEnabled;
        this.listVerbose = b.listVerbose;
        this.listJson = b.listJson;
        this.installSpec = b.installSpec;
        this.installForce = b.installForce;
        this.installPin = b.installPin;
        this.dangerouslyForceUnsafeInstall = b.dangerouslyForceUnsafeInstall;
        this.marketplace = b.marketplace;
        this.installLink = b.installLink;
        this.inspectId = b.inspectId;
        this.inspectJson = b.inspectJson;
        this.inspectAll = b.inspectAll;
        this.pluginId = b.pluginId;
        this.uninstallDryRun = b.uninstallDryRun;
        this.uninstallKeepFiles = b.uninstallKeepFiles;
        this.updateAll = b.updateAll;
        this.updateDryRun = b.updateDryRun;
        this.yes = b.yes;
        this.marketplaceSource = b.marketplaceSource;
        this.marketplaceJson = b.marketplaceJson;
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
            case LIST:
                out.add("list");
                OpenClawCliArgv.addFlag(out, "--enabled", listEnabled);
                OpenClawCliArgv.addFlag(out, "--verbose", listVerbose);
                OpenClawCliArgv.addFlag(out, "--json", listJson);
                break;
            case INSTALL:
                out.add("install");
                OpenClawCliArgv.addFlag(out, "-l", installLink);
                if (installSpec != null && !installSpec.isBlank()) {
                    out.add(installSpec.trim());
                }
                OpenClawCliArgv.addFlag(out, "--force", installForce);
                OpenClawCliArgv.addFlag(out, "--pin", installPin);
                OpenClawCliArgv.addFlag(out, "--dangerously-force-unsafe-install", dangerouslyForceUnsafeInstall);
                OpenClawCliArgv.addIfPresent(out, "--marketplace", marketplace);
                break;
            case INSPECT:
            case INFO:
                out.add(mode == Mode.INFO ? "info" : "inspect");
                if (inspectAll) {
                    out.add("--all");
                } else if (inspectId != null && !inspectId.isBlank()) {
                    out.add(inspectId.trim());
                }
                OpenClawCliArgv.addFlag(out, "--json", inspectJson);
                break;
            case ENABLE:
                out.add("enable");
                if (pluginId != null && !pluginId.isBlank()) {
                    out.add(pluginId.trim());
                }
                break;
            case DISABLE:
                out.add("disable");
                if (pluginId != null && !pluginId.isBlank()) {
                    out.add(pluginId.trim());
                }
                break;
            case UNINSTALL:
                out.add("uninstall");
                if (pluginId != null && !pluginId.isBlank()) {
                    out.add(pluginId.trim());
                }
                OpenClawCliArgv.addFlag(out, "--dry-run", uninstallDryRun);
                OpenClawCliArgv.addFlag(out, "--keep-files", uninstallKeepFiles);
                break;
            case DOCTOR:
                out.add("doctor");
                break;
            case UPDATE:
                out.add("update");
                if (updateAll) {
                    out.add("--all");
                } else if (pluginId != null && !pluginId.isBlank()) {
                    out.add(pluginId.trim());
                }
                OpenClawCliArgv.addFlag(out, "--dry-run", updateDryRun);
                OpenClawCliArgv.addFlag(out, "--dangerously-force-unsafe-install", dangerouslyForceUnsafeInstall);
                OpenClawCliArgv.addFlag(out, "--yes", yes);
                break;
            case MARKETPLACE_LIST:
                out.add("marketplace");
                out.add("list");
                if (marketplaceSource != null && !marketplaceSource.isBlank()) {
                    out.add(marketplaceSource.trim());
                }
                OpenClawCliArgv.addFlag(out, "--json", marketplaceJson);
                break;
            default:
                break;
        }
        OpenClawCliArgv.addExtra(out, extra);
        return Collections.unmodifiableList(out);
    }

    /**
     * {@link PluginsOptions} 构建器。
     */
    public static final class Builder {
        private Mode mode = Mode.LIST;
        private boolean listEnabled;
        private boolean listVerbose;
        private boolean listJson;
        private String installSpec;
        private boolean installForce;
        private boolean installPin;
        private boolean dangerouslyForceUnsafeInstall;
        private String marketplace;
        private boolean installLink;
        private String inspectId;
        private boolean inspectJson;
        private boolean inspectAll;
        private String pluginId;
        private boolean uninstallDryRun;
        private boolean uninstallKeepFiles;
        private boolean updateAll;
        private boolean updateDryRun;
        private boolean yes;
        private String marketplaceSource;
        private boolean marketplaceJson;
        private List<String> extra = new ArrayList<>();

        /**
         * @return {@code this}（{@code plugins list}）
         */
        public Builder list() {
            this.mode = Mode.LIST;
            return this;
        }

        /**
         * @param enabled list：{@code --enabled}
         * @return {@code this}
         */
        public Builder listEnabled(boolean enabled) {
            this.listEnabled = enabled;
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
         * @param json list：{@code --json}
         * @return {@code this}
         */
        public Builder listJson(boolean json) {
            this.listJson = json;
            return this;
        }

        /**
         * @param spec install：包 spec
         * @return {@code this}
         */
        public Builder install(String spec) {
            this.mode = Mode.INSTALL;
            this.installSpec = spec;
            return this;
        }

        /**
         * @param force {@code --force}
         * @return {@code this}
         */
        public Builder installForce(boolean force) {
            this.installForce = force;
            return this;
        }

        /**
         * @param pin {@code --pin}
         * @return {@code this}
         */
        public Builder installPin(boolean pin) {
            this.installPin = pin;
            return this;
        }

        /**
         * @param unsafe {@code --dangerously-force-unsafe-install}
         * @return {@code this}
         */
        public Builder dangerouslyForceUnsafeInstall(boolean unsafe) {
            this.dangerouslyForceUnsafeInstall = unsafe;
            return this;
        }

        /**
         * @param marketplace {@code --marketplace}
         * @return {@code this}
         */
        public Builder marketplace(String marketplace) {
            this.marketplace = marketplace;
            return this;
        }

        /**
         * @param link install：{@code -l}
         * @return {@code this}
         */
        public Builder installLink(boolean link) {
            this.installLink = link;
            return this;
        }

        /**
         * @param id inspect：插件 ID
         * @return {@code this}
         */
        public Builder inspect(String id) {
            this.mode = Mode.INSPECT;
            this.inspectId = id;
            this.inspectAll = false;
            return this;
        }

        /**
         * @param all inspect：{@code --all}
         * @return {@code this}
         */
        public Builder inspectAll(boolean all) {
            this.inspectAll = all;
            if (all) {
                this.mode = Mode.INSPECT;
                this.inspectId = null;
            }
            return this;
        }

        /**
         * @param json inspect：{@code --json}
         * @return {@code this}
         */
        public Builder inspectJson(boolean json) {
            this.inspectJson = json;
            return this;
        }

        /**
         * @param id info：插件 ID
         * @return {@code this}
         */
        public Builder info(String id) {
            this.mode = Mode.INFO;
            this.inspectId = id;
            return this;
        }

        /**
         * @param id enable：插件 ID
         * @return {@code this}
         */
        public Builder enable(String id) {
            this.mode = Mode.ENABLE;
            this.pluginId = id;
            return this;
        }

        /**
         * @param id disable：插件 ID
         * @return {@code this}
         */
        public Builder disable(String id) {
            this.mode = Mode.DISABLE;
            this.pluginId = id;
            return this;
        }

        /**
         * @param id uninstall：插件 ID
         * @return {@code this}
         */
        public Builder uninstall(String id) {
            this.mode = Mode.UNINSTALL;
            this.pluginId = id;
            return this;
        }

        /**
         * @param dryRun {@code --dry-run}
         * @return {@code this}
         */
        public Builder uninstallDryRun(boolean dryRun) {
            this.uninstallDryRun = dryRun;
            return this;
        }

        /**
         * @param keep {@code --keep-files}
         * @return {@code this}
         */
        public Builder uninstallKeepFiles(boolean keep) {
            this.uninstallKeepFiles = keep;
            return this;
        }

        /**
         * @return {@code this}（{@code plugins doctor}）
         */
        public Builder doctor() {
            this.mode = Mode.DOCTOR;
            return this;
        }

        /**
         * @param idOrSpec update：插件 ID 或 spec
         * @return {@code this}
         */
        public Builder update(String idOrSpec) {
            this.mode = Mode.UPDATE;
            this.updateAll = false;
            this.pluginId = idOrSpec;
            return this;
        }

        /**
         * @param all update：{@code --all}
         * @return {@code this}
         */
        public Builder updateAll(boolean all) {
            this.mode = Mode.UPDATE;
            this.updateAll = all;
            this.pluginId = null;
            return this;
        }

        /**
         * @param dryRun update：{@code --dry-run}
         * @return {@code this}
         */
        public Builder updateDryRun(boolean dryRun) {
            this.updateDryRun = dryRun;
            return this;
        }

        /**
         * @param yes update：{@code --yes}
         * @return {@code this}
         */
        public Builder yes(boolean yes) {
            this.yes = yes;
            return this;
        }

        /**
         * @param source marketplace list：来源
         * @return {@code this}
         */
        public Builder marketplaceList(String source) {
            this.mode = Mode.MARKETPLACE_LIST;
            this.marketplaceSource = source;
            return this;
        }

        /**
         * @param json marketplace：{@code --json}
         * @return {@code this}
         */
        public Builder marketplaceJson(boolean json) {
            this.marketplaceJson = json;
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
         * @return 不可变 {@link PluginsOptions}
         */
        public PluginsOptions build() {
            return new PluginsOptions(this);
        }
    }
}
