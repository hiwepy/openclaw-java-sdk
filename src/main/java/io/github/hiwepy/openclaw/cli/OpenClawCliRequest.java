package io.github.hiwepy.openclaw.cli;

import io.github.hiwepy.openclaw.util.OpenClawLists;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 本地 CLI 调用描述：全局参数 + {@code openclaw} 之后的参数序列。
 * <p>
 * 全局参数顺序与文档一致：<code>[--dev] [--profile &lt;name&gt;] [--container &lt;name&gt;] [--no-color]</code>，再接子命令与 flag。
 * </p>
 */
@Getter
public final class OpenClawCliRequest {

    private final boolean dev;
    private final String profile;
    private final String container;
    private final boolean noColor;
    /** null 表示使用 {@link io.github.hiwepy.openclaw.OpenClawClientConfig#getLocalTimeoutSeconds()} */
    private final Integer timeoutSeconds;
    private final List<String> arguments;

    private OpenClawCliRequest(Builder b) {
        this.dev = b.dev;
        this.profile = b.profile;
        this.container = b.container;
        this.noColor = b.noColor;
        this.timeoutSeconds = b.timeoutSeconds;
        this.arguments = OpenClawLists.copyOf(b.arguments);
    }

    public static Builder builder() {
        return new Builder();
    }

    /**
     * 构建器。
     */
    public static final class Builder {

        private boolean dev;
        private String profile;
        private String container;
        private boolean noColor;
        private Integer timeoutSeconds;
        private final List<String> arguments = new ArrayList<>();

        public Builder dev(boolean dev) {
            this.dev = dev;
            return this;
        }

        public Builder profile(String profile) {
            this.profile = profile;
            return this;
        }

        public Builder container(String container) {
            this.container = container;
            return this;
        }

        public Builder noColor(boolean noColor) {
            this.noColor = noColor;
            return this;
        }

        /**
         * 进程执行超时（秒）；不设置则使用配置中的默认本地/CLI 超时。
         */
        public Builder timeoutSeconds(Integer timeoutSeconds) {
            this.timeoutSeconds = timeoutSeconds;
            return this;
        }

        /**
         * 设置完整参数序列（不含可执行文件名与全局参数），例如 {@code "gateway", "health"}。
         */
        public Builder arguments(String... args) {
            this.arguments.clear();
            if (args != null) {
                this.arguments.addAll(Arrays.asList(args));
            }
            return this;
        }

        public Builder arguments(List<String> args) {
            this.arguments.clear();
            if (args != null) {
                this.arguments.addAll(args);
            }
            return this;
        }

        public OpenClawCliRequest build() {
            return new OpenClawCliRequest(this);
        }
    }
}
