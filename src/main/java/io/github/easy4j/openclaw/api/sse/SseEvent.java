package io.github.easy4j.openclaw.api.sse;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class SseEvent {
    @com.fasterxml.jackson.annotation.JsonProperty("event") private String event;
    @com.fasterxml.jackson.annotation.JsonProperty("data") private String data;
    @com.fasterxml.jackson.annotation.JsonProperty("done") private boolean done;
    @com.fasterxml.jackson.annotation.JsonProperty("parsed") private Object parsed;

    public boolean isTerminal() { return done; }
    public static SseEvent terminal() { SseEvent e = new SseEvent(); e.done = true; return e; }
    public static SseEvent data(String data) { SseEvent e = new SseEvent(); e.data = data; return e; }
    public static SseEvent of(String event, String data) { SseEvent e = new SseEvent(); e.event = event; e.data = data; return e; }
}
