package com.github.hiwepy.openclaw.cli.opts;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * B8：completion / tui / dashboard / directory / dns / system 强类型 argv 映射烟测。
 */
class CliBatch8OptionsTest {

    @Test
    void completion_and_tui() {
        assertEquals(
                List.of("--shell", "fish", "--install", "--yes"),
                CompletionOptions.builder()
                        .shell(CompletionOptions.Shell.FISH)
                        .install(true)
                        .yes(true)
                        .build()
                        .toSubcommandArguments());
        assertEquals(
                List.of("--url", "ws://127.0.0.1:18789", "--session", "main", "--deliver"),
                TuiOptions.builder().url("ws://127.0.0.1:18789").session("main").deliver(true).build().toSubcommandArguments());
    }

    @Test
    void directory_peers() {
        assertEquals(
                List.of("peers", "list", "--channel", "slack", "--query", "U0", "--json"),
                DirectoryOptions.builder()
                        .peersList()
                        .channel("slack")
                        .query("U0")
                        .json(true)
                        .build()
                        .toSubcommandArguments());
    }

    @Test
    void dns_setup_and_system_event() {
        assertEquals(
                List.of("setup", "--domain", "openclaw.internal", "--apply"),
                DnsOptions.builder().domain("openclaw.internal").apply(true).build().toSubcommandArguments());
        assertEquals(
                List.of(
                        "event",
                        "--text",
                        "Check",
                        "--mode",
                        "now",
                        "--url",
                        "ws://127.0.0.1:18789",
                        "--json"),
                SystemOptions.builder()
                        .event("Check")
                        .eventMode("now")
                        .gatewayUrl("ws://127.0.0.1:18789")
                        .json(true)
                        .build()
                        .toSubcommandArguments());
    }
}
