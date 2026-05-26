package io.github.hiwepy.openclaw.ws.protocol.result;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

/**
 * {@code sessions.send} RPC 成功响应体（由 {@code chat.send} 确认载荷扩展而来）。
 */
@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class SessionsSendResult {

    @JsonProperty("runId")
    private String runId;

    @JsonProperty("messageSeq")
    private Integer messageSeq;

    @JsonProperty("interruptedActiveRun")
    private Boolean interruptedActiveRun;
}
