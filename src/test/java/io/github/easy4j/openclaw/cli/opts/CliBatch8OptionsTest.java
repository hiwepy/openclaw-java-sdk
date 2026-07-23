package io.github.easy4j.openclaw.cli.opts;

import io.github.easy4j.openclaw.util.OpenClawLists;
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
                OpenClawLists.of("--shell", "fish", "--install", "--yes"),
                CompletionOptions.builder()
                        .shell(CompletionOptions.Shell.FISH)
                        .install(true)
                        .yes(true)
                        .build()
                        .toSubcommandArguments());
        assertEquals(
                OpenClawLists.of("--url", "ws://127.0.0.1:18789", "--session", "main", "--deliver"),
                TuiOptions.builder().url("ws://127.0.0.1:18789").session("main").deliver(true).build().toSubcommandArguments());
    }

    @Test
    void directory_peers() {
        assertEquals(
                OpenClawLists.of("peers", "list", "--channel", "slack", "--query", "U0", "--json"),
                DirectoryOptions.builder()
                        .peersList()
                        .channel("slack")
                        .query("U0")
                        .json(true)
                        .build()
                        .toSubcommandArguments());
    }
}
