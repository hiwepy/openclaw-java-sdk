package io.github.hiwepy.openclaw.cli.opts;

import io.github.hiwepy.openclaw.util.OpenClawLists;
import io.github.hiwepy.openclaw.OpenClawClientConfig;
import io.github.hiwepy.openclaw.cli.OpenClawCliExecutor;
import io.github.hiwepy.openclaw.cli.OpenClawCliRequest;
import org.apache.commons.exec.CommandLine;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * {@link AgentOptions} 与官方 {@code openclaw agent} 文档的 argv 映射及构建校验测试。
 */
class AgentOptionsTest {

    @Test
    void toSubcommandArguments_matchesDocExample_agentAndMessage() {
        AgentOptions opts = AgentOptions.builder()
                .agent("ops")
                .message("Summarize logs")
                .build();
        assertEquals(
                OpenClawLists.of("--message", "Summarize logs", "--agent", "ops"),
                opts.toSubcommandArguments());
    }

    @Test
    void toSubcommandArguments_fullFlags_viaEnumsAndTimeout() {
        AgentOptions opts = AgentOptions.builder()
                .to("+15555550123")
                .message("Trace logs")
                .thinking(ThinkingLevel.MEDIUM)
                .verbose(VerboseLevel.ON)
                .timeoutSeconds(600)
                .json(true)
                .deliver(true)
                .local(false)
                .build();
        assertEquals(
                OpenClawLists.of("--message", "Trace logs",
                        "--to", "+15555550123",
                        "--thinking", "medium",
                        "--verbose", "on",
                        "--deliver",
                        "--timeout", "600",
                        "--json"),
                opts.toSubcommandArguments());
    }

    @Test
    void toSubcommandArguments_thinkingVerboseStringEscapeHatch() {
        AgentOptions opts = AgentOptions.builder()
                .sessionId("1234")
                .message("x")
                .thinking("custom-level")
                .verbose("maybe")
                .build();
        assertEquals(
                OpenClawLists.of("--message", "x", "--session-id", "1234", "--thinking", "custom-level", "--verbose", "maybe"),
                opts.toSubcommandArguments());
    }

    @Test
    void build_requiresMessage() {
        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> AgentOptions.builder().agent("a").message("").build());
        assertEquals("agent: --message is required and must be non-blank", ex.getMessage());
    }

    @Test
    void build_requiresSessionSelector() {
        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> AgentOptions.builder().message("hi").build());
        assertEquals(
                "agent: at least one of --to, --session-id, or --agent is required (non-blank)",
                ex.getMessage());
    }

    @Test
    void openClawCli_fullCommandLine_agentSubcommand() {
        OpenClawClientConfig cfg = new OpenClawClientConfig();
        cfg.setLocalExecutable("openclaw");
        OpenClawCliExecutor exec = new OpenClawCliExecutor(cfg);
        // 消息体无空格，避免 commons-exec CommandLine.toStrings() 对 token 加引号导致与裸字符串数组比较不一致
        AgentOptions opts = AgentOptions.builder()
                .agent("ops")
                .message("SummarizeLogs")
                .build();
        List<String> args = new ArrayList<>();
        args.add("agent");
        args.addAll(opts.toSubcommandArguments());
        CommandLine cmd = exec.toCommandLine(OpenClawCliRequest.builder().arguments(args).build());
        assertArrayEquals(
                new String[]{"openclaw", "agent", "--message", "SummarizeLogs", "--agent", "ops"},
                cmd.toStrings());
    }
}
