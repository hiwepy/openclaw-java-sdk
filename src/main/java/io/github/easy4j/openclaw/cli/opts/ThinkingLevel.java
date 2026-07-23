package io.github.easy4j.openclaw.cli.opts;

/**
 * {@code openclaw agent --thinking} 的合法取值：调节 agent「思考」深度（推理用量档位），与官方 agent CLI 列出的枚举一致。
 *
 * @see <a href="https://docs.openclaw.ai/cli/agent">agent CLI</a>
 */
public enum ThinkingLevel {

    /** 关闭该维度的思考增强（文档档位 {@code off}）。 */
    OFF("off"),
    /** 最小思考档位 {@code minimal}。 */
    MINIMAL("minimal"),
    /** 低档 {@code low}。 */
    LOW("low"),
    /** 中档 {@code medium}。 */
    MEDIUM("medium"),
    /** 高档 {@code high}。 */
    HIGH("high"),
    /** 最高档 {@code xhigh}。 */
    XHIGH("xhigh");

    /**
     * 传给 CLI 的 {@code --thinking} 参数字面量（小写，与 openclaw 文档一致）。
     */
    private final String cliValue;

    /**
     * @param cliValue 非 null，与 openclaw 文档一致
     */
    ThinkingLevel(String cliValue) {
        this.cliValue = cliValue;
    }

    /**
     * 传给 CLI 的 {@code --thinking} 参数值（小写标识）。
     *
     * @return 与 openclaw 一致的 token
     */
    public String cliValue() {
        return cliValue;
    }
}
