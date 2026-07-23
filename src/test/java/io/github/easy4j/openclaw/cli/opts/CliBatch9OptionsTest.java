package io.github.easy4j.openclaw.cli.opts;

import io.github.easy4j.openclaw.util.OpenClawLists;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * B9：voicecall / clawbot / acp 强类型 argv 映射烟测。
 */
class CliBatch9OptionsTest {

    @Test
    void acp_bridge_and_client() {
        assertEquals(
                OpenClawLists.of("--url",
                        "wss://g:1",
                        "--token-file",
                        "/t",
                        "--session",
                        "agent:main:main",
                        "--reset-session"),
                AcpOptions.builder()
                        .bridge()
                        .url("wss://g:1")
                        .tokenFile("/t")
                        .session("agent:main:main")
                        .resetSession(true)
                        .build()
                        .toSubcommandArguments());
        assertEquals(
                OpenClawLists.of("client", "--server-args", "--url", "wss://h", "--token-file", "/tok"),
                AcpOptions.builder()
                        .client()
                        .addServerArg("--url")
                        .addServerArg("wss://h")
                        .addServerArg("--token-file")
                        .addServerArg("/tok")
                        .build()
                        .toSubcommandArguments());
    }
}
