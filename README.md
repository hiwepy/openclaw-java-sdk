# openclaw-java-sdk

纯 Java 库（无 Spring）：使用 Kong Unirest 调用 OpenClaw Gateway `POST /hooks/agent`（推荐 `OpenClawClient#agent`，兼容旧方法 `OpenClawClient#invokeViaGateway`）：
- **任意顶层命令**：`OpenClawClient#cli()` 返回 [`OpenClawCli`](src/main/java/io/github/hiwepy/openclaw/cli/OpenClawCli.java)，每个方法与官方 CLI 文档页一一对应；第二个参数为 [`cli.opts`](src/main/java/io/github/hiwepy/openclaw/cli/opts) 包中与该命令对应的 `*Options`（实现 [`CliSubArgs`](src/main/java/io/github/hiwepy/openclaw/cli/args/CliSubArgs.java)），例如 [`SetupOptions`](src/main/java/io/github/hiwepy/openclaw/cli/opts/SetupOptions.java)、[`AgentOptions`](src/main/java/io/github/hiwepy/openclaw/cli/opts/AgentOptions.java)。Gateway 另可用 [`GatewayCommandOptions`](src/main/java/io/github/hiwepy/openclaw/cli/opts/GatewayCommandOptions.java) 与 `GatewayRpcOptions`。

HTTP 与 CLI 互不降级。入口类 `OpenClawClient`。

Spring Boot 应用请使用 [openclaw-spring-boot-starter](../openclaw-spring-boot-starter)。

## 认证与能力边界

| 配置字段 | 含义 | 用于 |
|----------|------|------|
| `hooksToken` | 对应 Gateway `hooks.token` | Webhook：`Authorization: Bearer`（默认）或 `x-openclaw-token`（见下行） |
| `hooksUseXOpenclawTokenHeader` | `true` 时仅用 `x-openclaw-token`，`false` 时用 `Authorization: Bearer` | 与文档「二选一」一致，勿同时发两种头 |
| `apiKey`（兼容） | 未设 `hooksToken` 时的 Hook 令牌兜底 | 同上；**不是** `gateway.auth.token` |
| `gatewayAuthToken` / `gatewayAuthPassword` | 控制面凭证（如 `OPENCLAW_GATEWAY_TOKEN`、密码模式） | 文档对齐与后续 WS/高级 HTTP；当前 **Webhook HTTP 客户端不读取** |

### `POST /hooks/agent` 请求体（`InvokeAgentRequest`）

与 [Gateway configuration-reference — Hooks](https://docs.openclaw.ai/gateway/configuration-reference) 对齐的可选字段：`sessionKey`、`deliver`、`channel`、`to`、`model`、`thinking`；未设置的属性**不会**出现在 JSON 中。`sessionKey` 需网关配置 `hooks.allowRequestSessionKey` 等策略，否则可能被拒绝。

### Hook `sessionKey` 约定（`OpenClawSessionKeys` / `OpenClawClient`）

| 场景 | sessionKey | SDK 用法 |
|------|------------|----------|
| 一次性、无 peer | 不传 → Gateway 生成 `hook:<uuid>` | `client.agentOneShot(request)` |
| 一次性、有 peer | `hook:<peerId>:<uuid>` | `client.agentOneShotForPeer(peerId, request)` |
| 固定多轮 | `hook:<agentId>:<peerId>` | `client.agentWithStableSession(agentId, peerId, request)` 或 `agentWithStableSession(peerId, request)`（`request` 已含 `agentId`） |

也可手动：`OpenClawSessionKeys.forStableSession(...)` / `forEphemeralPeer(...)` / `newCorrelationId()` 写入 `InvokeAgentRequest#setSessionKey`，再调用 `agent(request)`。

```java
// 一次性、无 peer → Gateway 生成 hook:<uuid>
client.agentOneShot(request);

// 一次性、有 peer → hook:<peerId>:<uuid>
client.agentOneShotForPeer(userId, request);

// 固定多轮 → hook:<agentId>:<peerId>
client.agentWithStableSession("xiaohongshu-data-assistant", userId, request);
```

Gateway 建议：`hooks.allowRequestSessionKey: true`，`hooks.allowedSessionKeyPrefixes: ["hook:"]`。

OpenClaw 官方外部 [App SDK](https://docs.openclaw.ai/) 以 **WebSocket** 连 Gateway（`connect`、流式事件、`agent.wait` 等）。本 Java 库现阶段提供 **Webhook HTTP** + **本地 `openclaw` CLI**；若需与 TS `@openclaw/sdk` 对等的 WS 能力，需另行集成或等待本仓库扩展。

## CLI 封装与文档映射

全局参数（`--dev`、`--profile`、`--container`、`--no-color`）请使用 [`OpenClawCliRequest`](src/main/java/io/github/hiwepy/openclaw/cli/OpenClawCliRequest.java) 并通过 [`OpenClawCli#execute`](src/main/java/io/github/hiwepy/openclaw/cli/OpenClawCli.java) 调用。

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

与 [gateway CLI](https://docs.openclaw.ai/cli/gateway) 中「Query a running Gateway」共享的 `--url`、`--token`、`--password`、`--timeout`、`--expect-final`、`--json` 等，可用 [`GatewayRpcOptions`](src/main/java/io/github/hiwepy/openclaw/cli/opts/GatewayRpcOptions.java) 构建；[`GatewayCliArgv`](src/main/java/io/github/hiwepy/openclaw/cli/opts/GatewayCliArgv.java) 生成 `gateway health|status|probe` 的参数列表（不含可执行文件名）。

[`OpenClawCli`](src/main/java/io/github/hiwepy/openclaw/cli/OpenClawCli.java) 提供便捷方法：

- `gatewayHealth(GatewayRpcOptions)` → `openclaw gateway health ...`
- `gatewayStatus(GatewayRpcOptions, GatewayCliArgv.GatewayStatusOptions)` → `openclaw gateway status ...`
- `gatewayProbe(GatewayRpcOptions, GatewayCliArgv.GatewayProbeOptions)` → `openclaw gateway probe ...`

也可自行调用 `GatewayCliArgv.health(...)` 等得到 `List<String>`，组装为 `GatewayCommandOptions.builder().add(...)`，或传入 `OpenClawCliRequest.arguments("gateway", ...)`。

## 发布与 JDK

- 本模块要求 **JDK 17**（见 `pom.xml` 中 `maven-enforcer-plugin`）。
- 发布快照/正式版：配置 `~/.m2/settings.xml` 中与 `distributionManagement` 匹配的 server 凭据后执行：

```bash
mvn clean deploy -DskipTests
```

- 发布 [openclaw-spring-boot-starter](../openclaw-spring-boot-starter) 各 Spring Boot 线时，请在**对应分支**使用**该线要求的 JDK**（通常 2.x 为 JDK 8、3.x/4.x 为 17+，以各分支 `pom.xml` 为准），在 monorepo 根目录执行例如：

```bash
./release-starter-versions.sh openclaw-spring-boot-starter 2.7.x 20260516
```

执行前请确认该分支 `pom.xml` 中 **`openclaw-java-sdk.version` 与已部署的 SDK 版本一致**（`release-starter-versions.sh` 主要调整 parent 与构件版本，不一定会改传递依赖版本）。
