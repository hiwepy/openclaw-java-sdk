package io.github.hiwepy.openclaw.cli;

import io.github.hiwepy.openclaw.cli.args.CliSubArgs;
import io.github.hiwepy.openclaw.cli.opts.*;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 官方 CLI 顶层命令封装；每个方法接收 {@code io.github.hiwepy.openclaw.cli.opts} 包中的 {@link CliSubArgs} 实现（如 {@link SetupOptions}、{@link AgentOptions}），
 * 避免使用裸 {@code String...} 导致调用方不清楚参数顺序与含义。
 * <p>
 * 文档索引：<a href="https://docs.openclaw.ai/cli">CLI Reference</a>。
 * Gateway 的 RPC 查询场景可优先使用 {@link GatewayCommandOptions.Builder#health(GatewayRpcOptions)} 等，
 * 或使用 {@link #gatewayHealth(GatewayRpcOptions)} 等便捷方法。
 * </p>
 */
@Getter
@Slf4j
public class OpenClawCli {

    /**
     * 底层执行器（高级用法：直接构造 {@link OpenClawCliRequest}）
     */
    private final OpenClawCliExecutor executor;

    /**
     * @param executor 用于执行本地 {@code openclaw} 的执行器
     */
    public OpenClawCli(OpenClawCliExecutor executor) {
        this.executor = Objects.requireNonNull(executor, "executor");
    }

    /**
     * 等价于 {@code openclaw --version}（与文档中 {@code -V} / {@code --version} 一致）。
     * <p>示例（shell）：{@code openclaw --version}</p>
     *
     * @see <a href="https://docs.openclaw.ai/cli">CLI Reference</a>
     */
    public OpenClawCliResult version() {
        return executor.execute(OpenClawCliRequest.builder().arguments("--version").build());
    }

    /**
     * 等价于 {@code openclaw --help}（根级帮助，具体子命令请使用 {@code &lt;cmd&gt; --help}）。
     * <p>示例：{@code openclaw --help}</p>
     *
     * @see <a href="https://docs.openclaw.ai/cli">CLI Reference</a>
     */
    public OpenClawCliResult help() {
        return executor.execute(OpenClawCliRequest.builder().arguments("--help").build());
    }

    // --- Gateway & daemon & health ---

    /**
     * {@code openclaw gateway ...}。
     * <p>示例：{@code gateway(GatewayCommandOptions.builder().health(GatewayRpcOptions.builder().url("ws://127.0.0.1:18789").build()).build())}</p>
     *
     * @param args 子命令与 flag，见 {@link GatewayCommandOptions}
     * @see <a href="https://docs.openclaw.ai/cli/gateway">gateway CLI</a>
     */
    public OpenClawCliResult gateway(GatewayCommandOptions args) {
        return run("gateway", args);
    }

    /**
     * 类型化 {@code gateway health}（内部使用 {@link GatewayCommandOptions.Builder#health(GatewayRpcOptions)}）。
     *
     * @param rpcOptions 与文档「Query a running Gateway」一致的 RPC 选项
     * @see <a href="https://docs.openclaw.ai/cli/gateway">gateway CLI</a>
     * @see GatewayCliArgv#health(GatewayRpcOptions)
     */
    public OpenClawCliResult gatewayHealth(GatewayRpcOptions rpcOptions) {
        return gateway(GatewayCommandOptions.builder().health(rpcOptions).build());
    }

    /**
     * 类型化 {@code gateway status}。
     *
     * @see <a href="https://docs.openclaw.ai/cli/gateway">gateway CLI</a>
     * @see GatewayCliArgv#status(GatewayRpcOptions, GatewayCliArgv.GatewayStatusOptions)
     */
    public OpenClawCliResult gatewayStatus(GatewayRpcOptions rpcOptions,
                                           GatewayCliArgv.GatewayStatusOptions statusOptions) {
        return gateway(GatewayCommandOptions.builder().status(rpcOptions, statusOptions).build());
    }

    /**
     * 类型化 {@code gateway probe}。
     *
     * @see <a href="https://docs.openclaw.ai/cli/gateway">gateway CLI</a>
     * @see GatewayCliArgv#probe(GatewayRpcOptions, GatewayCliArgv.GatewayProbeOptions)
     */
    public OpenClawCliResult gatewayProbe(GatewayRpcOptions rpcOptions,
                                          GatewayCliArgv.GatewayProbeOptions probeOptions) {
        return gateway(GatewayCommandOptions.builder().probe(rpcOptions, probeOptions).build());
    }

    /**
     * {@code openclaw daemon ...}。
     *
     * @param args 子命令与 flag，见 {@link DaemonOptions}
     * @see <a href="https://docs.openclaw.ai/cli/daemon">daemon CLI</a>
     */
    public OpenClawCliResult daemon(DaemonOptions args) {
        return run("daemon", args);
    }

    /**
     * 顶层 {@code openclaw health}（非 {@code gateway health}）。
     *
     * @param args 子命令与 flag，见 {@link HealthCommandOptions}
     * @see <a href="https://docs.openclaw.ai/cli/health">health CLI</a>
     */
    public OpenClawCliResult health(HealthCommandOptions args) {
        return run("health", args);
    }

    /**
     * {@code openclaw status ...}（顶层 status，非仅 gateway 子命令）。
     *
     * @param args 子命令与 flag，见 {@link StatusCommandOptions}
     * @see <a href="https://docs.openclaw.ai/cli/status">status CLI</a>
     */
    public OpenClawCliResult status(StatusCommandOptions args) {
        return run("status", args);
    }

    /**
     * {@code openclaw doctor ...}。
     *
     * @param args 子命令与 flag，见 {@link DoctorOptions}
     * @see <a href="https://docs.openclaw.ai/cli/doctor">doctor CLI</a>
     */
    public OpenClawCliResult doctor(DoctorOptions args) {
        return run("doctor", args);
    }

    /**
     * {@code openclaw logs ...}。
     *
     * @param args 子命令与 flag，见 {@link LogsOptions}
     * @see <a href="https://docs.openclaw.ai/cli/logs">logs CLI</a>
     */
    public OpenClawCliResult logs(LogsOptions args) {
        return run("logs", args);
    }

    // --- Config & setup ---

    /**
     * {@code openclaw config ...}。
     * <p>示例：{@code config(ConfigOptions.builder().tail("get", "gateway.mode").build())}</p>
     *
     * @param args 子命令与 flag，见 {@link ConfigOptions}
     * @see <a href="https://docs.openclaw.ai/cli/config">config CLI</a>
     */
    public OpenClawCliResult config(ConfigOptions args) {
        return run("config", args);
    }

    /**
     * {@code openclaw configure ...}。
     *
     * @param args 子命令与 flag，见 {@link ConfigureOptions}
     * @see <a href="https://docs.openclaw.ai/cli/configure">configure CLI</a>
     */
    public OpenClawCliResult configure(ConfigureOptions args) {
        return run("configure", args);
    }

    /**
     * {@code openclaw setup ...}。
     *
     * @param args 子命令与 flag，见 {@link SetupOptions}
     * @see <a href="https://docs.openclaw.ai/cli/setup">setup CLI</a>
     */
    public OpenClawCliResult setup(SetupOptions args) {
        return run("setup", args);
    }

    /**
     * {@code openclaw onboard ...}。
     *
     * @param args 子命令与 flag，见 {@link OnboardOptions}
     * @see <a href="https://docs.openclaw.ai/cli/onboard">onboard CLI</a>
     */
    public OpenClawCliResult onboard(OnboardOptions args) {
        return run("onboard", args);
    }

    /**
     * {@code openclaw docs ...}。
     *
     * @param args 子命令与 flag，见 {@link DocsCommandOptions}
     * @see <a href="https://docs.openclaw.ai/cli/docs">docs CLI</a>
     */
    public OpenClawCliResult docs(DocsCommandOptions args) {
        return run("docs", args);
    }

    // --- Agents & sessions & skills ---

    /**
     * {@code openclaw agent ...}。
     * <p>使用 {@link AgentOptions} 构造完整参数：{@code --message} 必填；{@code --to} / {@code --session-id} / {@code --agent} 至少其一；
     * 可选 {@link ThinkingLevel}、{@link VerboseLevel}、{@link AgentOptions.Builder#timeoutSeconds(int)} 等。</p>
     * <p>示例：{@code agent(AgentOptions.builder().agent("ops").message("Summarize logs").build())}</p>
     *
     * @param args 子命令与 flag，见 {@link AgentOptions}
     * @see <a href="https://docs.openclaw.ai/cli/agent">agent CLI</a>
     */
    public OpenClawCliResult agent(AgentOptions args) {
        return run("agent", args);
    }

    /**
     * {@code openclaw agents ...}。
     *
     * @param args 子命令与 flag，见 {@link AgentsOptions}
     * @see <a href="https://docs.openclaw.ai/cli/agents">agents CLI</a>
     */
    public OpenClawCliResult agents(AgentsOptions args) {
        return run("agents", args);
    }

    /**
     * {@code openclaw sessions ...}。
     *
     * @param args 子命令与 flag，见 {@link SessionsOptions}
     * @see <a href="https://docs.openclaw.ai/cli/sessions">sessions CLI</a>
     */
    public OpenClawCliResult sessions(SessionsOptions args) {
        return run("sessions", args);
    }

    /**
     * {@code openclaw skills ...}。
     *
     * @param args 子命令与 flag，见 {@link SkillsOptions}
     * @see <a href="https://docs.openclaw.ai/cli/skills">skills CLI</a>
     */
    public OpenClawCliResult skills(SkillsOptions args) {
        return run("skills", args);
    }

    /**
     * {@code openclaw memory ...}。
     *
     * @param args 子命令与 flag，见 {@link MemoryOptions}
     * @see <a href="https://docs.openclaw.ai/cli/memory">memory CLI</a>
     */
    public OpenClawCliResult memory(MemoryOptions args) {
        return run("memory", args);
    }

    /**
     * {@code openclaw approvals ...}。
     *
     * @param args 子命令与 flag，见 {@link ApprovalsOptions}
     * @see <a href="https://docs.openclaw.ai/cli/approvals">approvals CLI</a>
     */
    public OpenClawCliResult approvals(ApprovalsOptions args) {
        return run("approvals", args);
    }

    // --- Channels & messaging & nodes ---

    /**
     * {@code openclaw channels ...}。
     *
     * @param args 子命令与 flag，见 {@link ChannelsOptions}
     * @see <a href="https://docs.openclaw.ai/cli/channels">channels CLI</a>
     */
    public OpenClawCliResult channels(ChannelsOptions args) {
        return run("channels", args);
    }

    /**
     * {@code openclaw message ...}。
     *
     * @param args 子命令与 flag，见 {@link MessageOptions}
     * @see <a href="https://docs.openclaw.ai/cli/message">message CLI</a>
     */
    public OpenClawCliResult message(MessageOptions args) {
        return run("message", args);
    }

    /**
     * {@code openclaw pairing ...}。
     *
     * @param args 子命令与 flag，见 {@link PairingOptions}
     * @see <a href="https://docs.openclaw.ai/cli/pairing">pairing CLI</a>
     */
    public OpenClawCliResult pairing(PairingOptions args) {
        return run("pairing", args);
    }

    /**
     * {@code openclaw qr ...}。
     *
     * @param args 子命令与 flag，见 {@link QrOptions}
     * @see <a href="https://docs.openclaw.ai/cli/qr">qr CLI</a>
     */
    public OpenClawCliResult qr(QrOptions args) {
        return run("qr", args);
    }

    /**
     * {@code openclaw node ...}。
     *
     * @param args 子命令与 flag，见 {@link NodeOptions}
     * @see <a href="https://docs.openclaw.ai/cli/node">node CLI</a>
     */
    public OpenClawCliResult node(NodeOptions args) {
        return run("node", args);
    }

    /**
     * {@code openclaw nodes ...}。
     *
     * @param args 子命令与 flag，见 {@link NodesOptions}
     * @see <a href="https://docs.openclaw.ai/cli/nodes">nodes CLI</a>
     */
    public OpenClawCliResult nodes(NodesOptions args) {
        return run("nodes", args);
    }

    /**
     * {@code openclaw devices ...}。
     *
     * @param args 子命令与 flag，见 {@link DevicesOptions}
     * @see <a href="https://docs.openclaw.ai/cli/devices">devices CLI</a>
     */
    public OpenClawCliResult devices(DevicesOptions args) {
        return run("devices", args);
    }

    // --- Browser & MCP & tools ---

    /**
     * {@code openclaw browser ...}。
     *
     * @param args 子命令与 flag，见 {@link BrowserOptions}
     * @see <a href="https://docs.openclaw.ai/cli/browser">browser CLI</a>
     */
    public OpenClawCliResult browser(BrowserOptions args) {
        return run("browser", args);
    }

    /**
     * {@code openclaw mcp ...}。
     *
     * @param args 子命令与 flag，见 {@link McpOptions}
     * @see <a href="https://docs.openclaw.ai/cli/mcp">mcp CLI</a>
     */
    public OpenClawCliResult mcp(McpOptions args) {
        return run("mcp", args);
    }

    /**
     * {@code openclaw plugins ...}。
     *
     * @param args 子命令与 flag，见 {@link PluginsOptions}
     * @see <a href="https://docs.openclaw.ai/cli/plugins">plugins CLI</a>
     */
    public OpenClawCliResult plugins(PluginsOptions args) {
        return run("plugins", args);
    }

    // --- Automation & webhooks & cron ---

    /**
     * {@code openclaw cron ...}。
     *
     * @param args 子命令与 flag，见 {@link CronOptions}
     * @see <a href="https://docs.openclaw.ai/cli/cron">cron CLI</a>
     */
    public OpenClawCliResult cron(CronOptions args) {
        return run("cron", args);
    }

    /**
     * {@code openclaw hooks ...}。
     *
     * @param args 子命令与 flag，见 {@link HooksOptions}
     * @see <a href="https://docs.openclaw.ai/cli/hooks">hooks CLI</a>
     */
    public OpenClawCliResult hooks(HooksOptions args) {
        return run("hooks", args);
    }

    /**
     * {@code openclaw webhooks ...}。
     *
     * @param args 子命令与 flag，见 {@link WebhooksOptions}
     * @see <a href="https://docs.openclaw.ai/cli/webhooks">webhooks CLI</a>
     */
    public OpenClawCliResult webhooks(WebhooksOptions args) {
        return run("webhooks", args);
    }

    /**
     * Task flow 子命令（官方文档对应 {@code openclaw tasks flow ...}，见 {@link FlowsOptions}）。
     *
     * @param args 子命令与 flag，见 {@link FlowsOptions}
     * @see <a href="https://docs.openclaw.ai/cli/flows">flows CLI</a>
     */
    public OpenClawCliResult flows(FlowsOptions args) {
        return run("tasks", args);
    }

    // --- Models & security & misc ---

    /**
     * {@code openclaw models ...}。
     *
     * @param args 子命令与 flag，见 {@link ModelsOptions}
     * @see <a href="https://docs.openclaw.ai/cli/models">models CLI</a>
     */
    public OpenClawCliResult models(ModelsOptions args) {
        return run("models", args);
    }

    /**
     * {@code openclaw security ...}。
     *
     * @param args 子命令与 flag，见 {@link SecurityOptions}
     * @see <a href="https://docs.openclaw.ai/cli/security">security CLI</a>
     */
    public OpenClawCliResult security(SecurityOptions args) {
        return run("security", args);
    }

    /**
     * {@code openclaw secrets ...}。
     *
     * @param args 子命令与 flag，见 {@link SecretsOptions}
     * @see <a href="https://docs.openclaw.ai/cli/secrets">secrets CLI</a>
     */
    public OpenClawCliResult secrets(SecretsOptions args) {
        return run("secrets", args);
    }

    /**
     * {@code openclaw sandbox ...}。
     *
     * @param args 子命令与 flag，见 {@link SandboxOptions}
     * @see <a href="https://docs.openclaw.ai/cli/sandbox">sandbox CLI</a>
     */
    public OpenClawCliResult sandbox(SandboxOptions args) {
        return run("sandbox", args);
    }

    /**
     * {@code openclaw backup ...}。
     *
     * @param args 子命令与 flag，见 {@link BackupOptions}
     * @see <a href="https://docs.openclaw.ai/cli/backup">backup CLI</a>
     */
    public OpenClawCliResult backup(BackupOptions args) {
        return run("backup", args);
    }

    /**
     * {@code openclaw update ...}。
     *
     * @param args 子命令与 flag，见 {@link UpdateOptions}
     * @see <a href="https://docs.openclaw.ai/cli/update">update CLI</a>
     */
    public OpenClawCliResult update(UpdateOptions args) {
        return run("update", args);
    }

    /**
     * {@code openclaw uninstall ...}。
     *
     * @param args 子命令与 flag，见 {@link UninstallOptions}
     * @see <a href="https://docs.openclaw.ai/cli/uninstall">uninstall CLI</a>
     */
    public OpenClawCliResult uninstall(UninstallOptions args) {
        return run("uninstall", args);
    }

    /**
     * {@code openclaw reset ...}。
     *
     * @param args 子命令与 flag，见 {@link ResetOptions}
     * @see <a href="https://docs.openclaw.ai/cli/reset">reset CLI</a>
     */
    public OpenClawCliResult reset(ResetOptions args) {
        return run("reset", args);
    }

    /**
     * {@code openclaw completion ...}。
     *
     * @param args 子命令与 flag，见 {@link CompletionOptions}
     * @see <a href="https://docs.openclaw.ai/cli/completion">completion CLI</a>
     */
    public OpenClawCliResult completion(CompletionOptions args) {
        return run("completion", args);
    }

    /**
     * {@code openclaw tui ...}。
     *
     * @param args 子命令与 flag，见 {@link TuiOptions}
     * @see <a href="https://docs.openclaw.ai/cli/tui">tui CLI</a>
     */
    public OpenClawCliResult tui(TuiOptions args) {
        return run("tui", args);
    }

    /**
     * {@code openclaw dashboard ...}。
     *
     * @param args 子命令与 flag，见 {@link DashboardOptions}
     * @see <a href="https://docs.openclaw.ai/cli/dashboard">dashboard CLI</a>
     */
    public OpenClawCliResult dashboard(DashboardOptions args) {
        return run("dashboard", args);
    }

    /**
     * {@code openclaw directory ...}。
     *
     * @param args 子命令与 flag，见 {@link DirectoryOptions}
     * @see <a href="https://docs.openclaw.ai/cli/directory">directory CLI</a>
     */
    public OpenClawCliResult directory(DirectoryOptions args) {
        return run("directory", args);
    }

    /**
     * {@code openclaw dns ...}。
     *
     * @param args 子命令与 flag，见 {@link DnsOptions}
     * @see <a href="https://docs.openclaw.ai/cli/dns">dns CLI</a>
     */
    public OpenClawCliResult dns(DnsOptions args) {
        return run("dns", args);
    }

    /**
     * {@code openclaw system ...}。
     *
     * @param args 子命令与 flag，见 {@link SystemOptions}
     * @see <a href="https://docs.openclaw.ai/cli/system">system CLI</a>
     */
    public OpenClawCliResult system(SystemOptions args) {
        return run("system", args);
    }

    /**
     * {@code openclaw voicecall ...}。
     *
     * @param args 子命令与 flag，见 {@link VoicecallOptions}
     * @see <a href="https://docs.openclaw.ai/cli/voicecall">voicecall CLI</a>
     */
    public OpenClawCliResult voicecall(VoicecallOptions args) {
        return run("voicecall", args);
    }

    /**
     * {@code openclaw clawbot ...}。
     *
     * @param args 子命令与 flag，见 {@link ClawbotOptions}
     * @see <a href="https://docs.openclaw.ai/cli/clawbot">clawbot CLI</a>
     */
    public OpenClawCliResult clawbot(ClawbotOptions args) {
        return run("clawbot", args);
    }

    /**
     * {@code openclaw acp ...}。
     *
     * @param args 子命令与 flag，见 {@link AcpOptions}
     * @see <a href="https://docs.openclaw.ai/cli/acp">acp CLI</a>
     */
    public OpenClawCliResult acp(AcpOptions args) {
        return run("acp", args);
    }

    /**
     * 使用自定义 {@link OpenClawCliRequest}（可设置全局 {@code --dev}、{@code --profile} 等）。
     *
     * @param request 完整请求（含参数列表）
     * @see <a href="https://docs.openclaw.ai/cli">CLI Reference</a>
     */
    public OpenClawCliResult execute(OpenClawCliRequest request) {
        return executor.execute(request);
    }

    /**
     * 组装 {@code openclaw &lt;顶层&gt; &lt;子参数...&gt;} 并执行。
     */
    private OpenClawCliResult run(String topLevel, CliSubArgs subArgs) {
        Objects.requireNonNull(topLevel, "topLevel");
        Objects.requireNonNull(subArgs, "subArgs");
        List<String> args = new ArrayList<>();
        args.add(topLevel);
        args.addAll(subArgs.toSubcommandArguments());
        return executor.execute(OpenClawCliRequest.builder().arguments(args).build());
    }
}
