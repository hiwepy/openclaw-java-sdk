# openclaw-java-sdk

纯 Java 库（无 Spring）：使用 Kong Unirest 调用 OpenClaw Gateway `POST /hooks/agent`（推荐 `OpenClawClient#agent`，兼容旧方法 `OpenClawClient#invokeViaGateway`）：
- **任意顶层命令**：`OpenClawClient#cli()` 返回 [`OpenClawCli`](src/main/java/com/github/hiwepy/openclaw/cli/OpenClawCli.java)，每个方法与官方 CLI 文档页一一对应；第二个参数为 [`cli.opts`](src/main/java/com/github/hiwepy/openclaw/cli/opts) 包中与该命令对应的 `*Options`（实现 [`CliSubArgs`](src/main/java/com/github/hiwepy/openclaw/cli/args/CliSubArgs.java)），例如 [`SetupOptions`](src/main/java/com/github/hiwepy/openclaw/cli/opts/SetupOptions.java)、[`AgentOptions`](src/main/java/com/github/hiwepy/openclaw/cli/opts/AgentOptions.java)。Gateway 另可用 [`GatewayCommandOptions`](src/main/java/com/github/hiwepy/openclaw/cli/opts/GatewayCommandOptions.java) 与 `GatewayRpcOptions`。

HTTP 与 CLI 互不降级。入口类 `OpenClawClient`。

Spring Boot 应用请使用 [openclaw-spring-boot-starter](../openclaw-spring-boot-starter)。

## CLI 封装与文档映射

全局参数（`--dev`、`--profile`、`--container`、`--no-color`）请使用 [`OpenClawCliRequest`](src/main/java/com/github/hiwepy/openclaw/cli/OpenClawCliRequest.java) 并通过 [`OpenClawCli#execute`](src/main/java/com/github/hiwepy/openclaw/cli/OpenClawCli.java) 调用。

**表格怎么读**：第一列是官方 CLI 的**顶层**命令（`openclaw <cmd>` 里的 `<cmd>`），在 `OpenClawCli` 里**都已经**有同名方法；第二参数类型为 `cli.opts` 中 `*Options`（如 `SetupOptions`、`DaemonOptions`；文档未逐 flag 建模的命令提供 `*Options.builder().add(...)` 作为扩展）。Gateway 的 RPC 场景优先用 `GatewayRpcOptions` + `gatewayHealth` / `gatewayStatus` / `gatewayProbe` 或 `GatewayCommandOptions.builder().health(...)`，见下文「Gateway RPC」。

