package io.github.easy4j.openclaw.ws.protocol.result;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Getter;

/**
 * {@code config.get} RPC 成功响应体（配置快照，字段较多）。
 * <p>常用顶层字段已建模；完整结构请使用 {@link #getSnapshot()} 访问。</p>
 */
@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class ConfigGetResult {

    @JsonProperty("hash")
    private String hash;

    @JsonProperty("valid")
    private Boolean valid;

    @JsonProperty("config")
    private JsonNode config;

    @JsonProperty("uiHints")
    private JsonNode uiHints;

    @JsonProperty("path")
    private String path;
}
