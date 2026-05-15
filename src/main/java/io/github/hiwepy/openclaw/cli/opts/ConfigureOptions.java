package io.github.hiwepy.openclaw.cli.opts;

import io.github.hiwepy.openclaw.cli.args.CliSubArgs;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * {@code openclaw configure}：交互式向导，用于录入凭据、设备与 agent 默认值；可重复 {@code --section} 只跑部分步骤。
 * <p>与裸 {@code openclaw config}（无子命令）打开同一向导；非交互改键请用 {@code openclaw config get|set|unset}。
 * Model 段包含 {@code agents.defaults.models} 允许多选；从某 provider 认证入口进入时会优先筛该 provider 的模型目录。</p>
 *
 * @see <a href="https://docs.openclaw.ai/cli/configure">configure CLI</a>
 */
public final class ConfigureOptions implements CliSubArgs {

    /**
     * 可重复的 {@code --section} 取值，用于限制向导只跑指定段落（workspace、model、web、gateway、daemon、channels、plugins、skills、health）。
     */
    private final List<String> sections;

    /**
     * @param b 构建器快照
     */
    private ConfigureOptions(Builder b) {
        this.sections = List.copyOf(b.sections);
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
        for (String s : sections) {
            if (s != null && !s.isEmpty()) {
                out.add("--section");
                out.add(s);
            }
        }
        return Collections.unmodifiableList(out);
    }

    /**
     * {@link ConfigureOptions} 构建器。
     */
    public static final class Builder {

        /** 累积的 section 名称。 */
        private final List<String> sections = new ArrayList<>();

        /**
         * 追加 {@code --section}（可多次调用；文档允许 workspace、model、web、gateway、daemon、channels、plugins、skills、health）。
         */
        public Builder section(String section) {
            if (section != null && !section.isEmpty()) {
                sections.add(section);
            }
            return this;
        }

        /**
         * @return 不可变 {@link ConfigureOptions}
         */
        public ConfigureOptions build() {
            return new ConfigureOptions(this);
        }
    }
}
