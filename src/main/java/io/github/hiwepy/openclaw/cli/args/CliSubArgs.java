package io.github.hiwepy.openclaw.cli.args;

import java.util.Collections;
import java.util.List;

/**
 * 表示官方 CLI 中「顶层命令之后」的参数片段（不含 {@code openclaw} 与顶层命令名本身）。
 * <p>
 * 典型实现见 {@code io.github.hiwepy.openclaw.cli.opts} 包（如 {@link io.github.hiwepy.openclaw.cli.opts.SetupOptions}、{@link io.github.hiwepy.openclaw.cli.opts.AgentOptions}）。
 * </p>
 */
@FunctionalInterface
public interface CliSubArgs {

    /**
     * @return 传给可执行文件的参数列表中、紧跟在顶层命令名之后的 token 序列（顺序与 shell 一致）
     */
    List<String> toSubcommandArguments();

    /**
     * @return 无子命令参数（顶层命令后不再追加 token）
     */
    static CliSubArgs empty() {
        return Collections::emptyList;
    }
}
