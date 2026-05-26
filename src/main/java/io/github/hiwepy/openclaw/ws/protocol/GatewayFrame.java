package io.github.hiwepy.openclaw.ws.protocol;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * Gateway WebSocket 帧的判别联合类型。
 * <p>三种帧：{@code req}（客户端→Gateway）、{@code res}（Gateway→客户端）、{@code event}（Gateway→客户端推送）。</p>
 *
 * @see RequestFrame
 * @see ResponseFrame
 * @see EventFrame
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = RequestFrame.class, name = "req"),
        @JsonSubTypes.Type(value = ResponseFrame.class, name = "res"),
        @JsonSubTypes.Type(value = EventFrame.class, name = "event"),
})
public abstract class GatewayFrame {

    private final String type;

    protected GatewayFrame(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }
}
