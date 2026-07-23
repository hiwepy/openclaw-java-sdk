package io.github.easy4j.openclaw.cli.opts;

import io.github.easy4j.openclaw.util.OpenClawLists;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * B5：cron / hooks / webhooks / flows 强类型 argv 映射烟测。
 */
class CliBatch5OptionsTest {

    @Test
    void cron_run_and_add() {
        assertEquals(
                OpenClawLists.of("run", "job-1", "--due"),
                CronOptions.builder().run("job-1").runDue(true).build().toSubcommandArguments());
        assertEquals(
                OpenClawLists.of("add",
                        "--name",
                        "Morning",
                        "--cron",
                        "0 7 * * *",
                        "--session",
                        "isolated",
                        "--message",
                        "Hi",
                        "--light-context",
                        "--no-deliver"),
                CronOptions.builder()
                        .add()
                        .name("Morning")
                        .cronExpr("0 7 * * *")
                        .session("isolated")
                        .message("Hi")
                        .lightContext(true)
                        .noDeliver(true)
                        .build()
                        .toSubcommandArguments());
    }

    @Test
    void hooks_list_and_enable() {
        assertEquals(
                OpenClawLists.of("list", "--json"),
                HooksOptions.builder().list().listJson(true).build().toSubcommandArguments());
        assertEquals(OpenClawLists.of("enable", "session-memory"), HooksOptions.builder().enable("session-memory").build().toSubcommandArguments());
    }

    @Test
    void flows_list() {
        assertEquals(OpenClawLists.of("flow", "list", "--json"), FlowsOptions.builder().list().listJson(true).build().toSubcommandArguments());
    }
}
