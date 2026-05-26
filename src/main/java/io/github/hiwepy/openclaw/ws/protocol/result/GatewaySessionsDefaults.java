package io.github.hiwepy.openclaw.ws.protocol.result;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

/**
 * {@code sessions.list} 响应中的默认模型/思考级别信息。
 */
@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class GatewaySessionsDefaults {

    @JsonProperty("modelProvider")
    private String modelProvider;

    @JsonProperty("model")
    private String model;

    @JsonProperty("contextTokens")
    private Integer contextTokens;

    @JsonProperty("thinkingDefault")
    private String thinkingDefault;
}
