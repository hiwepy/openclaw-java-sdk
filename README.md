# openclaw-java-sdk

纯 Java 库（无 Spring）：五条独立通信通道的门面客户端 [`OpenClawClient`](src/main/java/io/github/hiwepy/openclaw/OpenClawClient.java)。

| 通道 | 用途 | 入口方法 |
|------|------|----------|
| **HTTP Webhook** | 无状态触发 agent | `agent()` / `wake()` / `hook()` |
| **OpenAI 兼容 HTTP** | Chat/Embeddings/Responses 标准 API | `chatCompletion()` / `listModels()` / `createEmbeddings()` / `createResponse()` |
| **OpenAI 流式 SSE** | 流式 Chat Completions / OpenResponses | `chatCompletionStream()` / `createResponseStream()` |
| **Tools Invoke HTTP** | 直接调用单个工具 | `toolInvoke()` |
| **WebSocket** | 双向实时通信、流式对话、完整 RPC | `ws()` / `wsConnect()` / `wsChatSend()` |
| **CLI** | 本地 `openclaw` 命令封装 | `cli()` → `OpenClawCli` |

Spring Boot 应用请使用 [openclaw-spring-boot-starter](../openclaw-spring-boot-starter)。

---

## 快速开始

```java
import io.github.hiwepy.openclaw.*;
import io.github.hiwepy.openclaw.api.model.*;

// 1. 配置
OpenClawClientConfig config = new OpenClawClientConfig();
config.

        setGatewayBaseUrl("http://localhost:18789");
config.

        setGatewayAuthToken("your-gateway-token");  // 控制面凭证 (gateway.auth.token)
config.

        setHooksToken("your-hooks-token");           // Webhook 凭证 (hooks.token)

        // 2. 创建客户端
        OpenClawClient client = new OpenClawClient(config);

        // 3. 调用
        ChatCompletionRequest req = new ChatCompletionRequest();
req.

        setModel("openclaw/default");
req.

        setMessages(List.of(new ChatCompletionMessage("user", "你好")));
        ChatCompletionResponse resp = client.chatCompletion(req);
System.out.

        println(resp.getChoices().

        get(0).

        getMessage().

        getContent());

// 4. 释放
        client.

        close();
```

---

## 认证与能力边界

SDK 对不同 API 端点使用**不同的凭证**：

| 端点 | 使用的凭证 | 配置字段 |
|------|-----------|----------|
| `POST /hooks/*` (Webhook) | Hooks 令牌 | `hooksToken`（或兼容 `apiKey`） |
| `POST /v1/*` (OpenAI 兼容) | Gateway 控制面凭证 | `gatewayAuthToken` → `gatewayAuthPassword` → `hooksToken` |
| `POST /tools/invoke` | Gateway 控制面凭证 | 同上 |
| WebSocket | Gateway 控制面凭证 | `gatewayAuthToken` / `gatewayAuthPassword` |

凭证解析优先级（控制面 API）：`gatewayAuthToken` > `gatewayAuthPassword` > `hooksToken` > `apiKey`。

| 配置字段 | 含义 | 用于 |
|----------|------|------|
| `gatewayAuthToken` | 对应 `gateway.auth.token` 或 `OPENCLAW_GATEWAY_TOKEN` | 控制面 HTTP + WS 鉴权 |
| `gatewayAuthPassword` | 对应 `gateway.auth.password` | 密码模式控制面鉴权 |
| `hooksToken` | 对应 `hooks.token` | Webhook `Authorization: Bearer`（默认）或 `x-openclaw-token` |
| `hooksUseXOpenclawTokenHeader` | `true` 时用 `x-openclaw-token`，`false` 时用 `Authorization: Bearer` | Webhook 头选择（二选一，勿同时发） |
| `apiKey`（兼容） | 未设 `hooksToken` 时的兜底 | 同 Webhook |

---

## OpenAI 兼容 API（`/v1/*`）

