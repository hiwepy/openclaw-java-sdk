package io.github.easy4j.openclaw.cli.opts;

import io.github.easy4j.openclaw.cli.args.CliSubArgs;

import java.util.Collections;
import java.util.List;

/**
 * {@code openclaw tools}：根帮助别名（root help alias），无顶层 CLI 选项。
 * <p>
 * 在 openclaw 源码（{@code src/cli/run-main-policy.ts}）中，{@code tools} 列在
 * {@code ROOT_HELP_ALIASES} 集合中，{@code openclaw tools --help} 会触发根帮助快速路径。
 * 顶层不存在 Commander {@code .option(...)} 定义。
 * </p>
 * <p>
 * 注意：带选项的「tools」语义位于 {@code mcp tools} 子命令（更新 MCP 服务器工具 include/exclude 过滤器），
 * 应使用 {@link McpOptions} 封装；本类仅作为顶层 {@code tools} 占位。
 * </p>
 *
 * @see McpOptions
 * @see <a href="https://docs.openclaw.ai/cli/tools">tools CLI</a>
 */
public final class ToolsOptions implements CliSubArgs {

    /**
     * 无选项实例：对应裸 {@code openclaw tools}（触发根帮助）。
     *
     * @return 共享的空参数实例
     */
    public static ToolsOptions empty() {
        return INSTANCE;
    }

    private static final ToolsOptions INSTANCE = new ToolsOptions();

    private ToolsOptions() {
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
