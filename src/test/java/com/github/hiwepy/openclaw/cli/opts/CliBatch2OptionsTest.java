package com.github.hiwepy.openclaw.cli.opts;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CliBatch2OptionsTest {

    @Test
    void qr_and_pairing() {
        assertEquals(
                List.of("--remote", "--json"),
                QrOptions.builder().remote(true).json(true).build().toSubcommandArguments());
        assertEquals(
                List.of("list", "telegram", "--json"),
                PairingOptions.builder().list("telegram").json(true).build().toSubcommandArguments());
        assertEquals(
                List.of("approve", "--channel", "telegram", "CODE", "--notify"),
                PairingOptions.builder()
                        .approve(null, "CODE")
                        .channel("telegram")
                        .notify(true)
                        .build()
                        .toSubcommandArguments());
    }

    @Test
    void approvals_and_channels() {
        assertEquals(
                List.of("get", "--gateway", "--json"),
                ApprovalsOptions.builder().get().gateway(true).json(true).build().toSubcommandArguments());
        assertEquals(
                List.of("capabilities", "--channel", "discord", "--json"),
                ChannelsOptions.builder().capabilities().channel("discord").json(true).build().toSubcommandArguments());
    }

    @Test
    void message_send() {
        assertEquals(
                List.of("send", "--channel", "slack", "--target", "C1", "--message", "hi", "--json"),
                MessageOptions.builder()
                        .action("send")
                        .channel("slack")
                        .target("C1")
                        .message("hi")
                        .json(true)
                        .build()
                        .toSubcommandArguments());
    }
}
