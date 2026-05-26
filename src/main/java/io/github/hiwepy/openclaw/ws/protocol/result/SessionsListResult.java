package io.github.hiwepy.openclaw.ws.protocol.result;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.util.Collections;
import java.util.List;

/**
 * {@code sessions.list} RPC 成功响应体。
 */
@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class SessionsListResult {

    @JsonProperty("ts")
    private long ts;

    @JsonProperty("path")
    private String path;

    @JsonProperty("count")
    private int count;

    @JsonProperty("totalCount")
    private Integer totalCount;

    @JsonProperty("limitApplied")
    private Integer limitApplied;

    @JsonProperty("hasMore")
    private Boolean hasMore;

    @JsonProperty("defaults")
    private GatewaySessionsDefaults defaults;

    @JsonProperty("sessions")
    private List<GatewaySessionRow> sessions;

    /**
     * @return 非 null 的会话列表视图
     */
    public List<GatewaySessionRow> getSessions() {
        return sessions != null ? sessions : Collections.emptyList();
    }
}