对应文档：[OpenAI Chat Completions](https://docs.openclaw.ai/gateway/openai-http-api)

### Agent 目标约定

OpenClaw 将 OpenAI `model` 字段解释为 **agent 目标**，而非原始模型 ID：

- `"openclaw"` / `"openclaw/default"` → 默认 agent
- `"openclaw/<agentId>"` → 指定 agent
- 兼容别名：`"openclaw:<agentId>"`、`"agent:<agentId>"`

使用 `x-openclaw-model` 头覆盖后端模型（如 `openai/gpt-5.4`）。

### Chat Completions（非流式）

```java
import io.github.hiwepy.openclaw.api.model.*;

ChatCompletionRequest req = new ChatCompletionRequest();
req.

setModel("openclaw/default");
req.

setMessages(List.of(
        new ChatCompletionMessage("system", "你是一个有用的助手"),
    new

ChatCompletionMessage("user","今天天气怎么样？")
));

// 基本调用
ChatCompletionResponse resp = client.chatCompletion(req);
String answer = resp.getChoices().get(0).getMessage().getContent();

// 带 OpenClaw 自定义头
Map<String, String> headers = OpenClawHeaders.builder()
        .model("openai/gpt-5.4")                    // x-openclaw-model：覆盖后端模型
        .sessionKey("my-session")                    // x-openclaw-session-key：显式会话路由
        .messageChannel("slack")                     // x-openclaw-message-channel：通道上下文
        .build();
ChatCompletionResponse resp2 = client.chatCompletion(req, headers);
```

### Chat Completions（流式 SSE）

```java
import io.github.hiwepy.openclaw.api.sse.*;

ChatCompletionRequest req = new ChatCompletionRequest();
req.

setModel("openclaw/default");
req.

setMessages(List.of(new ChatCompletionMessage("user", "写一首诗")));
// req.setStream(true) 会被自动设置

Map<String, String> headers = OpenClawHeaders.builder()
        .model("openai/gpt-5.4")
        .build();

client.

chatCompletionStream(req, headers, new SseEventHandler() {
    @Override
    public void onEvent (SseEvent event){
        ChatCompletionChunk chunk = (ChatCompletionChunk) event.getParsed();
        if (chunk != null && chunk.getChoices() != null) {
            for (ChatCompletionChunk.DeltaChoice choice : chunk.getChoices()) {
                if (choice.getDelta() != null && choice.getDelta().getContent() != null) {
                    System.out.print(choice.getDelta().getContent());
                }
            }
        }
    }

    @Override
    public void onComplete () {
        System.out.println("\n[流式完成]");
    }

    @Override
    public void onError (Throwable error){
        System.err.println("流式错误: " + error.getMessage());
    }
});
```

### 流式工具调用

```java
// 定义客户端工具
List<Map<String, Object>> tools = List.of(
    Map.of(
        "type", "function",
        "function", Map.of(
            "name", "get_weather",
            "description", "获取指定城市的天气",
            "parameters", Map.of(
                "type", "object",
                "properties", Map.of(
                    "city", Map.of("type", "string", "description", "城市名称")
                ),
                "required", List.of("city")
            )
        )
    )
);

ChatCompletionRequest req = new ChatCompletionRequest();
req.setModel("openclaw/default");
req.setMessages(List.of(new ChatCompletionMessage("user", "北京今天天气如何？")));
req.setTools(tools);
req.setToolChoice("auto");

client.chatCompletionStream(req, new SseEventHandler() {
    @Override
    public void onEvent(SseEvent event) {
        ChatCompletionChunk chunk = (ChatCompletionChunk) event.getParsed();
        if (chunk == null || chunk.getChoices() == null) return;
        for (ChatCompletionChunk.DeltaChoice choice : chunk.getChoices()) {
            // 文本内容
            if (choice.getDelta() != null && choice.getDelta().getContent() != null) {
                System.out.print(choice.getDelta().getContent());
            }
            // 工具调用增量
            if (choice.getDelta() != null && choice.getDelta().getToolCalls() != null) {
                for (ChatCompletionMessage.ToolCall tc : choice.getDelta().getToolCalls()) {
                    System.out.println("[工具调用] " + tc.getFunction().getName()
                        + " args=" + tc.getFunction().getArguments());
                }
            }
            // 完成原因
            if ("tool_calls".equals(choice.getFinishReason())) {
                System.out.println("\n[需要执行工具调用并回传结果]");
            }
        }
    }

    @Override
    public void onComplete() { System.out.println("[完成]"); }
    @Override
    public void onError(Throwable error) { error.printStackTrace(); }
});
```

### 获取模型列表

```java
ModelsResponse models = client.listModels();
for (ModelsResponse.ModelData m : models.getData()) {
    System.out.println(m.getId());  // openclaw, openclaw/default, openclaw/<agentId>
}

// 获取单个模型（自动 URL 编码）
ModelsResponse.ModelData model = client.openai().getModel("openclaw/default");
```

### Embeddings

```java
EmbeddingsRequest req = new EmbeddingsRequest();
req.setModel("openclaw/default");
req.setInput(List.of("hello", "world"));

// 无自定义头
EmbeddingsResponse resp = client.createEmbeddings(req);

// 指定嵌入模型
Map<String, String> headers = OpenClawHeaders.builder()
    .model("openai/text-embedding-3-small")
    .build();
EmbeddingsResponse resp2 = client.createEmbeddings(req, headers);
```

### 会话行为

默认每次请求无状态（新会话 key）。复用会话的两种方式：

```java
// 方式 1：通过 user 字段派生稳定 session key
ChatCompletionRequest req = new ChatCompletionRequest();
req.setModel("openclaw/default");
req.setUser("conv:my-conversation-id");  // 同一对话线程复用相同值
req.setMessages(List.of(new ChatCompletionMessage("user", "继续之前的讨论")));

// 方式 2：通过 x-openclaw-session-key 显式控制
Map<String, String> headers = OpenClawHeaders.builder()
    .sessionKey("my-explicit-session-key")
    .build();
```

---

## OpenResponses API（`/v1/responses`）

对应文档：[OpenResponses API](https://docs.openclaw.ai/gateway/openresponses-http-api)

### 非流式

```java
import io.github.hiwepy.openclaw.responses.model.*;

ResponseRequest req = new ResponseRequest();
req.setModel("openclaw");
req.setInput("分析今天的任务");

ResponseResult result = client.createResponse(req);
System.out.println("状态: " + result.getStatus());  // completed
```

### Item-based 输入（图片 + 文件）

```java
ResponseRequest req = new ResponseRequest();
req.setModel("openclaw/default");

// 使用 Item 数组作为输入
List<Map<String, Object>> input = List.of(
    // 系统指令
    Map.of("type", "message", "role", "system",
           "content", "你是一个图片分析助手"),
    // 图片输入（URL）
    Map.of("type", "input_image",
           "source", Map.of("type", "url", "url", "https://example.com/photo.jpg")),
    // 文件输入（base64）
    Map.of("type", "input_file",
           "source", Map.of("type", "base64", "media_type", "application/pdf",
                            "data", "base64-encoded-content", "filename", "report.pdf")),
    // 用户消息
    Map.of("type", "message", "role", "user", "content", "描述这张图片的内容")
);
req.setInput(input);

ResponseResult result = client.createResponse(req);
```

### 流式

```java
ResponseRequest req = new ResponseRequest();
req.setModel("openclaw");
req.setInput("写一个项目计划");

Map<String, String> headers = OpenClawHeaders.builder()
    .sessionKey("planning-session")
    .build();

client.createResponseStream(req, headers, new SseEventHandler() {
    @Override
    public void onEvent(SseEvent event) {
        // event.getEvent() 为事件类型：response.output_text.delta 等
        // event.getData() 为原始 JSON
        String eventType = event.getEvent();
        if ("response.output_text.delta".equals(eventType)) {
            // 解析 delta 文本
            try {
                var node = new com.fasterxml.jackson.databind.ObjectMapper().readTree(event.getData());
                if (node.has("delta")) {
                    System.out.print(node.get("delta").asText());
                }
            } catch (Exception ignored) {}
        }
    }

    @Override
    public void onComplete() { System.out.println("\n[完成]"); }
    @Override
    public void onError(Throwable error) { error.printStackTrace(); }
});
```

### 工具调用回传

```java
// 定义工具
List<Map<String, Object>> tools = List.of(
    Map.of("type", "function", "name", "get_weather",
           "description", "获取天气", "parameters", Map.of(...))
);

ResponseRequest req = new ResponseRequest();
req.setModel("openclaw");
req.setInput("北京天气");
req.setTools(tools);
req.setToolChoice("auto");

ResponseResult result = client.createResponse(req);

// 检查是否有 function_call 输出
for (Map<String, Object> item : result.getOutput()) {
    if ("function_call".equals(item.get("type"))) {
        String callId = (String) item.get("call_id");
        // 执行工具，发送回传
        ResponseRequest followUp = new ResponseRequest();
        followUp.setModel("openclaw");
        followUp.setInput(List.of(
            Map.of("type", "function_call_output",
                   "call_id", callId,
                   "output", "{\"temperature\": \"25°C\", \"condition\": \"晴\"}")
        ));
        ResponseResult finalResult = client.createResponse(followUp);
    }
}
```

---

## Tools Invoke API（`/tools/invoke`）

对应文档：[Tools Invoke API](https://docs.openclaw.ai/gateway/tools-invoke-http-api)

```java
import io.github.hiwepy.openclaw.tools.model.*;

ToolInvokeRequest req = new ToolInvokeRequest();
req.setTool("sessions_list");
req.setAction("json");
req.setArgs(Map.of());

ToolInvokeResult result = client.toolInvoke(req);
if (Boolean.TRUE.equals(result.getOk())) {
    System.out.println("结果: " + result.getResult());
} else {
    System.err.println("错误: " + result.getError().getMessage());
}
```

---

## WebSocket 控制面

对应文档：[Gateway Protocol](https://docs.openclaw.ai/gateway/protocol) / [Bridge Protocol (legacy)](https://docs.openclaw.ai/gateway/bridge-protocol)

```java
import io.github.hiwepy.openclaw.ws.*;
import io.github.hiwepy.openclaw.ws.protocol.*;

// 连接并握手
HelloOk hello = client.wsConnect();
System.out.println("协议版本: " + hello.getProtocol());
System.out.println("服务端版本: " + hello.getServer().getVersion());

// 流式对话
client.wsChatSend("你好", new ChatStreamHandler() {
    @Override
    public void onDelta(String text) { System.out.print(text); }
    @Override
    public void onComplete(String fullText) { System.out.println("\n[完成]"); }
    @Override
    public void onError(String error) { System.err.println("错误: " + error); }
});

// 指定会话的流式对话
client.wsChatSend("my-session", "继续讨论", handler);

// 非流式 RPC：向指定会话发消息
SessionsSendResult sendResult = client.wsSessionsSend("my-session", "你好");

// 添加事件监听器
client.addWsListener(new OpenClawWsListener() {
    @Override
    public void onConnected(HelloOk helloOk) { System.out.println("已连接"); }
    @Override
    public void onDisconnected(int code, String reason, boolean remote) { System.out.println("断开"); }
    @Override
    public void onEvent(EventFrame frame) { System.out.println("事件: " + frame.getEvent()); }
    @Override
    public void onResponse(ResponseFrame frame) { System.out.println("响应: " + frame.getId()); }
    @Override
    public void onError(Exception ex) { ex.printStackTrace(); }
});
```

---

## HTTP Webhook（`/hooks/*`）

```java
import io.github.hiwepy.openclaw.*;

InvokeAgentRequest req = new InvokeAgentRequest();
req.setMessage("总结今天的任务");

// 一次性调用
InvokeAgentResult result = client.agentOneShot(req);

// 带 peer
InvokeAgentResult result2 = client.agentOneShotForPeer(userId, req);

// 稳定多轮会话
InvokeAgentResult result3 = client.agentWithStableSession(
    "xiaohongshu-data-assistant", userId, req);

// 注入系统事件
client.wake("提醒：3点有会议", "now");

// 自定义映射 webhook
client.hook("my-webhook", Map.of("key", "value"));
```

### Hook `sessionKey` 约定

| 场景 | sessionKey | SDK 用法 |
|------|------------|----------|
| 一次性、无 peer | 不传 → Gateway 生成 `hook:<uuid>` | `client.agentOneShot(request)` |
| 一次性、有 peer | `hook:<peerId>:<uuid>` | `client.agentOneShotForPeer(peerId, request)` |
| 固定多轮 | `hook:<agentId>:<peerId>` | `client.agentWithStableSession(agentId, peerId, request)` |

Gateway 建议：`hooks.allowRequestSessionKey: true`，`hooks.allowedSessionKeyPrefixes: ["hook:"]`。

---

## OpenClaw 自定义请求头

通过 [`OpenClawHeaders`](src/main/java/io/github/hiwepy/openclaw/api/OpenClawHeaders.java) 构建器创建自定义头：

```java
Map<String, String> headers = OpenClawHeaders.builder()
    .model("openai/gpt-5.4")                              // x-openclaw-model
    .agentId("research")                                   // x-openclaw-agent-id
    .sessionKey("my-session")                              // x-openclaw-session-key
    .messageChannel("slack")                               // x-openclaw-message-channel
    .scopes("operator.read,operator.write")                // x-openclaw-scopes
    .build();

// 用于任何 HTTP API
client.chatCompletion(req, headers);
client.chatCompletionStream(req, headers, handler);
client.createResponse(req, headers);
client.createResponseStream(req, headers, handler);
client.openai().createEmbeddings(req, headers);
```

| 头部 | 常量 | 用途 |
|------|------|------|
| `x-openclaw-model` | `OpenClawHeaders.X_OPENCLAW_MODEL` | 覆盖后端模型（如 `openai/gpt-5.4`） |
| `x-openclaw-agent-id` | `OpenClawHeaders.X_OPENCLAW_AGENT_ID` | 兼容性 agent 覆盖 |
| `x-openclaw-session-key` | `OpenClawHeaders.X_OPENCLAW_SESSION_KEY` | 显式会话路由 |
| `x-openclaw-message-channel` | `OpenClawHeaders.X_OPENCLAW_MESSAGE_CHANNEL` | 合成入口通道上下文 |
| `x-openclaw-scopes` | `OpenClawHeaders.X_OPENCLAW_SCOPES` | 权限范围声明（共享密钥模式下被忽略） |

---

## CLI 封装

对应文档：[CLI Reference](https://docs.openclaw.ai/cli) / [CLI Backends](https://docs.openclaw.ai/gateway/cli-backends)

全局参数（`--dev`、`--profile`、`--container`、`--no-color`）使用 [`OpenClawCliRequest`](src/main/java/io/github/hiwepy/openclaw/cli/OpenClawCliRequest.java) 通过 [`OpenClawCli#execute`](src/main/java/io/github/hiwepy/openclaw/cli/OpenClawCli.java) 调用。

```java
// 版本
OpenClawCliResult version = client.cli().version();

// Gateway 健康检查
GatewayRpcOptions rpc = GatewayRpcOptions.builder()
    .url("ws://127.0.0.1:18789")
    .token("my-token")
    .build();
OpenClawCliResult health = client.cli().gatewayHealth(rpc);

// Agent 命令
AgentOptions agentOpts = AgentOptions.builder()
    .agent("ops")
    .message("总结日志")
    .build();
OpenClawCliResult agentResult = client.cli().agent(agentOpts);
```

| `OpenClawCli` 方法 | 官方文档 |
|-------------------|----------|
| `version()` / `help()` | [CLI Reference](https://docs.openclaw.ai/cli) |
| `gateway` / `gatewayHealth` / `gatewayStatus` / `gatewayProbe` | [gateway](https://docs.openclaw.ai/cli/gateway) |
| `daemon` | [daemon](https://docs.openclaw.ai/cli/daemon) |
| `health` / `status` / `doctor` / `logs` | [health](https://docs.openclaw.ai/cli/health) |
| `config` / `configure` / `setup` / `onboard` / `docs` | [config](https://docs.openclaw.ai/cli/config) |
| `agent` / `agents` / `sessions` / `skills` / `memory` / `approvals` | [agent](https://docs.openclaw.ai/cli/agent) |
| `channels` / `message` / `pairing` / `qr` | [channels](https://docs.openclaw.ai/cli/channels) |
| `node` / `nodes` / `devices` | [nodes](https://docs.openclaw.ai/cli/nodes) |
| `browser` / `mcp` / `plugins` | [browser](https://docs.openclaw.ai/cli/browser) |
| `cron` / `hooks` / `webhooks` / `flows` | [cron](https://docs.openclaw.ai/cli/cron) |
| `models` / `security` / `secrets` / `sandbox` | [models](https://docs.openclaw.ai/cli/models) |
| `backup` / `update` / `uninstall` / `reset` | [backup](https://docs.openclaw.ai/cli/backup) |
| `completion` / `tui` / `dashboard` / `directory` / `dns` / `system` | [completion](https://docs.openclaw.ai/cli/completion) |
| `voicecall` / `clawbot` / `acp` | [voicecall](https://docs.openclaw.ai/cli/voicecall) |

---

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
