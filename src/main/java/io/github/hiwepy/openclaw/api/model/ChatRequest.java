package io.github.hiwepy.openclaw.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.Map;

/**
 * OpenAI Chat Completions API 请求体。
 * <p>
 * 对应 {@code POST /v1/chat/completions} 的请求 JSON。
 * OpenClaw Gateway 将 {@code model} 字段解释为 <b>agent 目标</b>，而非原始模型 ID：
 * <ul>
 *   <li>{@code "openclaw"} 或 {@code "openclaw/default"} - 路由到默认 agent</li>
 *   <li>{@code "openclaw/<agentId>"} - 路由到指定 agent</li>
 * </ul>
 * 如需覆盖后端模型，请使用 HTTP 头 {@code x-openclaw-model}（如 {@code openai/gpt-5.4}）。
 * </p>
 *
 * <h3>会话行为</h3>
 * <p>默认每次请求无状态（新会话 key）。若请求包含 {@code user} 字符串，
 * Gateway 会从中派生稳定的 session key，使后续调用共享同一 agent 会话。</p>
 *
 * @see <a href="https://docs.openclaw.ai/gateway/openai-http-api">OpenAI Chat Completions</a>
 */
@Getter
@Setter
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ChatRequest {

    /**
     * Agent 目标标识。
     * <p>使用 {@code "openclaw"}、{@code "openclaw/default"} 或 {@code "openclaw/<agentId>"}。
     * 兼容别名 {@code "openclaw:<agentId>"} 和 {@code "agent:<agentId>"} 仍被接受。</p>
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
     * 停止序列（字符串或最多 4 个字符串的数组）。
     * <p>超过 4 个序列或非字符串/空条目返回 {@code 400 invalid_request_error}。</p>
     */
    private Object stop;
}
