package io.github.hiwepy.openclaw.ws.protocol.result;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

/**
 * {@code sessions.list} 响应中的单条会话摘要。
 * <p>对齐 {@code GatewaySessionRow}（{@code src/gateway/session-utils.types.ts}）常用字段。</p>
 */
@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class GatewaySessionRow {

    @JsonProperty("key")
    private String key;

    @JsonProperty("sessionId")
    private String sessionId;

    @JsonProperty("kind")
    private String kind;

    @JsonProperty("label")
    private String label;

    @JsonProperty("displayName")
    private String displayName;

    @JsonProperty("derivedTitle")
    private String derivedTitle;

    @JsonProperty("lastMessagePreview")
    private String lastMessagePreview;

    @JsonProperty("updatedAt")
    private Long updatedAt;

    @JsonProperty("modelProvider")
    private String modelProvider;

    @JsonProperty("model")
    private String model;

    @JsonProperty("hasActiveRun")
    private Boolean hasActiveRun;

    @JsonProperty("status")
    private String status;
}
