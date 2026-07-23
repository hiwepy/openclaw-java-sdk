package io.github.easy4j.openclaw.cli.opts;

import io.github.easy4j.openclaw.util.OpenClawLists;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * B10：openclaw 2026.7.1 新增命令 argv 映射烟测。
 * <p>覆盖 {@code chat}、{@code terminal}、{@code commitments}、{@code crestodian}、
 * {@code exec-approvals}、{@code exec-policy}、{@code migrate}、{@code proxy}、{@code worktrees}、
 * {@code tool}、{@code tools} 共 11 个新命令。</p>
 */
class CliBatch10OptionsTest {

    @Test
    void chat_localWithMessage() {
        List<String> argv = ChatOptions.builder()
                .local(true)
                .session("main")
                .message("hello")
                .historyLimit(100)
                .build()
                .toSubcommandArguments();
        assertEquals(OpenClawLists.of("--local", "--session", "main", "--message", "hello", "--history-limit", "100"), argv);
    }

    @Test
    void chat_remoteUrl() {
        List<String> argv = ChatOptions.builder()
                .url("ws://gw:18789")
                .token("tok")
                .deliver(true)
                .build()
                .toSubcommandArguments();
        assertEquals(OpenClawLists.of("--url", "ws://gw:18789", "--token", "tok", "--deliver"), argv);
    }

    @Test
    void terminal_aliasOfTui() {
        // terminal 与 chat 共享选项集
        List<String> argv = TerminalOptions.builder()
                .message("hi")
                .build()
                .toSubcommandArguments();
        assertEquals(OpenClawLists.of("--message", "hi"), argv);
    }

    @Test
    void tool_reservedRoot_empty() {
        assertTrue(ToolOptions.empty().toSubcommandArguments().isEmpty());
    }

    @Test
    void tools_helpAlias_empty() {
        assertTrue(ToolsOptions.empty().toSubcommandArguments().isEmpty());
    }

    @Test
    void crestodian_messageAndYes() {
        List<String> argv = CrestodianOptions.builder()
                .message("check")
                .yes(true)
                .json(true)
                .build()
                .toSubcommandArguments();
        assertEquals(OpenClawLists.of("--message", "check", "--yes", "--json"), argv);
    }

    @Test
    void commitments_listDefault() {
        List<String> argv = CommitmentsOptions.builder()
                .agent("work")
                .json(true)
                .build()
                .toSubcommandArguments();
        assertEquals(OpenClawLists.of("--json", "--agent", "work"), argv);
    }

    @Test
    void commitments_dismissByIds() {
        List<String> argv = CommitmentsOptions.builder()
                .dismiss()
                .dismissIds(Arrays.asList("c1", "c2"))
                .build()
                .toSubcommandArguments();
        assertEquals(OpenClawLists.of("dismiss", "c1", "c2"), argv);
    }

    @Test
    void execPolicy_show() {
        List<String> argv = ExecPolicyOptions.builder()
                .show()
                .json(true)
                .build()
                .toSubcommandArguments();
        assertEquals(OpenClawLists.of("show", "--json"), argv);
    }

    @Test
    void execPolicy_preset() {
        List<String> argv = ExecPolicyOptions.builder()
                .preset("cautious")
                .build()
                .toSubcommandArguments();
        assertEquals(OpenClawLists.of("preset", "cautious"), argv);
    }

    @Test
    void execPolicy_set() {
        List<String> argv = ExecPolicyOptions.builder()
                .set()
                .host("sandbox")
                .security("allowlist")
                .ask("on-miss")
                .build()
                .toSubcommandArguments();
        assertEquals(OpenClawLists.of("set", "--host", "sandbox", "--security", "allowlist", "--ask", "on-miss"), argv);
    }

    @Test
    void execApprovals_get() {
        List<String> argv = ExecApprovalsOptions.builder()
                .get()
                .node("node-1")
                .build()
                .toSubcommandArguments();
        assertEquals(OpenClawLists.of("get", "--node", "node-1"), argv);
    }

    @Test
    void execApprovals_allowlistAdd() {
        List<String> argv = ExecApprovalsOptions.builder()
                .allowlistAdd("git-*")
                .agent("work")
                .build()
                .toSubcommandArguments();
        assertEquals(OpenClawLists.of("allowlist", "add", "git-*", "--agent", "work"), argv);
    }

    @Test
    void migrate_planWithSkills() {
        List<String> argv = MigrateOptions.builder()
                .plan("hermes")
                .from("/src")
                .skills(Arrays.asList("s1", "s2"))
                .json(true)
                .build()
                .toSubcommandArguments();
        assertEquals(OpenClawLists.of("plan", "hermes", "--from", "/src", "--skill", "s1", "--skill", "s2", "--json"), argv);
    }

    @Test
    void migrate_applyForce() {
        List<String> argv = MigrateOptions.builder()
                .apply("hermes")
                .yes(true)
                .noBackup(true)
                .force(true)
                .build()
                .toSubcommandArguments();
        assertEquals(OpenClawLists.of("apply", "hermes", "--yes", "--no-backup", "--force"), argv);
    }

    @Test
    void proxy_start() {
        List<String> argv = ProxyOptions.builder()
                .start()
                .host("0.0.0.0")
                .port(8080)
                .build()
                .toSubcommandArguments();
        assertEquals(OpenClawLists.of("start", "--host", "0.0.0.0", "--port", "8080"), argv);
    }

    @Test
    void proxy_validateWithUrls() {
        List<String> argv = ProxyOptions.builder()
                .validate()
                .allowedUrls(Arrays.asList("https://a.com", "https://b.com"))
                .deniedUrls(Collections.singletonList("https://bad.com"))
                .timeoutMs(5000)
                .build()
                .toSubcommandArguments();
        assertEquals(
                OpenClawLists.of("validate",
                        "--allowed-url", "https://a.com",
                        "--allowed-url", "https://b.com",
                        "--denied-url", "https://bad.com",
                        "--timeout-ms", "5000"),
                argv);
    }

    @Test
    void worktrees_create() {
        List<String> argv = WorktreesOptions.builder()
                .create("/repo")
                .name("wt-1")
                .baseRef("main")
                .build()
                .toSubcommandArguments();
        assertEquals(OpenClawLists.of("create", "/repo", "--name", "wt-1", "--base-ref", "main"), argv);
    }

    @Test
    void worktrees_removeForce() {
        List<String> argv = WorktreesOptions.builder()
                .remove("wt-id")
                .force(true)
                .json(true)
                .build()
                .toSubcommandArguments();
        assertEquals(OpenClawLists.of("remove", "wt-id", "--force", "--json"), argv);
    }
}
