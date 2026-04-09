package com.github.hiwepy.openclaw.cli.opts;

import com.github.hiwepy.openclaw.cli.args.CliSubArgs;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * {@code openclaw onboard}：本地或远程 Gateway 的交互式 / 非交互式引导（向导）。
 * <p>常用自动化 flag 由 {@link Builder} 方法追加；其余选项请用 {@link Builder#extra(String...)} 按官方文档顺序透传。
 * {@code --json} 不隐含非交互；脚本须同时传 {@code --non-interactive}。Gateway 令牌相关互斥与 SecretRef 规则见 onboard 文档。</p>
 *
 * @see <a href="https://docs.openclaw.ai/cli/onboard">onboard CLI</a>
 */
public final class OnboardOptions implements CliSubArgs {

    /**
     * 已按 shell 顺序展开的 onboard 子命令 argv 片段不可变列表；{@link #toSubcommandArguments()} 直接返回该视图。
     */
    private final List<String> segments;

    /**
     * @param segments 非 null；内部持有引用，调用方应传入不可变或拷贝后的列表
     */
    private OnboardOptions(List<String> segments) {
        this.segments = segments;
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
        return segments;
    }

    /**
     * {@link OnboardOptions} 构建器：逐项追加与官方文档一致的 flag。
     */
    public static final class Builder {

        /** 可变参数缓冲，构建时拷贝为不可变列表。 */
        private final List<String> s = new ArrayList<>();

        /**
         * @param flow {@code --flow}
         * @return {@code this}
         */
        public Builder flow(String flow) {
            if (flow != null && !flow.isEmpty()) {
                s.add("--flow");
                s.add(flow);
            }
            return this;
        }

        /**
         * @param mode {@code --mode}
         * @return {@code this}
         */
        public Builder mode(String mode) {
            if (mode != null && !mode.isEmpty()) {
                s.add("--mode");
                s.add(mode);
            }
            return this;
        }

        /**
         * @param remoteUrl {@code --remote-url}
         * @return {@code this}
         */
        public Builder remoteUrl(String remoteUrl) {
            if (remoteUrl != null && !remoteUrl.isEmpty()) {
                s.add("--remote-url");
                s.add(remoteUrl);
            }
            return this;
        }

        /**
         * @param nonInteractive 为 true 时追加 {@code --non-interactive}
         * @return {@code this}
         */
        public Builder nonInteractive(boolean nonInteractive) {
            if (nonInteractive) {
                s.add("--non-interactive");
            }
            return this;
        }

        /**
         * @param json 为 true 时追加 {@code --json}
         * @return {@code this}
         */
        public Builder json(boolean json) {
            if (json) {
                s.add("--json");
            }
            return this;
        }

        /**
         * @param authChoice {@code --auth-choice}
         * @return {@code this}
         */
        public Builder authChoice(String authChoice) {
            if (authChoice != null && !authChoice.isEmpty()) {
                s.add("--auth-choice");
                s.add(authChoice);
            }
            return this;
        }

        /**
         * @param customBaseUrl {@code --custom-base-url}
         * @return {@code this}
         */
        public Builder customBaseUrl(String customBaseUrl) {
            if (customBaseUrl != null && !customBaseUrl.isEmpty()) {
                s.add("--custom-base-url");
                s.add(customBaseUrl);
            }
            return this;
        }

        /**
         * @param customModelId {@code --custom-model-id}
         * @return {@code this}
         */
        public Builder customModelId(String customModelId) {
            if (customModelId != null && !customModelId.isEmpty()) {
                s.add("--custom-model-id");
                s.add(customModelId);
            }
            return this;
        }

        /**
         * @param secretInputMode {@code --secret-input-mode}
         * @return {@code this}
         */
        public Builder secretInputMode(String secretInputMode) {
            if (secretInputMode != null && !secretInputMode.isEmpty()) {
                s.add("--secret-input-mode");
                s.add(secretInputMode);
            }
            return this;
        }

        /**
         * @param acceptRisk 为 true 时追加 {@code --accept-risk}
         * @return {@code this}
         */
        public Builder acceptRisk(boolean acceptRisk) {
            if (acceptRisk) {
                s.add("--accept-risk");
            }
            return this;
        }

        /**
         * @param gatewayAuth {@code --gateway-auth}
         * @return {@code this}
         */
        public Builder gatewayAuth(String gatewayAuth) {
            if (gatewayAuth != null && !gatewayAuth.isEmpty()) {
                s.add("--gateway-auth");
                s.add(gatewayAuth);
            }
            return this;
        }

        /**
         * @param gatewayToken {@code --gateway-token}
         * @return {@code this}
         */
        public Builder gatewayToken(String gatewayToken) {
            if (gatewayToken != null && !gatewayToken.isEmpty()) {
                s.add("--gateway-token");
                s.add(gatewayToken);
            }
            return this;
        }

        /**
         * @param envVar {@code --gateway-token-ref-env} 环境变量名
         * @return {@code this}
         */
        public Builder gatewayTokenRefEnv(String envVar) {
            if (envVar != null && !envVar.isEmpty()) {
                s.add("--gateway-token-ref-env");
                s.add(envVar);
            }
            return this;
        }

        /**
         * @param installDaemon 为 true 时追加 {@code --install-daemon}
         * @return {@code this}
         */
        public Builder installDaemon(boolean installDaemon) {
            if (installDaemon) {
                s.add("--install-daemon");
            }
            return this;
        }

        /**
         * @param skipHealth 为 true 时追加 {@code --skip-health}
         * @return {@code this}
         */
        public Builder skipHealth(boolean skipHealth) {
            if (skipHealth) {
                s.add("--skip-health");
            }
            return this;
        }

        /**
         * @param allowUnconfigured 为 true 时追加 {@code --allow-unconfigured}
         * @return {@code this}
         */
        public Builder allowUnconfigured(boolean allowUnconfigured) {
            if (allowUnconfigured) {
                s.add("--allow-unconfigured");
            }
            return this;
        }

        /**
         * 文档中其余 flag 或子参数，顺序与 shell 一致。
         *
         * @param tokens 可为 null（忽略）
         * @return {@code this}
         */
        public Builder extra(String... tokens) {
            if (tokens != null) {
                Collections.addAll(s, tokens);
            }
            return this;
        }

        /**
         * @return 不可变 {@link OnboardOptions}
         */
        public OnboardOptions build() {
            return new OnboardOptions(List.copyOf(s));
        }
    }
}
