package io.github.hiwepy.openclaw.api.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Tools Invoke API 响应 —— {@code POST /tools/invoke}。
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public record ToolInvokeResult(
        @JsonProperty("ok") Boolean ok,
        @JsonProperty("result") Object result,
        @JsonProperty("error") ErrorDetail error) {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ErrorDetail(
            @JsonProperty("type") String type,
            @JsonProperty("message") String message) {
    }
}
