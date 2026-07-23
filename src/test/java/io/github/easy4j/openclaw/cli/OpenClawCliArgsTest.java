package io.github.easy4j.openclaw.cli;

import io.github.easy4j.openclaw.OpenClawCliConfig;
import io.github.easy4j.openclaw.cli.opts.DaemonOptions;
import io.github.easy4j.openclaw.cli.opts.GatewayCommandOptions;
import io.github.easy4j.openclaw.cli.opts.GatewayRpcOptions;
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
        OpenClawCliConfig cfg = new OpenClawCliConfig();
        cfg.setExecutable("openclaw");
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
    void daemon_empty_yieldsTopLevelOnly() {
        OpenClawCliConfig cfg = new OpenClawCliConfig();
        cfg.setExecutable("openclaw");
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
