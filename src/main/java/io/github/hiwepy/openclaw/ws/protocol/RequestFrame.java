package io.github.hiwepy.openclaw.ws.protocol;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

/**
 * 客户端→Gateway RPC 请求帧：{@code { type: "req", id, method, params }}。
 */
public class RequestFrame extends GatewayFrame {

    private final String id;
    private final String method;
    private final Map<String, Object> params;

    @JsonCreator
    public RequestFrame(
            @JsonProperty("type") String type,
            @JsonProperty("id") String id,
            @JsonProperty("method") String method,
            @JsonProperty("params") Map<String, Object> params) {
        super("req");
        this.id = id;
        this.method = method;
        this.params = params;
    }

    public RequestFrame(String id, String method, Map<String, Object> params) {
        super("req");
        this.id = id;
        this.method = method;
        this.params = params;
    }

    public String getId() { return id; }
    public String getMethod() { return method; }
    public Map<String, Object> getParams() { return params; }
}
