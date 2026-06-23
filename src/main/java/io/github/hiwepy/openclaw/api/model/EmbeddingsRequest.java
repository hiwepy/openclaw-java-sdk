package io.github.hiwepy.openclaw.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * OpenAI Embeddings API 请求体。
 * <p>
 * 对应 {@code POST /v1/embeddings} 的请求 JSON。
 * </p>
 *
 * <h3>字段语义</h3>
 * <ul>
 *   <li>{@code agent} - Agent 目标路由（如 {@code "openclaw/default"}）
 *   <li>{@code model} - 后端嵌入模型（如 {@code "openai/text-embedding-3-small"}）
 * </ul>
 *
 * @see <a href="https://docs.openclaw.ai/gateway/openai-http-api">OpenAI Chat Completions</a>
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class EmbeddingsRequest {

    /**
     * Agent 目标标识。
     * <p>使用 {@code "openclaw"}、{@code "openclaw/default"} 或 {@code "openclaw/<agentId>"}。</p>
     */
    private String agent;

    /**
     * 后端嵌入模型标识。
     * <p>如 {@code "openai/text-embedding-3-small"}。
     * 若未指定，使用 Agent 配置的默认嵌入模型。</p>
     */
    private String model;

    /**
     * 输入文本（字符串或字符串数组）。
     * <p>支持单个字符串或字符串数组两种格式。</p>
     */
    private Object input;
}
