package io.github.hiwepy.openclaw.api.sse;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * SSE（Server-Sent Events）事件。
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public record SseEvent(
        @com.fasterxml.jackson.annotation.JsonProperty("event") String event,
        @com.fasterxml.jackson.annotation.JsonProperty("data") String data,
        @com.fasterxml.jackson.annotation.JsonProperty("done") boolean done,
        @com.fasterxml.jackson.annotation.JsonProperty("parsed") Object parsed) {

    public static SseEvent terminal() {
        return new SseEvent(null, null, true, null);
    }

    public static SseEvent data(String data) {
        return new SseEvent(null, data, false, null);
    }

    public static SseEvent of(String event, String data) {
        return new SseEvent(event, data, false, null);
    }

    public SseEvent withParsed(Object parsed) {
        return new SseEvent(event, data, done, parsed);
    }

    public boolean isTerminal() {
        return done;
    }
}
