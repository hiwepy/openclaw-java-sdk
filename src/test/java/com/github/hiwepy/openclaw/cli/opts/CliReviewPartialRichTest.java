package com.github.hiwepy.openclaw.cli.opts;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * 复核已有「强类型」CLI 选项：{@link GatewayCommandOptions}、{@link DaemonOptions} 等与文档一致的 argv 烟测。
 */
class CliReviewPartialRichTest {

    @Test
    void gateway_command_health_via_builder() {
        GatewayRpcOptions rpc = GatewayRpcOptions.builder().url("ws://127.0.0.1:18789").build();
        assertEquals(
                List.of("health", "--url", "ws://127.0.0.1:18789"),
                GatewayCommandOptions.builder().health(rpc).build().toSubcommandArguments());
    }

    @Test
    void daemon_status_matches_gateway_status_argv() {
        GatewayRpcOptions rpc = GatewayRpcOptions.builder().json(true).build();
        GatewayCliArgv.GatewayStatusOptions extra =
                GatewayCliArgv.GatewayStatusOptions.builder().deep(true).build();
        assertEquals(
                List.of("status", "--json", "--deep"),
                DaemonOptions.builder()
                        .subcommand(DaemonOptions.Subcommand.STATUS)
                        .statusRpc(rpc)
                        .statusExtra(extra)
                        .json(false)
                        .build()
                        .toSubcommandArguments());
    }
}
