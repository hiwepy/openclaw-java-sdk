package io.github.hiwepy.openclaw.api.sse;

import io.github.hiwepy.openclaw.api.model.ChatChunk;
import lombok.Getter;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class StreamingChatResponse extends CompletableFuture<ChatChunk>
        implements SseEventHandler {

    @Getter
    private final SseEventAccumulator accumulator = new SseEventAccumulator();
    private Consumer<String> deltaConsumer;

    public StreamingChatResponse onDelta(Consumer<String> deltaConsumer) {
        this.deltaConsumer = deltaConsumer;
        return this;
    }

    @Override
    public void onEvent(SseEvent event) {
        if (event.isTerminal()) {
            return;
        }
        Object p = event.getParsed(); ChatChunk chunk = p instanceof ChatChunk ? (ChatChunk) p : null;
        if (chunk == null) {
            return;
        }
        accumulator.merge(chunk);
        if (deltaConsumer != null && chunk.getChoices() != null && !chunk.getChoices().isEmpty()) {
            ChatChunk.DeltaChoice choice = chunk.getChoices().get(0);
            ChatChunk.DeltaMessage delta = choice.getDelta();
            if (delta != null && delta.getContent() != null) {
                deltaConsumer.accept(delta.getContent());
            }
        }
    }

    @Override
    public void onComplete() { complete(accumulator.getAccumulated()); }

    @Override
    public void onError(Throwable error) { completeExceptionally(error); }

}
