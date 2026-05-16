package io.github.hiwepy.openclaw.cli.opts;

import io.github.hiwepy.openclaw.util.OpenClawLists;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * {@link GatewayCliArgv} 与 {@link GatewayRpcOptions} 拼装测试。
 */
class GatewayCliArgvTest {

    @Test
    void health_matchesDocExample() {
        GatewayRpcOptions rpc = GatewayRpcOptions.builder()
                .url("ws://127.0.0.1:18789")
                .build();
        assertEquals(
                OpenClawLists.of("health", "--url", "ws://127.0.0.1:18789"),
                GatewayCliArgv.health(rpc));
    }

    @Test
    void status_withFlags() {
        GatewayRpcOptions rpc = GatewayRpcOptions.builder()
                .json(true)
                .build();
        GatewayCliArgv.GatewayStatusOptions extra = GatewayCliArgv.GatewayStatusOptions.builder()
                .requireRpc(true)
                .build();
        assertEquals(
                OpenClawLists.of("status", "--json", "--require-rpc"),
                GatewayCliArgv.status(rpc, extra));
    }

    @Test
    void probe_withSsh() {
        GatewayRpcOptions rpc = GatewayRpcOptions.builder().json(true).build();
        GatewayCliArgv.GatewayProbeOptions p = GatewayCliArgv.GatewayProbeOptions.builder()
                .ssh("user@gateway-host")
                .build();
        assertEquals(
                OpenClawLists.of("probe", "--json", "--ssh", "user@gateway-host"),
                GatewayCliArgv.probe(rpc, p));
    }
}
