package io.github.hiwepy.openclaw.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.util.List;
import java.util.Map;

/**
 * OpenAI Chat Completions API 请求体。
 * <p>
 * 对应 {@code POST /v1/chat/completions} 的请求 JSON。
 * </p>
 *
 * <h3>字段语义（重要）</h3>
 * <ul>
 *   <li>{@code agent} - Agent 目标路由（如 {@code "openclaw/default"}）
 *   <li>{@code model} - <b>后端 LLM 模型</b>（如 {@code "gpt-4o"}）
 * </ul>
 *
 * <h3>用法示例</h3>
 * <pre>{@code
 * // 方式1：使用 Builder
 * ChatRequest request = ChatRequest.builder()
 *     .agent("openclaw/default")
 *     .model("gpt-4o")
 *     .messages(List.of(ChatMessage.ofUser("Hello")))
 *     .build();
 *
 * // 方式2：使用 setter
 * ChatRequest request = new ChatRequest();
 * request.setAgent("openclaw/default");
 * request.setModel("gpt-4o");
 * request.setMessages(List.of(ChatMessage.ofUser("Hello")));
 * }</pre>
 *
 * <h3>会话行为</h3>
 * <p>默认每次请求无状态（新会话 key）。若请求包含 {@code user} 字符串，
 * Gateway 会从中派生稳定的 session key，使后续调用共享同一 agent 会话。</p>
 *
 * @see <a href="https://docs.openclaw.ai/gateway/openai-http-api">OpenAI Chat Completions</a>
 */
@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ChatRequest {

    /**
     * Agent 目标标识。
     * <p>使用 {@code "openclaw"}、{@code "openclaw/default"} 或 {@code "openclaw/<agentId>"}。
     * 兼容别名 {@code "openclaw:<agentId>"} 和 {@code "agent:<agentId>"} 仍被接受。</p>
     */
    private String agent;

    /**
     * 后端 LLM 模型标识。
     * <p>对应 OpenAI 的标准 model 字段语义，如 {@code "gpt-4o"}、{@code "claude-3-opus"} 等。
     * 若未指定，使用 Agent 配置的默认模型。</p>
     */
    private String model;

    /**
     * 消息数组。按 OpenAI 标准格式，支持 {@code system}、{@code user}、{@code assistant}、{@code tool} 角色。
     */
    private List<ChatMessage> messages;

    /**
     * 是否启用 SSE 流式响应。
     * <p>为 {@code true} 时，响应 Content-Type 为 {@code text/event-stream}，
     * 每行 {@code data: <json>}，以 {@code data: [DONE]} 结束。</p>
     */
    private Boolean stream;

    /**
     * 流式选项。当 {@code stream} 为 {@code true} 时可设置。
     * <p>若 {@code include_usage} 为 {@code true}，则在 {@code [DONE]} 前发送 usage 统计块。</p>
     */
    private Map<String, Object> streamOptions;

    /**
     * 客户端工具定义数组。
     * <p>每个元素格式：{@code { "type": "function", "function": { "name": "...", "description": "...", "parameters": {...} } }}</p>
     */
    private List<Map<String, Object>> tools;

    /**
     * 工具选择策略。
     * <p>支持：{@code "auto"}、{@code "none"}、{@code "required"}、
     * 或 {@code { "type": "function", "function": { "name": "..." } }}（指定函数）。</p>
     */
    private Object toolChoice;

    /**
     * 用户标识（用于派生稳定的 session key）。
     * <p>推荐使用 {@code conv:<conversationId>} 格式，
     * 同一对话线程复用相同 user 值以共享 agent 会话。</p>
     */
    private String user;

    /**
     * 最大完成 token 数（含推理 token）。
     * <p>优先于 {@code maxTokens}。向上传递到 agent 的 stream-param 通道。</p>
     */
    private Integer maxCompletionTokens;

    /**
     * 最大 token 数（旧版字段，当 {@code maxCompletionTokens} 存在时被忽略）。
     */
    private Integer maxTokens;

    /**
     * 采样温度（0-2）。最佳努力转发到上游 provider。
     */
    private Double temperature;

    /**
     * nucleus 采样参数（0-1）。最佳努力转发到上游 provider。
     */
    private Double topP;

    /**
     * 频率惩罚（-2.0 到 2.0）。超出范围返回 {@code 400 invalid_request_error}。
     */
    private Double frequencyPenalty;

    /**
     * 存在惩罚（-2.0 到 2.0）。超出范围返回 {@code 400 invalid_request_error}。
     */
    private Double presencePenalty;

    /**
     * 随机种子（整数）。最佳努力转发到上游 provider。
     */
    private Integer seed;

    /**
     * 响应格式控制。
     * <p>使用 {@link ResponseFormat} 构建，支持三种模式：
     * {@link ResponseFormat#jsonObject()} 强制 JSON 输出（最常用）、
     * {@link ResponseFormatType#JSON_SCHEMA} 结构化输出、
     * {@link ResponseFormatType#TEXT} 默认文本。</p>
     *
     * @see ResponseFormat
     */
    private ResponseFormat responseFormat;

    /**
     * 停止序列（字符串或最多 4 个字符串的数组）。
     * <p>超过 4 个序列或非字符串/空条目返回 {@code 400 invalid_request_error}。</p>
     */
    private Object stop;
}
