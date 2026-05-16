package io.github.hiwepy.openclaw.cli.opts;

import io.github.hiwepy.openclaw.util.OpenClawLists;
import io.github.hiwepy.openclaw.cli.args.CliSubArgs;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * {@code openclaw docs [query...]}：在联机文档索引中检索；无查询词时进入实时文档搜索入口。
 * <p>多词查询会作为一次搜索请求原样提交（与 shell 传参顺序一致）。</p>
 *
 * @see <a href="https://docs.openclaw.ai/cli/docs">docs CLI</a>
 */
public final class DocsCommandOptions implements CliSubArgs {

    /**
     * 传给 CLI 的位置参数序列，即文档所述 {@code [query...]} 搜索词；空列表表示不带查询词打开搜索入口。
     */
    private final List<String> queryWords;

    /**
     * @param b 构建器快照
     */
    private DocsCommandOptions(Builder b) {
        this.queryWords = OpenClawLists.copyOf(b.queryWords);
    }

    /**
     * @return 新 {@link Builder}
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * 无查询词：与 {@code openclaw docs} 不带参数一致，打开联机文档搜索入口。
     */
    public static DocsCommandOptions empty() {
        return builder().build();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> toSubcommandArguments() {
        return Collections.unmodifiableList(new ArrayList<>(queryWords));
    }

    /**
     * {@link DocsCommandOptions} 构建器。
     */
    public static final class Builder {

        /** 查询词列表。 */
        private final List<String> queryWords = new ArrayList<>();

        /** 追加搜索词（多词可多次调用或空格分词由调用方决定）。 */
        public Builder queryWord(String word) {
            if (word != null && !word.isEmpty()) {
                queryWords.add(word);
            }
            return this;
        }

        /** 一次传入多个 token 作为查询片段（与 shell 传参一致）。 */
        public Builder queryWords(String... words) {
            if (words != null) {
                Collections.addAll(queryWords, words);
            }
            return this;
        }

        /**
         * @return 不可变 {@link DocsCommandOptions}
         */
        public DocsCommandOptions build() {
            return new DocsCommandOptions(this);
        }
    }
}
