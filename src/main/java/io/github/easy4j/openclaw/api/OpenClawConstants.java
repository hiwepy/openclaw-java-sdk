package io.github.easy4j.openclaw.api;

/**
 * OpenClaw API 常量定义。
 * <p>
 * 集中管理所有魔法值，包括：
 * <ul>
 *   <li>HTTP 端点路径</li>
 *   <li>Chat Message 角色</li>
 *   <li>Chat Response 对象类型</li>
 *   <li>Finish Reason 完成原因</li>
 *   <li>Request Header 名称</li>
 *   <li>Request/Response 类型标识</li>
 *   <li>WS 消息类型</li>
 *   <li>Input Item 类型</li>
 *   <li>Think Level 思考级别</li>
 *   <li>工具调用相关常量</li>
 * </ul>
 */
public final class OpenClawConstants {

    private OpenClawConstants() {}

    // ==================== HTTP Endpoints ====================

    /** Chat Completions 端点 */
    public static final String ENDPOINT_CHAT_COMPLETIONS = "/v1/chat/completions";

    /** Models 列表端点 */
    public static final String ENDPOINT_MODELS = "/v1/models";

    /** Embeddings 端点 */
    public static final String ENDPOINT_EMBEDDINGS = "/v1/embeddings";

    /** Responses 端点 */
    public static final String ENDPOINT_RESPONSES = "/v1/responses";

    /** Tools Invoke 端点 */
    public static final String ENDPOINT_TOOLS_INVOKE = "/tools/invoke";

    /** Webhook: Agent 端点 */
    public static final String ENDPOINT_HOOKS_AGENT = "/hooks/agent";

    /** Webhook: Wake 端点 */
    public static final String ENDPOINT_HOOKS_WAKE = "/hooks/wake";

    // ==================== Message Roles ====================

    /** 消息角色：系统 */
    public static final String ROLE_SYSTEM = "system";

    /** 消息角色：用户 */
    public static final String ROLE_USER = "user";

    /** 消息角色：助手 */
    public static final String ROLE_ASSISTANT = "assistant";

    /** 消息角色：工具 */
    public static final String ROLE_TOOL = "tool";

    // ==================== Object Types ====================

    /** 对象类型：Chat Completion */
    public static final String OBJECT_CHAT_COMPLETION = "chat.completion";

    /** 对象类型：Chat Completion Chunk */
    public static final String OBJECT_CHAT_COMPLETION_CHUNK = "chat.completion.chunk";

    /** 对象类型：Embedding */
    public static final String OBJECT_EMBEDDING = "embedding";

    /** 对象类型：List */
    public static final String OBJECT_LIST = "list";

    /** 对象类型：Model */
    public static final String OBJECT_MODEL = "model";

    /** 对象类型：Response */
    public static final String OBJECT_RESPONSE = "response";

    // ==================== Finish Reasons ====================

    /** 完成原因：正常完成 */
    public static final String FINISH_REASON_STOP = "stop";

    /** 完成原因：工具调用 */
    public static final String FINISH_REASON_TOOL_CALLS = "tool_calls";

    /** 完成原因：达到 token 限制 */
    public static final String FINISH_REASON_LENGTH = "length";

    /** 完成原因：工具执行错误 */
    public static final String FINISH_REASON_TOOL_ERROR = "tool_error";

    // ==================== Request Headers ====================

    /** Header: 覆盖后端模型 */
    public static final String HEADER_X_OPENCLAW_MODEL = "x-openclaw-model";

    /** Header: Agent ID 兼容性覆盖 */
    public static final String HEADER_X_OPENCLAW_AGENT_ID = "x-openclaw-agent-id";

    /** Header: 会话路由 Key */
    public static final String HEADER_X_OPENCLAW_SESSION_KEY = "x-openclaw-session-key";

    /** Header: 入口通道上下文 */
    public static final String HEADER_X_OPENCLAW_MESSAGE_CHANNEL = "x-openclaw-message-channel";

    /** Header: 权限范围声明 */
    public static final String HEADER_X_OPENCLAW_SCOPES = "x-openclaw-scopes";

    /** Header: Webhook Token（旧版兼容） */
    public static final String HEADER_X_OPENCLAW_TOKEN = "x-openclaw-token";

