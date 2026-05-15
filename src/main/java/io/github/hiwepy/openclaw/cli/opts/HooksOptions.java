package io.github.hiwepy.openclaw.cli.opts;

import io.github.hiwepy.openclaw.cli.args.CliSubArgs;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * {@code openclaw hooks}：列举、检查、启用或禁用工作区与捆绑等来源的 agent hooks（事件驱动自动化）。
 * <p>插件托管的 hook 只能改插件开关；安装 hook 包推荐 {@code openclaw plugins install}，{@code hooks install} 为转发别名并可能提示弃用。</p>
 *
 * @see <a href="https://docs.openclaw.ai/cli/hooks">hooks CLI</a>
 */
public final class HooksOptions implements CliSubArgs {

    /**
     * hooks 子命令：默认与 {@code list} 等价，另有 info、健康检查、启用禁用与遗留 install。
     */
    public enum Mode {
        /** {@code hooks list}：发现 workspace、managed、extra、bundled 目录下的全部 hook。 */
        LIST,
        /** {@code hooks info}：展示单个 hook 的元数据、事件与依赖。 */
        INFO,
        /** {@code hooks check}：汇总 eligible 与缺失依赖统计。 */
        CHECK,
        /** {@code hooks enable}：在配置中打开 {@code hooks.internal.entries.*.enabled}。 */
        ENABLE,
        /** {@code hooks disable}：在配置中关闭指定 hook。 */
        DISABLE,
        /** {@code hooks install}：兼容别名，转发到 plugins 安装流。 */
        INSTALL
    }

    /** list / info / check / enable / disable / install 之一。 */
    private final Mode mode;
    /**
     * list：{@code --eligible} 只显示依赖已满足的 hook。
     */
    private final boolean listEligible;
    /**
     * list：{@code --json} 结构化列表。
     */
    private final boolean listJson;
    /**
     * list：{@code --verbose} 展示未满足依赖等诊断信息。
     */
    private final boolean listVerbose;
    /**
     * info / enable / disable：hook 名称或 key 位置参数。
     */
    private final String hookName;
    /**
     * info：{@code --json}。
     */
    private final boolean infoJson;
    /**
     * check：{@code --json}。
     */
    private final boolean checkJson;
    /**
     * install：包路径、npm 名或归档位置参数（实际由 plugins 子系统处理）。
     */
    private final String installSpec;
    /**
     * install：{@code --link} 链接本地目录到 {@code hooks.internal.load.extraDirs} 而非复制。
     */
    private final boolean installLink;
    /**
     * install：{@code --pin} npm 安装时记录精确版本到 {@code hooks.internal.installs}。
     */
    private final boolean installPin;
    /**
     * 其它 argv。
     */
    private final List<String> extra;

    /**
     * @param b 构建器快照
     */
    private HooksOptions(Builder b) {
        this.mode = b.mode;
        this.listEligible = b.listEligible;
        this.listJson = b.listJson;
        this.listVerbose = b.listVerbose;
        this.hookName = b.hookName;
        this.infoJson = b.infoJson;
        this.checkJson = b.checkJson;
        this.installSpec = b.installSpec;
        this.installLink = b.installLink;
        this.installPin = b.installPin;
        this.extra = b.extra == null ? List.of() : List.copyOf(b.extra);
    }

    /**
     * @return 新 {@link Builder}
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * 空参数：对应 CLI 默认列出 hooks（与 {@code hooks list} 行为一致）。
     */
    public static HooksOptions defaultList() {
        return builder().list().build();
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
                OpenClawCliArgv.addFlag(out, "--eligible", listEligible);
                OpenClawCliArgv.addFlag(out, "--json", listJson);
                OpenClawCliArgv.addFlag(out, "--verbose", listVerbose);
                break;
            case INFO:
                out.add("info");
                if (hookName != null && !hookName.isBlank()) {
                    out.add(hookName.trim());
                }
                OpenClawCliArgv.addFlag(out, "--json", infoJson);
                break;
            case CHECK:
                out.add("check");
                OpenClawCliArgv.addFlag(out, "--json", checkJson);
                break;
            case ENABLE:
                out.add("enable");
                if (hookName != null && !hookName.isBlank()) {
                    out.add(hookName.trim());
                }
                break;
            case DISABLE:
                out.add("disable");
                if (hookName != null && !hookName.isBlank()) {
                    out.add(hookName.trim());
                }
                break;
            case INSTALL:
                out.add("install");
                if (installSpec != null && !installSpec.isBlank()) {
                    out.add(installSpec.trim());
                }
                OpenClawCliArgv.addFlag(out, "--link", installLink);
                OpenClawCliArgv.addFlag(out, "--pin", installPin);
                break;
            default:
                break;
        }
        OpenClawCliArgv.addExtra(out, extra);
        return Collections.unmodifiableList(out);
    }

    /**
     * {@link HooksOptions} 构建器。
     */
    public static final class Builder {
        private Mode mode = Mode.LIST;
        private boolean listEligible;
        private boolean listJson;
        private boolean listVerbose;
        private String hookName;
        private boolean infoJson;
        private boolean checkJson;
        private String installSpec;
        private boolean installLink;
        private boolean installPin;
        private List<String> extra = new ArrayList<>();

        /**
         * @return {@code this}（{@code hooks list}）
         */
        public Builder list() {
            this.mode = Mode.LIST;
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
         * @param name hook 名称
         * @return {@code this}
         */
        public Builder info(String name) {
            this.mode = Mode.INFO;
            this.hookName = name;
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
         * @return {@code this}（{@code hooks check}）
         */
        public Builder check() {
            this.mode = Mode.CHECK;
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
         * @param name hook 名称
         * @return {@code this}
         */
        public Builder enable(String name) {
            this.mode = Mode.ENABLE;
            this.hookName = name;
            return this;
        }

        /**
         * @param name hook 名称
         * @return {@code this}
         */
        public Builder disable(String name) {
            this.mode = Mode.DISABLE;
            this.hookName = name;
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
         * @param link {@code --link}
         * @return {@code this}
         */
        public Builder installLink(boolean link) {
            this.installLink = link;
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
         * @return 不可变 {@link HooksOptions}
         */
        public HooksOptions build() {
            return new HooksOptions(this);
        }
    }
}
