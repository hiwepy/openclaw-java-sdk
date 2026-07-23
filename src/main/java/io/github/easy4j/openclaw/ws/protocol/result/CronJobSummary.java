package io.github.easy4j.openclaw.ws.protocol.result;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

/**
 * {@code cron.list} 响应中的单条 cron 任务摘要。
 */
@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class CronJobSummary {

    @JsonProperty("id")
    private String id;

    @JsonProperty("name")
    private String name;

    @JsonProperty("enabled")
    private Boolean enabled;

    @JsonProperty("agentId")
    private String agentId;

    @JsonProperty("updatedAtMs")
    private Long updatedAtMs;

    @JsonProperty("nextRunAtMs")
    private Long nextRunAtMs;
}
