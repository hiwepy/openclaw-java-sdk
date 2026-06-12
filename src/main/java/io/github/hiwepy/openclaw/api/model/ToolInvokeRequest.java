package io.github.hiwepy.openclaw.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.Map;

/**
 * Tools Invoke API 请求体 —— {@code POST /tools/invoke}。
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ToolInvokeRequest(
        @com.fasterxml.jackson.annotation.JsonProperty("tool") String tool,
        @com.fasterxml.jackson.annotation.JsonProperty("action") String action,
        @com.fasterxml.jackson.annotation.JsonProperty("args") Map<String, Object> args,
        @com.fasterxml.jackson.annotation.JsonProperty("sessionKey") String sessionKey,
        @com.fasterxml.jackson.annotation.JsonProperty("dryRun") Boolean dryRun) {
}
