package io.github.easy4j.openclaw.cli.opts;

import io.github.easy4j.openclaw.util.OpenClawLists;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * B1：agents / sessions / skills / memory 强类型 argv 映射烟测。
 */
class CliBatch1OptionsTest {

    @Test
    void agents_defaultList_and_explicitList() {
        assertEquals(OpenClawLists.empty(), AgentsOptions.builder().defaultList().build().toSubcommandArguments());
        assertEquals(
                OpenClawLists.of("list", "--json"),
                AgentsOptions.builder().list().listJson(true).build().toSubcommandArguments());
        assertEquals(
                OpenClawLists.of("add", "work", "--workspace", "/ws", "--non-interactive"),
                AgentsOptions.builder()
                        .add("work")
                        .workspace("/ws")
                        .nonInteractive(true)
                        .build()
                        .toSubcommandArguments());
        assertEquals(
                OpenClawLists.of("bind", "--agent", "work", "--bind", "telegram:ops"),
                AgentsOptions.builder()
                        .bindCommand()
                        .bindAgent("work")
                        .bind("telegram:ops")
                        .build()
                        .toSubcommandArguments());
    }

    @Test
    void sessions_list_and_cleanup() {
        assertEquals(
                OpenClawLists.of("--agent", "work", "--json"),
                SessionsOptions.builder().agent("work").json(true).build().toSubcommandArguments());
        assertEquals(
                OpenClawLists.of("cleanup", "--dry-run", "--all-agents", "--json"),
                SessionsOptions.builder()
                        .cleanup()
                        .cleanupDryRun(true)
                        .allAgents(true)
                        .cleanupJson(true)
                        .build()
                        .toSubcommandArguments());
    }

    @Test
    void skills_search_install_list() {
        assertEquals(
                OpenClawLists.of("search", "calendar", "--limit", "20", "--json"),
                SkillsOptions.builder()
                        .search("calendar")
                        .searchLimit(20)
                        .searchJson(true)
                        .build()
                        .toSubcommandArguments());
        assertEquals(
                OpenClawLists.of("install", "my-skill", "--version", "1.0.0", "--force"),
                SkillsOptions.builder()
                        .install("my-skill")
                        .installVersion("1.0.0")
                        .installForce(true)
                        .build()
                        .toSubcommandArguments());
        assertEquals(
                OpenClawLists.of("--json", "--verbose"),
                SkillsOptions.builder().defaultList().listJson(true).listVerbose(true).build().toSubcommandArguments());
    }
}
