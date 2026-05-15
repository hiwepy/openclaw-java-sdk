package io.github.hiwepy.openclaw.cli.opts;

import io.github.hiwepy.openclaw.cli.args.CliSubArgs;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * {@code openclaw config}：对 {@code openclaw.json} 做非交互式读写、校验与 schema 输出；无子命令时进入与 {@code openclaw configure} 相同的向导。
 * <p>根级可重复 {@code --section} 用于向导步骤过滤，取值限于文档列出的 {@code workspace|model|web|gateway|daemon|channels|plugins|skills|health}。
 * {@link #tail(String...)} 承载 {@code get/set/unset/validate/schema/file} 等子命令及其路径、JSON 值与各类 builder flag。</p>
 *
 * @see <a href="https://docs.openclaw.ai/cli/config">config CLI</a>
 */
public final class ConfigOptions implements CliSubArgs {

    /**
     * 根级可重复 {@code --section}：无子命令运行向导时限制出现的配置分区（可多次指定）。
     */
    private final List<String> sections;
    /**
     * 子命令及后续 argv：如 {@code "get","agents.defaults.workspace"}、{@code "validate","--json"}、{@code "set", path, value, ...flags} 等，顺序须与 shell 一致。
     */
    private final List<String> tail;

    /**
     * @param b 构建器快照
     */
    private ConfigOptions(Builder b) {
        this.sections = List.copyOf(b.sections);
        this.tail = List.copyOf(b.tail);
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
        out.addAll(tail);
        return Collections.unmodifiableList(out);
    }

    /**
     * {@link ConfigOptions} 构建器。
     */
    public static final class Builder {

        /** 累积的 {@code --section} 值。 */
        private final List<String> sections = new ArrayList<>();
        /** 子命令与尾部参数。 */
        private final List<String> tail = new ArrayList<>();

        /** 无子命令时向导过滤；可重复。 */
        public Builder section(String section) {
            if (section != null && !section.isEmpty()) {
                sections.add(section);
            }
            return this;
        }

        /**
         * 子命令及后续参数（如 {@code "get", "browser.executablePath"}、{@code "validate", "--json"}）。
         */
        public Builder tail(String... tokens) {
            if (tokens != null) {
                for (String t : tokens) {
                    if (t != null) {
                        tail.add(t);
                    }
                }
            }
            return this;
        }

        /**
         * @return 不可变 {@link ConfigOptions}
         */
        public ConfigOptions build() {
            return new ConfigOptions(this);
        }
    }
}
