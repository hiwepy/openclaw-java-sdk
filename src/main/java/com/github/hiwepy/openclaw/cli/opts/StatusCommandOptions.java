package com.github.hiwepy.openclaw.cli.opts;

import com.github.hiwepy.openclaw.cli.args.CliSubArgs;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 顶层 {@code openclaw status}：渠道与会话诊断（非 {@code gateway status}）。
 * <p>文档说明：{@code --deep} 会对 WhatsApp Web、Telegram、Discord、Slack、Signal 等做实时探测；
 * {@code --usage} 将各提供商用量窗口规范为「剩余 X%」形式输出；{@code --all} 会扩展 Secrets 概览与诊断摘要等（见官方 Notes）。</p>
 *
 * @see <a href="https://docs.openclaw.ai/cli/status">status CLI</a>
 */
public final class StatusCommandOptions implements CliSubArgs {

    /**
     * {@code --all}：扩展输出（含 Secrets 概览行、secret 诊断摘要等，文档只读路径仍尽量解析 SecretRef）。
     */
    private final boolean all;
    /**
     * {@code --deep}：对各支持渠道执行实时在线探测（文档列举的 IM 渠道集合）。
     */
    private final boolean deep;
    /**
     * {@code --usage}：打印规范化后的提供商用量窗口（文档：显示为 {@code X% left} 等格式）。
     */
    private final boolean usage;
    /**
     * {@code --json}：机器可读 JSON（文档 Notes 中与 {@code status --json} 等只读形态一致）。
     */
    private final boolean json;

    /**
     * @param b 构建器快照
     */
    private StatusCommandOptions(Builder b) {
        this.all = b.all;
        this.deep = b.deep;
        this.usage = b.usage;
        this.json = b.json;
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
        if (all) {
            out.add("--all");
        }
        if (deep) {
            out.add("--deep");
        }
        if (usage) {
            out.add("--usage");
        }
        if (json) {
            out.add("--json");
        }
        return Collections.unmodifiableList(out);
    }

    /**
     * {@link StatusCommandOptions} 构建器。
     */
    public static final class Builder {

        private boolean all;
        private boolean deep;
        private boolean usage;
        private boolean json;

        /**
         * @param all {@code --all}
         * @return {@code this}
         */
        public Builder all(boolean all) {
            this.all = all;
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
         * @param usage {@code --usage}
         * @return {@code this}
         */
        public Builder usage(boolean usage) {
            this.usage = usage;
            return this;
        }

        /**
         * @param json {@code --json}
         * @return {@code this}
         */
        public Builder json(boolean json) {
            this.json = json;
            return this;
        }

        /**
         * @return 不可变 {@link StatusCommandOptions}
         */
        public StatusCommandOptions build() {
            return new StatusCommandOptions(this);
        }
    }
}
