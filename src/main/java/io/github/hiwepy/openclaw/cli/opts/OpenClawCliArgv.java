package io.github.hiwepy.openclaw.cli.opts;

import io.github.hiwepy.openclaw.util.OpenClawStrings;
import java.util.List;

/**
 * 将 CLI flag 与参数片段追加到 argv 列表的辅助工具类（package-private）。
 * <p>供各 {@link io.github.hiwepy.openclaw.cli.args.CliSubArgs} 实现类在
 * {@link io.github.hiwepy.openclaw.cli.args.CliSubArgs#toSubcommandArguments()} 中复用，避免重复拼接逻辑。</p>
 */
final class OpenClawCliArgv {

    private OpenClawCliArgv() {
    }

    /**
     * 当 {@code value} 非 null 且非空白时，追加 {@code flag} 与 {@code value} 各一项。
     *
     * @param out   目标参数列表
     * @param flag  形如 {@code "--url"} 的选项名
     * @param value 选项值
     */
    static void addIfPresent(List<String> out, String flag, String value) {
        if (value != null && OpenClawStrings.isNotBlank(value)) {
            out.add(flag);
            out.add(value);
        }
    }

    /**
     * 当 {@code value} 为正数时，追加 {@code flag} 与数值字符串。
     *
     * @param out   目标参数列表
     * @param flag  选项名
     * @param value 整型值（通常表示毫秒等）
     */
    static void addIfPositive(List<String> out, String flag, int value) {
        if (value > 0) {
            out.add(flag);
            out.add(Integer.toString(value));
        }
    }

    /**
     * 当 {@code value} 非 null 时，追加 {@code flag} 与 {@link Integer} 的十进制字符串形式。
     *
     * @param out   目标参数列表
     * @param flag  选项名
     * @param value 可空整型
     */
    static void addIfNotNull(List<String> out, String flag, Integer value) {
        if (value != null) {
            out.add(flag);
            out.add(Integer.toString(value));
        }
    }

    /**
     * 当 {@code value} 非 null 时，追加 {@code flag} 与 {@link Double} 的字符串形式。
     *
     * @param out   目标参数列表
     * @param flag  选项名
     * @param value 可空双精度值（如地理坐标）
     */
    static void addIfNotNull(List<String> out, String flag, Double value) {
        if (value != null) {
            out.add(flag);
            out.add(Double.toString(value));
        }
    }

    /**
     * 当 {@code enabled} 为 true 时，仅追加 {@code flag}（布尔开关型选项，无独立值 token）。
     *
     * @param out     目标参数列表
     * @param flag    选项名
     * @param enabled 是否输出该 flag
     */
    static void addFlag(List<String> out, String flag, boolean enabled) {
        if (enabled) {
            out.add(flag);
        }
    }

    /**
     * 对列表中每个非空元素重复追加 {@code flag} 与元素值（可重复选项，如多个 {@code --scope}）。
     *
     * @param out    目标参数列表
     * @param flag   可重复选项名
     * @param values 值列表，可为 null（忽略）
     */
    static void addRepeatable(List<String> out, String flag, List<String> values) {
        if (values == null) {
            return;
        }
        for (String v : values) {
            if (v != null && OpenClawStrings.isNotBlank(v)) {
                out.add(flag);
                out.add(v.trim());
            }
        }
    }

    /**
     * 将「逃生舱」额外 token 全部追加到末尾（{@link io.github.hiwepy.openclaw.cli.opts} 各 Builder 的 {@code extra}）。
     *
     * @param out   目标参数列表
     * @param extra 额外 token，可为 null（忽略）
     */
    static void addExtra(List<String> out, List<String> extra) {
        if (extra == null) {
            return;
        }
        out.addAll(extra);
    }
}
