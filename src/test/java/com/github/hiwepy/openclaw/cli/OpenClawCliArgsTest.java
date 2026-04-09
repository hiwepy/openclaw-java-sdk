package com.github.hiwepy.openclaw.cli;

import com.github.hiwepy.openclaw.OpenClawClientConfig;
import com.github.hiwepy.openclaw.cli.opts.DaemonOptions;
import com.github.hiwepy.openclaw.cli.opts.GatewayCommandOptions;
import com.github.hiwepy.openclaw.cli.opts.GatewayRpcOptions;
import com.github.hiwepy.openclaw.cli.opts.SetupOptions;
import org.apache.commons.exec.CommandLine;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

/**
 * {@link OpenClawCli} 与 {@code cli.opts} 参数对象组装后的命令行序列测试（仅 {@link OpenClawCliExecutor#toCommandLine}，不执行真实 openclaw）。
 */
class OpenClawCliArgsTest {

    /**
     * 与 {@link OpenClawCli#gateway(GatewayCommandOptions)} 内部拼装方式一致。
     */
    @Test
    void gateway_cliArgs_sameTokensAsOpenClawCliRun() {
        OpenClawClientConfig cfg = new OpenClawClientConfig();
        cfg.setLocalExecutable("openclaw");
        OpenClawCliExecutor exec = new OpenClawCliExecutor(cfg);
        GatewayCommandOptions g = GatewayCommandOptions.builder()
                .health(GatewayRpcOptions.builder().url("ws://127.0.0.1:18789").build())
                .build();
        List<String> args = new ArrayList<>();
        args.add("gateway");
        args.addAll(g.toSubcommandArguments());
        OpenClawCliRequest req = OpenClawCliRequest.builder().arguments(args).build();
        CommandLine cmd = exec.toCommandLine(req);
        assertArrayEquals(
                new String[]{"openclaw", "gateway", "health", "--url", "ws://127.0.0.1:18789"},
                cmd.toStrings());
    }

    @Test
    void setup_options_mapsDocumentedFlags() {
        OpenClawClientConfig cfg = new OpenClawClientConfig();
        cfg.setLocalExecutable("openclaw");
        OpenClawCliExecutor exec = new OpenClawCliExecutor(cfg);
        SetupOptions s = SetupOptions.builder()
                .workspace("/tmp/ws")
                .nonInteractive(true)
                .mode(SetupOptions.SetupMode.REMOTE)
                .remoteUrl("wss://gateway-host:18789")
                .remoteToken("tok")
                .build();
        List<String> args = new ArrayList<>();
        args.add("setup");
        args.addAll(s.toSubcommandArguments());
        OpenClawCliRequest req = OpenClawCliRequest.builder().arguments(args).build();
        assertArrayEquals(
                new String[]{
                        "openclaw", "setup",
                        "--workspace", "/tmp/ws",
                        "--non-interactive",
                        "--mode", "remote",
                        "--remote-url", "wss://gateway-host:18789",
                        "--remote-token", "tok"
                },
                exec.toCommandLine(req).toStrings());
    }

    @Test
    void daemon_empty_yieldsTopLevelOnly() {
        OpenClawClientConfig cfg = new OpenClawClientConfig();
        cfg.setLocalExecutable("openclaw");
        OpenClawCliExecutor exec = new OpenClawCliExecutor(cfg);
        List<String> args = new ArrayList<>();
        args.add("daemon");
        args.addAll(DaemonOptions.builder()
                .subcommand(DaemonOptions.Subcommand.STATUS)
                .json(false)
                .build()
                .toSubcommandArguments());
        OpenClawCliRequest req = OpenClawCliRequest.builder().arguments(args).build();
        assertArrayEquals(new String[]{"openclaw", "daemon", "status"}, exec.toCommandLine(req).toStrings());
    }
}
