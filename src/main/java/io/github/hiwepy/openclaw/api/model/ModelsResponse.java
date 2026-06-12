package io.github.hiwepy.openclaw.api.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * OpenAI Models API 响应。
 * <p>
 * 对应 {@code GET /v1/models} 返回的 JSON。
 * 返回的是 OpenClaw agent 目标列表（如 {@code openclaw}、{@code openclaw/default}、
 * {@code openclaw/<agentId>}），而非原始 provider 模型目录。
 * </p>
 *
 * <p>子 agent 不会出现在此列表中（它们是内部执行拓扑，不作为伪模型暴露）。</p>
 *
 * @see <a href="https://docs.openclaw.ai/gateway/openai-http-api">OpenAI Chat Completions</a>
 */
@Getter
@Setter
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ModelsResponse {

    /** 对象类型，固定为 {@code "list"}。 */
    private String object;

    /** 模型列表。 */
    private List<ModelData> data;

    /**
     * 单个模型/agent 目标信息。
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ModelData {
        /** 模型标识，如 {@code "openclaw"}、{@code "openclaw/default"}、{@code "openclaw/research"}。 */
        private String id;
        /** 对象类型，固定为 {@code "model"}。 */
        private String object;
        /** 创建时间戳。 */
        private Long created;
        /** 拥有者。 */
        @JsonProperty("owned_by")
        private String ownedBy;
    }
}
