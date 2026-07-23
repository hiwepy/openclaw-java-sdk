package io.github.easy4j.openclaw.ws.protocol.result;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.util.Collections;
import java.util.List;

/**
 * {@code chat.history} RPC 成功响应体。
 */
@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class ChatHistoryResult {

    @JsonProperty("sessionKey")
    private String sessionKey;

    @JsonProperty("sessionId")
    private String sessionId;

    @JsonProperty("messages")
    private List<Object> messages;

    @JsonProperty("thinkingLevel")
    private String thinkingLevel;

    @JsonProperty("fastMode")
    private Boolean fastMode;

    @JsonProperty("verboseLevel")
    private String verboseLevel;

    /**
     * @return 非 null 的消息列表（元素结构由 Gateway 定义，可用 Jackson 二次解析）
     */
    public List<Object> getMessages() {
        return messages != null ? messages : Collections.emptyList();
    }
}
