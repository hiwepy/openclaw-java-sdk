package io.github.hiwepy.openclaw.cli.opts;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * B1：agents / sessions / skills / memory 强类型 argv 映射烟测。
 */
class CliBatch1OptionsTest {

    @Test
    void agents_defaultList_and_explicitList() {
        assertEquals(List.of(), AgentsOptions.builder().defaultList().build().toSubcommandArguments());
        assertEquals(
                List.of("list", "--json"),
                AgentsOptions.builder().list().listJson(true).build().toSubcommandArguments());
        assertEquals(
                List.of("add", "work", "--workspace", "/ws", "--non-interactive"),
                AgentsOptions.builder()
                        .add("work")
                        .workspace("/ws")
                        .nonInteractive(true)
                        .build()
                        .toSubcommandArguments());
        assertEquals(
                List.of("bind", "--agent", "work", "--bind", "telegram:ops"),
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
                List.of("--agent", "work", "--json"),
                SessionsOptions.builder().agent("work").json(true).build().toSubcommandArguments());
        assertEquals(
                List.of("cleanup", "--dry-run", "--all-agents", "--json"),
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
                List.of("search", "calendar", "--limit", "20", "--json"),
                SkillsOptions.builder()
                        .search("calendar")
                        .searchLimit(20)
                        .searchJson(true)
                        .build()
                        .toSubcommandArguments());
        assertEquals(
                List.of("install", "my-skill", "--version", "1.0.0", "--force"),
                SkillsOptions.builder()
                        .install("my-skill")
                        .installVersion("1.0.0")
                        .installForce(true)
                        .build()
                        .toSubcommandArguments());
        assertEquals(
                List.of("--json", "--verbose"),
                SkillsOptions.builder().defaultList().listJson(true).listVerbose(true).build().toSubcommandArguments());
    }

    @Test
    void memory_status_and_search() {
        assertEquals(
                List.of("status", "--agent", "main", "--deep", "--json"),
                MemoryOptions.builder()
                        .status()
                        .agent("main")
                        .statusDeep(true)
                        .statusJson(true)
                        .build()
                        .toSubcommandArguments());
        assertEquals(
                List.of("search", "--query", "deployment", "--max-results", "20", "--json"),
                MemoryOptions.builder()
                        .search()
                        .searchQuery("deployment")
                        .maxResults(20)
                        .searchJson(true)
                        .build()
                        .toSubcommandArguments());
    }
}
