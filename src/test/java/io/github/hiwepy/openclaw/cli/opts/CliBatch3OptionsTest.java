package io.github.hiwepy.openclaw.cli.opts;

import io.github.hiwepy.openclaw.util.OpenClawLists;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * B3：node / nodes / devices 强类型 argv 映射烟测。
 */
class CliBatch3OptionsTest {

    @Test
    void node_run_and_install() {
        assertEquals(
                OpenClawLists.of("run", "--host", "gw.example.com", "--port", "18789", "--tls"),
                NodeOptions.builder()
                        .run()
                        .host("gw.example.com")
                        .port("18789")
                        .tls(true)
                        .build()
                        .toSubcommandArguments());
        assertEquals(OpenClawLists.of("status", "--json"), NodeOptions.builder().status().json(true).build().toSubcommandArguments());
        assertEquals(
                OpenClawLists.of("install", "--node-id", "n1", "--display-name", "edge-1", "--force"),
                NodeOptions.builder()
                        .install()
                        .nodeId("n1")
                        .displayName("edge-1")
                        .force(true)
                        .build()
                        .toSubcommandArguments());
    }

    @Test
    void nodes_list_and_invoke() {
        assertEquals(
                OpenClawLists.of("list", "--connected", "--last-connected", "node-a", "--json"),
                NodesOptions.builder()
                        .list()
                        .listConnected(true)
                        .lastConnected("node-a")
                        .json(true)
                        .build()
                        .toSubcommandArguments());
        assertEquals(
                OpenClawLists.of("invoke",
                        "--node",
                        "n1",
                        "--command",
                        "ping",
                        "--params",
                        "{}",
                        "--invoke-timeout",
                        "30s",
                        "--idempotency-key",
                        "k1",
                        "--url",
                        "wss://gw:18789"),
                NodesOptions.builder()
                        .invoke("n1", "ping")
                        .paramsJson("{}")
                        .invokeTimeout("30s")
                        .idempotencyKey("k1")
                        .url("wss://gw:18789")
                        .build()
                        .toSubcommandArguments());
    }

    @Test
    void devices_approve_rotate() {
        assertEquals(
                OpenClawLists.of("approve", "--latest", "--url", "wss://x", "--json"),
                DevicesOptions.builder()
                        .approve()
                        .approveLatest(true)
                        .url("wss://x")
                        .json(true)
                        .build()
                        .toSubcommandArguments());
        assertEquals(
                OpenClawLists.of("rotate", "--device", "d1", "--role", "ops", "--scope", "a", "--scope", "b"),
                DevicesOptions.builder().rotate("d1", "ops").scope("a").scope("b").build().toSubcommandArguments());
    }
}
