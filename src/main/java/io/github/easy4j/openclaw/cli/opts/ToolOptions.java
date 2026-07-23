package io.github.easy4j.openclaw.cli.opts;

import io.github.easy4j.openclaw.cli.args.CliSubArgs;

import java.util.Collections;
import java.util.List;

/**
 * {@code openclaw tool}：保留命令根（reserved command root），无 CLI 选项。
 * <p>
 * 在 openclaw 源码（{@code src/cli/command-registration-policy.ts}）中，{@code tool} 列在
 * {@code RESERVED_NON_PLUGIN_COMMAND_ROOTS} 集合中，仅用于路由策略与插件命名空间占用，
 * 当前不存在任何 Commander {@code .option(...)} 定义。
 * </p>
 * <p>
 * 本类作为 CLI 适配层的占位：调用 {@link #empty()} 等价于裸执行 {@code openclaw tool}。
 * </p>
 *
 * @see <a href="https://docs.openclaw.ai/cli/tool">tool CLI</a>
 */
public final class ToolOptions implements CliSubArgs {

    /**
     * 无选项实例：对应裸 {@code openclaw tool}。
     *
     * @return 共享的空参数实例
     */
    public static ToolOptions empty() {
        return INSTANCE;
    }

    private static final ToolOptions INSTANCE = new ToolOptions();

    private ToolOptions() {
    }

    /**
     * {@inheritDoc}
     *
     * @return 永远返回空列表（该命令根无任何子命令 token 或选项）
     */
    @Override
    public List<String> toSubcommandArguments() {
        return Collections.emptyList();
    }
}
