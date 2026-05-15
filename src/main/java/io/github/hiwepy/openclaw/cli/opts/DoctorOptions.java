package io.github.hiwepy.openclaw.cli.opts;

import io.github.hiwepy.openclaw.cli.args.CliSubArgs;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * {@code openclaw doctor}：针对 Gateway 与渠道的体检与快速修复。
 * <p>{@code --repair} 与 {@code --fix} 等价；{@code --fix} 会备份 {@code ~/.openclaw/openclaw.json.bak} 并剔除未知配置键。
 * 交互式提示仅在 TTY 且未指定 {@code --non-interactive} 时运行。</p>
 *
 * @see <a href="https://docs.openclaw.ai/cli/doctor">doctor CLI</a>
 */
public final class DoctorOptions implements CliSubArgs {

    /**
     * {@code --no-workspace-suggestions}：关闭工作区 memory/search 相关建议。
     */
    private final boolean noWorkspaceSuggestions;
    /**
     * {@code --yes}：不再逐项确认，直接接受默认建议。
     */
    private final boolean yes;
    /**
     * {@code --repair}：应用推荐修复（无需再确认）；CLI 输出层使用 {@code --repair}（{@code --fix} 为文档别名）。
     */
    private final boolean repair;
    /**
     * {@code --force}：更激进的修复，必要时覆盖自定义服务配置等。
     */
    private final boolean force;
    /**
     * {@code --non-interactive}：无提示运行，仅执行文档所称「安全迁移」类操作。
     */
    private final boolean nonInteractive;
    /**
     * {@code --generate-gateway-token}：生成并写入网关 token 配置。
     */
    private final boolean generateGatewayToken;
    /**
     * {@code --deep}：扫描系统级服务，查找额外的 Gateway 安装实例。
     */
    private final boolean deep;

    /**
     * @param b 构建器快照
     */
    private DoctorOptions(Builder b) {
        this.noWorkspaceSuggestions = b.noWorkspaceSuggestions;
        this.yes = b.yes;
        this.repair = b.repair;
        this.force = b.force;
        this.nonInteractive = b.nonInteractive;
        this.generateGatewayToken = b.generateGatewayToken;
        this.deep = b.deep;
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
        if (noWorkspaceSuggestions) {
            out.add("--no-workspace-suggestions");
        }
        if (yes) {
            out.add("--yes");
        }
        if (repair) {
            out.add("--repair");
        }
        if (force) {
            out.add("--force");
        }
        if (nonInteractive) {
            out.add("--non-interactive");
        }
        if (generateGatewayToken) {
            out.add("--generate-gateway-token");
        }
        if (deep) {
            out.add("--deep");
        }
        return Collections.unmodifiableList(out);
    }

    /**
     * {@link DoctorOptions} 构建器。
     */
    public static final class Builder {

        private boolean noWorkspaceSuggestions;
        private boolean yes;
        private boolean repair;
        private boolean force;
        private boolean nonInteractive;
        private boolean generateGatewayToken;
        private boolean deep;

        /**
         * @param noWorkspaceSuggestions {@code --no-workspace-suggestions}
         * @return {@code this}
         */
        public Builder noWorkspaceSuggestions(boolean noWorkspaceSuggestions) {
            this.noWorkspaceSuggestions = noWorkspaceSuggestions;
            return this;
        }

        /**
         * @param yes {@code --yes}
         * @return {@code this}
         */
        public Builder yes(boolean yes) {
            this.yes = yes;
            return this;
        }

        /** {@code --repair}（{@code --fix} 为别名，此处用 repair 输出）。 */
        public Builder repair(boolean repair) {
            this.repair = repair;
            return this;
        }

        /**
         * @param force {@code --force}
         * @return {@code this}
         */
        public Builder force(boolean force) {
            this.force = force;
            return this;
        }

        /**
         * @param nonInteractive {@code --non-interactive}
         * @return {@code this}
         */
        public Builder nonInteractive(boolean nonInteractive) {
            this.nonInteractive = nonInteractive;
            return this;
        }

        /**
         * @param generateGatewayToken {@code --generate-gateway-token}
         * @return {@code this}
         */
        public Builder generateGatewayToken(boolean generateGatewayToken) {
            this.generateGatewayToken = generateGatewayToken;
            return this;
        }

        /**
         * @param deep {@code --deep}
         * @return {@code this}
         */
        public Builder deep(boolean deep) {
            this.deep = deep;
            return this;
        }

        /**
         * @return 不可变 {@link DoctorOptions}
         */
        public DoctorOptions build() {
            return new DoctorOptions(this);
        }
    }
}