| `OpenClawCli` 方法 | 官方文档 |
|-------------------|----------|
| `version()` / `help()` | [CLI Reference（全局）](https://docs.openclaw.ai/cli) |
| `gateway` | [gateway](https://docs.openclaw.ai/cli/gateway) |
| `daemon` | [daemon](https://docs.openclaw.ai/cli/daemon) |
| `health` | [health](https://docs.openclaw.ai/cli/health) |
| `status` | [status](https://docs.openclaw.ai/cli/status) |
| `doctor` | [doctor](https://docs.openclaw.ai/cli/doctor) |
| `logs` | [logs](https://docs.openclaw.ai/cli/logs) |
| `config` | [config](https://docs.openclaw.ai/cli/config) |
| `configure` | [configure](https://docs.openclaw.ai/cli/configure) |
| `setup` | [setup](https://docs.openclaw.ai/cli/setup) |
| `onboard` | [onboard](https://docs.openclaw.ai/cli/onboard) |
| `docs` | [docs](https://docs.openclaw.ai/cli/docs) |
| `agent` | [agent](https://docs.openclaw.ai/cli/agent) |
| `agents` | [agents](https://docs.openclaw.ai/cli/agents) |
| `sessions` | [sessions](https://docs.openclaw.ai/cli/sessions) |
| `skills` | [skills](https://docs.openclaw.ai/cli/skills) |
| `memory` | [memory](https://docs.openclaw.ai/cli/memory) |
| `approvals` | [approvals](https://docs.openclaw.ai/cli/approvals) |
| `channels` | [channels](https://docs.openclaw.ai/cli/channels) |
| `message` | [message](https://docs.openclaw.ai/cli/message) |
| `pairing` | [pairing](https://docs.openclaw.ai/cli/pairing) |
| `qr` | [qr](https://docs.openclaw.ai/cli/qr) |
| `node` | [node](https://docs.openclaw.ai/cli/node) |
| `nodes` | [nodes](https://docs.openclaw.ai/cli/nodes) |
| `devices` | [devices](https://docs.openclaw.ai/cli/devices) |
| `browser` | [browser](https://docs.openclaw.ai/cli/browser) |
| `mcp` | [mcp](https://docs.openclaw.ai/cli/mcp) |
| `plugins` | [plugins](https://docs.openclaw.ai/cli/plugins) |
| `cron` | [cron](https://docs.openclaw.ai/cli/cron) |
| `hooks` | [hooks](https://docs.openclaw.ai/cli/hooks) |
| `webhooks` | [webhooks](https://docs.openclaw.ai/cli/webhooks) |
| `flows` | [flows](https://docs.openclaw.ai/cli/flows) |
| `models` | [models](https://docs.openclaw.ai/cli/models) |
| `security` | [security](https://docs.openclaw.ai/cli/security) |
| `secrets` | [secrets](https://docs.openclaw.ai/cli/secrets) |
| `sandbox` | [sandbox](https://docs.openclaw.ai/cli/sandbox) |
| `backup` | [backup](https://docs.openclaw.ai/cli/backup) |
| `update` | [update](https://docs.openclaw.ai/cli/update) |
| `uninstall` | [uninstall](https://docs.openclaw.ai/cli/uninstall) |
| `reset` | [reset](https://docs.openclaw.ai/cli/reset) |
| `completion` | [completion](https://docs.openclaw.ai/cli/completion) |
| `tui` | [tui](https://docs.openclaw.ai/cli/tui) |
| `dashboard` | [dashboard](https://docs.openclaw.ai/cli/dashboard) |
| `directory` | [directory](https://docs.openclaw.ai/cli/directory) |
| `dns` | [dns](https://docs.openclaw.ai/cli/dns) |
| `system` | [system](https://docs.openclaw.ai/cli/system) |
| `voicecall` | [voicecall](https://docs.openclaw.ai/cli/voicecall) |
| `clawbot` | [clawbot](https://docs.openclaw.ai/cli/clawbot) |
| `acp` | [acp](https://docs.openclaw.ai/cli/acp) |

## Gateway RPC 类型化参数（`cli.opts`）

与 [gateway CLI](https://docs.openclaw.ai/cli/gateway) 中「Query a running Gateway」共享的 `--url`、`--token`、`--password`、`--timeout`、`--expect-final`、`--json` 等，可用 [`GatewayRpcOptions`](src/main/java/com/github/hiwepy/openclaw/cli/opts/GatewayRpcOptions.java) 构建；[`GatewayCliArgv`](src/main/java/com/github/hiwepy/openclaw/cli/opts/GatewayCliArgv.java) 生成 `gateway health|status|probe` 的参数列表（不含可执行文件名）。

[`OpenClawCli`](src/main/java/com/github/hiwepy/openclaw/cli/OpenClawCli.java) 提供便捷方法：

- `gatewayHealth(GatewayRpcOptions)` → `openclaw gateway health ...`
- `gatewayStatus(GatewayRpcOptions, GatewayCliArgv.GatewayStatusOptions)` → `openclaw gateway status ...`
- `gatewayProbe(GatewayRpcOptions, GatewayCliArgv.GatewayProbeOptions)` → `openclaw gateway probe ...`

也可自行调用 `GatewayCliArgv.health(...)` 等得到 `List<String>`，组装为 `GatewayCommandOptions.builder().add(...)`，或传入 `OpenClawCliRequest.arguments("gateway", ...)`。
