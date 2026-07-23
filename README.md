# openclaw-java-sdk

纯 Java 库（无 Spring）：五条独立通信通道的门面客户端 [`OpenClawClient`](src/main/java/io/github/hiwepy/openclaw/OpenClawClient.java)。

| 通道 | 用途 | 入口方法 |
|------|------|----------|
| **HTTP Webhook** | 触发 agent / 注入事件 / 自定义 webhook | `hook()` / `wake()` |
| **Chat Completions** | `/v1/chat/completions` | `chat()` / `chatCompletion()` / `chatCompletionStream()` |
| **Models** | `/v1/models` | `listModels()` |
| **Embeddings** | `/v1/embeddings` | `embeddings()` / `createEmbeddings()` |
| **Responses** | `/v1/responses` | `responses()` / `createResponse()` |
| **Tools Invoke** | `/tools/invoke` | `toolInvoke()` |
| **WebSocket** | 双向实时通信、流式对话、完整 RPC | `ws()` / `connect()` / `chatSend()` |
| **CLI** | 本地 `openclaw` 命令封装 | `cli()` |

Spring Boot 应用请使用 [openclaw-spring-boot-starter](../openclaw-spring-boot-starter)。

---

## 快速开始

```java
import io.github.easy4j.openclaw.*;
import io.github.easy4j.openclaw.api.model.*;

// 1. 配置
OpenClawClientConfig config = new OpenClawClientConfig();
config.setGatewayBaseUrl("http://localhost:18789");
config.setGatewayAuthToken("your-gateway-token");

// 2. 创建客户端
OpenClawClient client = new OpenClawClient(config);

// 3. 调用（简洁写法）
ChatResponse resp = client.chatCompletion(
    "openclaw/default", "gpt-4o",
    List.of(ChatMessage.ofUser("你好"))
);
System.out.println(resp.getChoices().get(0).getMessage().getContent());

// 4. 释放
client.close();
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
| `hooksToken` | 对应 `hooks.token` | Webhook `Authorization: Bearer` |
| `apiKey`（兼容） | 未设 `hooksToken` 时的兜底 | 同 Webhook |

---

## OpenAI 兼容 API（`/v1/*`）

对应文档：[OpenAI Chat Completions](https://docs.openclaw.ai/gateway/openai-http-api)

### Agent 与 Model 字段约定

SDK 提供了清晰的 `agent` / `model` 分离：

| 字段 | 含义 | 示例 |
|------|------|------|
| `agent` | Agent 目标路由 | `"openclaw/default"`、`"openclaw/research"` |
| `model` | 后端 LLM 模型 | `"gpt-4o"`、`"claude-3-opus"` |

**优先级**：
1. `agent` 字段 → HTTP body 的 `model`（用于路由）
2. `model` 字段（若非 Agent 路由）→ `x-openclaw-model` header（用于覆盖后端模型）

### Chat Completions（非流式）

```java
import io.github.easy4j.openclaw.api.model.*;

// 方式1：Builder（推荐）
ChatRequest req = ChatRequest.builder()
    .agent("openclaw/default")
    .model("gpt-4o")
    .messages(List.of(
        ChatMessage.ofSystem("你是一个有用的助手"),
        ChatMessage.ofUser("今天天气怎么样？")
    ))
    .build();

// 方式2：便捷方法（最简洁）
ChatResponse resp = client.chatCompletion(
    "openclaw/default", "gpt-4o",
    List.of(ChatMessage.ofUser("今天天气怎么样？"))
);

// 方式3：setter
ChatRequest req = new ChatRequest();
req.setAgent("openclaw/default");
req.setModel("gpt-4o");
req.setMessages(List.of(ChatMessage.ofUser("你好")));
ChatResponse resp = client.chatCompletion(req);
String answer = resp.getChoices().get(0).getMessage().getContent();
```

### Chat Completions（流式 SSE）

```java
import io.github.easy4j.openclaw.api.sse.StreamingChatResponse;

ChatRequest req = ChatRequest.builder()
    .agent("openclaw/default")
    .model("gpt-4o")
    .messages(List.of(ChatMessage.ofUser("写一首关于春天的诗")))
    .build();

// 方式1：链式回调（推荐）
StreamingChatResponse stream = client.chatCompletionStream(req);
stream.onDelta(delta -> System.out.print(delta))       // 增量文本
      .onChunk(chunk -> System.out.println(chunk))    // 原始 chunk
      .onComplete(text -> System.out.println("\n完成")) // 流结束
      .onError(error -> error.printStackTrace());     // 错误处理

// 等待完整结果
ChatChunk result = stream.get();

// 方式2：Builder 模式
StreamingChatResponse stream2 = client.chatCompletionStream(req,
    StreamingChatResponse.builder()
        .onDelta(delta -> System.out.print(delta))
        .onComplete(text -> System.out.println("\n完成"))
        .build()
);
ChatChunk finalChunk = stream2.get();
```

### 流式工具调用

```java
// 定义客户端工具
List<Map<String, Object>> tools = List.of(
    Tools.function("get_weather", "获取指定城市的天气")
        .param("city", "string", "城市名称", true)
        .build()
);

ChatRequest req = ChatRequest.builder()
    .agent("openclaw/default")
    .model("gpt-4o")
    .messages(List.of(ChatMessage.ofUser("北京今天天气如何？")))
    .tools(tools)
    .toolChoice("auto")
    .build();

// 流式调用
StreamingChatResponse stream = client.chatCompletionStream(req);
stream.onDelta(delta -> System.out.print(delta))
      .onToolCall(toolCalls -> {
          // 处理工具调用
          for (ChatMessage.ToolCall tc : toolCalls) {
              String fnName = tc.getFunction().getName();
              String args = tc.getFunction().getArguments();
              // 执行工具...
          }
      })
      .onComplete(text -> System.out.println("\n完成"))
      .onError(error -> error.printStackTrace());
```

        StreamingChatResponse stream = client.chatCompletionStream(req);

// 处理工具调用
for(
        ChatMessage.ToolCall call :Tools.

        extractToolCalls(result.getChoices().

        get(0).

        getMessage())){
        Map<String, Object> args = Tools.parseArgsAsMap(call);
        String city = (String) args.get("city");

        // 执行工具...
        String weatherResult = executeGetWeather(city);

        // 构建工具结果消息
        ChatMessage toolResult = Tools.toolResult(call.getId(), weatherResult);

        // 继续对话（包含工具结果）
        List<ChatMessage> messagesWithResult = new java.util.ArrayList<>(req.getMessages());
    messagesWithResult.

        add(result.getChoices().

        get(0).

        getMessage()); // assistant tool call
        messagesWithResult.

        add(toolResult); // tool result

        ChatRequest followUp = ChatRequest.builder()
                .agent("openclaw/default")
                .model("gpt-4o")
                .messages(messagesWithResult)
                .build();

        ChatResponse finalResp = client.chatCompletion(followUp);
}
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
EmbeddingsRequest req = EmbeddingsRequest.builder()
    .agent("openclaw/default")
    .model("text-embedding-3-small")  // 指定嵌入模型
    .input(List.of("hello", "world"))
    .build();

EmbeddingsResponse resp = client.createEmbeddings(req);
```

### 会话行为

默认每次请求无状态（新会话 key）。复用会话的两种方式：

```java
// 方式 1：通过 user 字段派生稳定 session key
ChatResponse resp = client.chatCompletion(
    "openclaw/default", "gpt-4o", "conv:my-conversation-id",
    List.of(ChatMessage.ofUser("继续之前的讨论"))
);

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
ResponseRequest req = ResponseRequest.builder()
    .agent("openclaw/default")
    .model("gpt-4o")
    .input("分析今天的任务")
    .build();

ResponseResult result = client.createResponse(req);
System.out.println("状态: " + result.getStatus());
```

### Item-based 输入（图片 + 文件）

```java
ResponseRequest req = ResponseRequest.builder()
    .agent("openclaw/default")
    .input(List.of(
        ResponseRequest.InputItem.message()
            .role("system")
            .content("你是一个图片分析助手")
            .build(),
        ResponseRequest.InputItem.imageUrl("https://example.com/photo.jpg")
            .build(),
        ResponseRequest.InputItem.message()
            .role("user")
            .content("描述这张图片的内容")
            .build()
    ))
    .build();

ResponseResult result = client.createResponse(req);
```

### 流式

```java
ResponseRequest req = ResponseRequest.builder()
    .agent("openclaw/default")
    .input("写一个项目计划")
    .build();

StreamingChatResponse stream = client.chatCompletionStream(req);
stream.onDelta(delta -> System.out.print(delta));
ChatChunk result = stream.get();
```

---

## Tools Invoke API（`/tools/invoke`）

对应文档：[Tools Invoke API](https://docs.openclaw.ai/gateway/tools-invoke-http-api)

```java
ToolInvokeRequest req = ToolInvokeRequest.builder()
    .tool("sessions_list")
    .action("json")
    .args(Map.of())
    .build();

ToolInvokeResult result = client.toolInvoke(req);
if (Boolean.TRUE.equals(result.getOk())) {
    System.out.println("结果: " + result.getResult());
} else {
    System.err.println("错误: " + result.getError().getMessage());
}
```

---

## WebSocket 控制面

对应文档：[Gateway Protocol](https://docs.openclaw.ai/gateway/protocol)

```java
import io.github.easy4j.openclaw.ws.*;
import io.github.easy4j.openclaw.ws.protocol.*;

// 连接并握手
HelloOk hello = client.connect();
System.out.println("协议版本: " + hello.getProtocol());

// 流式对话
client.chatSend("你好", new ChatStreamHandler() {
    @Override
    public void onDelta(String text) { System.out.print(text); }
    @Override
    public void onComplete(String fullText) { System.out.println("\n[完成]"); }
    @Override
    public void onError(String error) { System.err.println("错误: " + error); }
});

// 指定会话的流式对话
client.chatSend("my-session", "继续讨论", handler);

// 非流式 RPC：向指定会话发消息
SessionsSendResult sendResult = client.sessionsSend("my-session", "你好");
```

---

## HTTP Webhook（`/hooks/*`）

```java
InvokeAgentRequest req = InvokeAgentRequest.builder()
    .message("总结今天的任务")
    .build();

// 一次性调用
InvokeAgentResult result = client.agentOneShot(req);

// 稳定多轮会话
InvokeAgentResult result2 = client.agentWithStableSession(
    "agent-id", userId, req);

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
    .model("gpt-4o")                              // x-openclaw-model
    .agentId("research")                          // x-openclaw-agent-id
    .sessionKey("my-session")                     // x-openclaw-session-key
    .messageChannel("slack")                      // x-openclaw-message-channel
    .scopes("operator.read,operator.write")       // x-openclaw-scopes
    .build();

// 用于任何 HTTP API
client.chatCompletion(req, headers);
```

| 头部 | 常量 | 用途 |
|------|------|------|
| `x-openclaw-model` | `OpenClawHeaders.X_OPENCLAW_MODEL` | 覆盖后端模型 |
| `x-openclaw-agent-id` | `OpenClawHeaders.X_OPENCLAW_AGENT_ID` | 兼容性 agent 覆盖 |
| `x-openclaw-session-key` | `OpenClawHeaders.X_OPENCLAW_SESSION_KEY` | 显式会话路由 |
| `x-openclaw-message-channel` | `OpenClawHeaders.X_OPENCLAW_MESSAGE_CHANNEL` | 合成入口通道上下文 |
| `x-openclaw-scopes` | `OpenClawHeaders.X_OPENCLAW_SCOPES` | 权限范围声明 |

---

## 发布与 JDK

| 分支 | 版本 | JDK | 用途 |
|------|------|-----|------|
| `feature/1.0.x` | `1.0.x.20260630-SNAPSHOT` | 1.8 | 兼容老项目、Spring Boot 2.x starter |
| `feature/2.0.x` | `2.0.x.20260630-SNAPSHOT` | 17 | 主流线、Spring Boot 3.x starter |
| `feature/3.0.x` | `3.0.x.20260630-SNAPSHOT` | 21 | 新项目、Spring Boot 4.x starter |
| `main` | `2.0.x.20260630-SNAPSHOT` | 17 | 默认分支，与 `feature/2.0.x` 保持一致 |

- 各分支的最低 JDK 要求由 `pom.xml` 中的 `<java.version>` 与 `maven-enforcer-plugin` 共同决定。
- 发布快照/正式版（在对应分支下，使用对应 JDK）：

```bash
mvn clean deploy -DskipTests
```

- 发布 [openclaw-spring-boot-starter](../openclaw-spring-boot-starter) 各 Spring Boot 线时：

```bash
./release-starter-versions.sh openclaw-spring-boot-starter 3.3.x 20251227
```

执行前请确认该分支 `pom.xml` 中 **`openclaw-java-sdk.version` 与已部署的 SDK 版本一致**。
