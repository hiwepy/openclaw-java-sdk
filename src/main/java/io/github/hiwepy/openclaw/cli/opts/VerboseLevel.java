package io.github.hiwepy.openclaw.cli.opts;

/**
 * {@code openclaw agent --verbose} 的合法取值：将会话级 verbose 开关持久化到该会话，与官方 agent CLI 一致。
 *
 * @see <a href="https://docs.openclaw.ai/cli/agent">agent CLI</a>
 */
public enum VerboseLevel {

    /**
     * {@code on}：为该会话打开 verbose（文档：persist verbose level for the session）。
     */
    ON("on"),
    /**
     * {@code off}：关闭该会话的 verbose 持久化。
     */
    OFF("off");

    /**
     * 传给 CLI 的 {@code --verbose} 参数字面量（{@code on} 或 {@code off}）。
     */
    private final String cliValue;

    /**
     * @param cliValue 非 null CLI 字符串
     */
    VerboseLevel(String cliValue) {
        this.cliValue = cliValue;
    }

    /**
     * 传给 CLI 的 {@code --verbose} 参数值。
     *
     * @return {@code on} 或 {@code off}
     */
    public String cliValue() {
        return cliValue;
    }
}
