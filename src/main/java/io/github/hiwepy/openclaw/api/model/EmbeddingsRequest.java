package io.github.hiwepy.openclaw.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * OpenAI Embeddings API 请求体。
 * <p>
 * 对应 {@code POST /v1/embeddings} 的请求 JSON。
 * 使用与 Chat Completions 相同的 agent 目标 {@code model} 约定：
 * {@code "openclaw/default"} 或 {@code "openclaw/<agentId>"}。
 * 如需指定嵌入模型，请使用 HTTP 头 {@code x-openclaw-model}（如 {@code openai/text-embedding-3-small}）。
 * </p>
 *
 * @see <a href="https://docs.openclaw.ai/gateway/openai-http-api">OpenAI Chat Completions</a>
 */
@Getter
@Setter
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class EmbeddingsRequest {

    /**
     * Agent 目标标识（如 {@code "openclaw/default"}）。
     */
    private String model;

    /**
     * 输入文本（字符串或字符串数组）。
     * <p>支持单个字符串或字符串数组两种格式。</p>
     */
    private Object input;
}
