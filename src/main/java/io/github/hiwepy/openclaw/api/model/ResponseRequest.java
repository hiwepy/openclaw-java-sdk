package io.github.hiwepy.openclaw.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.Map;

/**
 * OpenResponses API 请求体。
 * <p>
 * 对应 {@code POST /v1/responses} 的请求 JSON。
 * OpenClaw Gateway 将 {@code model} 字段解释为 agent 目标，与 OpenAI Chat Completions 一致。
 * </p>
 *
 * <h3>请求格式</h3>
 * <p>{@code input} 可以是：</p>
 * <ul>
 *   <li>单个字符串 - 作为用户消息发送</li>
 *   <li>Item 对象数组 - 支持消息、函数调用输出、图片、文件等</li>
 * </ul>
 *
 * <h3>会话行为</h3>
 * <p>默认每次请求无状态（新会话 key）。若请求包含 {@code user} 字符串，
 * Gateway 会从中派生稳定的 session key。</p>
 *
 * <h3>支持的字段</h3>
 * <ul>
 *   <li>{@code input} - 字符串或 Item 对象数组</li>
 *   <li>{@code instructions} - 合并到系统提示</li>
 *   <li>{@code tools} - 客户端工具定义</li>
 *   <li>{@code toolChoice} - 工具选择策略</li>
 *   <li>{@code stream} - 启用 SSE 流式</li>
 *   <li>{@code maxOutputTokens} - 最大输出 token（最佳努力）</li>
 *   <li>{@code temperature} - 采样温度</li>
 *   <li>{@code topP} - nucleus 采样</li>
 *   <li>{@code user} - 稳定 session 路由</li>
 *   <li>{@code previousResponseId} - 复用先前响应的 session</li>
 * </ul>
 *
 * @see <a href="https://docs.openclaw.ai/gateway/openresponses-http-api">OpenResponses API</a>
 */
@Getter
@Setter
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ResponseRequest {

    /**
     * Agent 目标标识。
     * <p>使用 {@code "openclaw"}、{@code "openclaw/default"} 或 {@code "openclaw/<agentId>"}。</p>
     */
    private String model;

    /**
     * 输入内容。
     * <p>可以是单个字符串（作为用户消息）或 Item 对象数组。</p>
     *
     * <h4>Item 类型</h4>
     * <ul>
     *   <li>{@code message} - 消息对象，角色：{@code system}、{@code developer}、{@code user}、{@code assistant}
     *     <ul>
     *       <li>{@code system} 和 {@code developer} 追加到系统提示</li>
     *       <li>最新的 {@code user} 或 {@code function_call_output} 成为"当前消息"</li>
     *     </ul>
     *   </li>
     *   <li>{@code function_call_output} - 工具调用结果
     *     <pre>{@code
     *     { "type": "function_call_output", "call_id": "call_123", "output": "{\"temperature\": \"72F\"}" }
     *     }</pre>
     *   </li>
     *   <li>{@code input_image} - 图片输入（base64 或 URL，最大 10MB）
     *     <ul>
     *       <li>支持 MIME：{@code image/jpeg}、{@code image/png}、{@code image/gif}、{@code image/webp}、{@code image/heic}、{@code image/heif}</li>
     *     </ul>
     *   </li>
     *   <li>{@code input_file} - 文件输入（base64 或 URL，最大 5MB）
     *     <ul>
     *       <li>支持 MIME：{@code text/plain}、{@code text/markdown}、{@code text/html}、{@code text/csv}、{@code application/json}、{@code application/pdf}</li>
     *       <li>文件内容解码后添加到系统提示（非用户消息），并标记为不受信任的外部内容</li>
     *     </ul>
     *   </li>
     * </ul>
     */
    private Object input;

    /**
     * 系统指令（合并到系统提示）。
     */
    private String instructions;

    /**
     * 客户端工具定义。
     * <p>格式：{@code [{ type: "function", name: "...", description: "...", parameters: {...} }]}</p>
     * <p>若 agent 决定调用工具，响应返回 {@code function_call} 输出项。
     * 客户端应发送包含 {@code function_call_output} 的后续请求以继续。</p>
     */
    private List<Map<String, Object>> tools;

    /**
     * 工具选择策略。
     * <p>支持：{@code "auto"}、{@code "none"}、{@code "required"}、
     * 或 {@code { "type": "function", "name": "..." }}。</p>
     */
    private Object toolChoice;

    /**
     * 是否启用 SSE 流式响应。
     * <p>流式事件类型：</p>
     * <ul>
     *   <li>{@code response.created}</li>
     *   <li>{@code response.in_progress}</li>
     *   <li>{@code response.output_item.added}</li>
     *   <li>{@code response.content_part.added}</li>
     *   <li>{@code response.output_text.delta}</li>
     *   <li>{@code response.output_text.done}</li>
     *   <li>{@code response.content_part.done}</li>
     *   <li>{@code response.output_item.done}</li>
     *   <li>{@code response.completed}</li>
     *   <li>{@code response.failed}（错误时）</li>
     * </ul>
     * <p>流以 {@code data: [DONE]} 结束。</p>
     */
    private Boolean stream;

    /**
     * 最大输出 token 数（最佳努力，取决于 provider）。
     */
    private Integer maxOutputTokens;

    /**
     * 采样温度（最佳努力转发到 provider）。
     */
    private Double temperature;

    /**
     * nucleus 采样参数（最佳努力转发到 provider）。
     */
    private Double topP;

    /**
     * 用户标识（用于派生稳定的 session key）。
     */
    private String user;

    /**
     * 先前响应 ID。
     * <p>OpenClaw 在相同 agent/user/requested-session scope 内复用先前响应的 session。</p>
     */
    private String previousResponseId;
}