    // ==================== WS Message Types ====================

    /** WS 消息类型：事件 */
    public static final String WS_TYPE_EVENT = "event";

    /** WS 消息类型：请求 */
    public static final String WS_TYPE_REQ = "req";

    /** WS 消息类型：响应 */
    public static final String WS_TYPE_RES = "res";

    // ==================== WS Events ====================

    /** WS 事件：聊天 */
    public static final String WS_EVENT_CHAT = "chat";

    /** WS 事件：心跳 */
    public static final String WS_EVENT_HEARTBEAT = "heartbeat";

    // ==================== Input Item Types ====================

    /** Input Item 类型：消息 */
    public static final String INPUT_TYPE_MESSAGE = "message";

    /** Input Item 类型：函数调用输出 */
    public static final String INPUT_TYPE_FUNCTION_CALL_OUTPUT = "function_call_output";

    /** Input Item 类型：图片输入 */
    public static final String INPUT_TYPE_IMAGE = "input_image";

    /** Input Item 类型：文件输入 */
    public static final String INPUT_TYPE_FILE = "input_file";

    // ==================== Tool Types ====================

    /** 工具类型：函数 */
    public static final String TOOL_TYPE_FUNCTION = "function";

    // ==================== Response Events ====================

    /** Response 事件：已创建 */
    public static final String RESPONSE_EVENT_CREATED = "response.created";

    /** Response 事件：进行中 */
    public static final String RESPONSE_EVENT_IN_PROGRESS = "response.in_progress";

    /** Response 事件：输出项已添加 */
    public static final String RESPONSE_EVENT_OUTPUT_ITEM_ADDED = "response.output_item.added";

    /** Response 事件：内容部分已添加 */
    public static final String RESPONSE_EVENT_CONTENT_PART_ADDED = "response.content_part.added";

    /** Response 事件：输出文本增量 */
    public static final String RESPONSE_EVENT_OUTPUT_TEXT_DELTA = "response.output_text.delta";

    /** Response 事件：输出文本完成 */
    public static final String RESPONSE_EVENT_OUTPUT_TEXT_DONE = "response.output_text.done";

    /** Response 事件：内容部分完成 */
    public static final String RESPONSE_EVENT_CONTENT_PART_DONE = "response.content_part.done";

    /** Response 事件：输出项完成 */
    public static final String RESPONSE_EVENT_OUTPUT_ITEM_DONE = "response.output_item.done";

    /** Response 事件：已完成 */
    public static final String RESPONSE_EVENT_COMPLETED = "response.completed";

    /** Response 事件：失败 */
    public static final String RESPONSE_EVENT_FAILED = "response.failed";

    // ==================== Tool Choice ====================

    /** 工具选择：自动 */
    public static final String TOOL_CHOICE_AUTO = "auto";

    /** 工具选择：无 */
    public static final String TOOL_CHOICE_NONE = "none";

    /** 工具选择：必需 */
    public static final String TOOL_CHOICE_REQUIRED = "required";

    // ==================== Misc ====================

    /** SSE 完成标记 */
    public static final String SSE_DONE = "[DONE]";

    /** 默认 wake 模式 */
    public static final String DEFAULT_WAKE_MODE = "now";

    /** CLI 可执行文件名 */
    public static final String CLI_EXECUTABLE = "openclaw";

    /** WS 线程名称 */
    public static final String WS_THREAD_NAME = "openclaw-ws-challenge";

    // ==================== Agent Model Prefixes ====================

    /** Agent 路由前缀：openclaw */
    public static final String AGENT_PREFIX_OPENCLAW = "openclaw";

    /** Agent 路由前缀：openclaw/ */
    public static final String AGENT_PREFIX_OPENCLAW_SLASH = "openclaw/";

    /** Agent 路由前缀：openclaw: */
    public static final String AGENT_PREFIX_OPENCLAW_COLON = "openclaw:";

    /** Agent 路由前缀：agent: */
    public static final String AGENT_PREFIX_AGENT_COLON = "agent:";

    /** Agent 默认路由 */
    public static final String AGENT_DEFAULT = "openclaw";

    /** Agent 默认路由（稳定别名） */
    public static final String AGENT_DEFAULT_STABLE = "openclaw/default";
}
