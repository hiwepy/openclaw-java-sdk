package io.github.easy4j.openclaw.cli.opts;

import io.github.easy4j.openclaw.util.OpenClawLists;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * B4：browser / mcp / plugins 强类型 argv 映射烟测。
 */
class CliBatch4OptionsTest {

    @Test
    void mcp_serve_and_registry() {
        assertEquals(
                OpenClawLists.of("serve",
                        "--url",
                        "wss://h:18789",
                        "--token-file",
                        "/t",
                        "--claude-channel-mode",
                        "off",
                        "--verbose"),
                McpOptions.builder()
                        .serve()
                        .url("wss://h:18789")
                        .tokenFile("/t")
                        .claudeChannelMode(McpOptions.ClaudeChannelMode.OFF)
                        .verbose(true)
                        .build()
                        .toSubcommandArguments());
        assertEquals(
                OpenClawLists.of("set", "ctx", "{\"command\":\"uvx\"}"),
                McpOptions.builder().set("ctx", "{\"command\":\"uvx\"}").build().toSubcommandArguments());
    }

    @Test
    void plugins_list_and_install() {
        assertEquals(
                OpenClawLists.of("list", "--enabled", "--json"),
                PluginsOptions.builder().list().listEnabled(true).listJson(true).build().toSubcommandArguments());
        assertEquals(
                OpenClawLists.of("install", "-l", "./p", "--force"),
                PluginsOptions.builder().install("./p").installLink(true).installForce(true).build().toSubcommandArguments());
    }
}
