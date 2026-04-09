package com.github.hiwepy.openclaw.cli.opts;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * B9：voicecall / clawbot / acp 强类型 argv 映射烟测。
 */
class CliBatch9OptionsTest {

    @Test
    void voicecall_call_and_expose() {
        assertEquals(
                List.of("call", "--to", "+1", "--message", "Hello", "--mode", "notify"),
                VoicecallOptions.builder().call("+1", "Hello", "notify").build().toSubcommandArguments());
        assertEquals(
                List.of("expose", "--mode", "serve"),
                VoicecallOptions.builder().expose(VoicecallOptions.ExposeMode.SERVE).build().toSubcommandArguments());
    }

    @Test
    void clawbot_qr() {
        assertEquals(
                List.of("qr", "--remote", "--json"),
                ClawbotOptions.builder().qr().remote(true).json(true).build().toSubcommandArguments());
    }

    @Test
    void acp_bridge_and_client() {
        assertEquals(
                List.of(
                        "--url",
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
                List.of("client", "--server-args", "--url", "wss://h", "--token-file", "/tok"),
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
