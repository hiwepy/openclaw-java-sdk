package io.github.hiwepy.openclaw.api.sse;

import io.github.hiwepy.openclaw.api.model.ChatCompletionChunk;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * Convenience wrapper around {@link SseEventHandler} + {@link SseEventAccumulator}
 * that collects streaming delta chunks and completes a {@link CompletableFuture}
 * with the fully merged {@link ChatCompletionChunk}.
 *
 * <pre>{@code
 * StreamingChatResponse stream = client.chatCompletionStream(req);
 * ChatCompletionChunk full = stream.get();  // blocks
 * // or:
 * stream.onDelta(delta -> System.out.print(delta));
 * stream.whenComplete(full -> System.out.println("done: " + full.getAccumulatedContent()));
 * }</pre>
 */
public class StreamingChatResponse extends CompletableFuture<ChatCompletionChunk>
        implements SseEventHandler {

    private final SseEventAccumulator accumulator = new SseEventAccumulator();
    private Consumer<String> deltaConsumer;

    /** Called per delta text chunk. */
    public StreamingChatResponse onDelta(Consumer<String> deltaConsumer) {
        this.deltaConsumer = deltaConsumer;
        return this;
    }

    @Override
    public void onEvent(SseEvent event) {
        if (event.isTerminal()) return;
        ChatCompletionChunk chunk = event.parsed() instanceof ChatCompletionChunk c ? c : null;
        if (chunk == null) return;
        accumulator.merge(chunk);
        if (deltaConsumer != null && chunk.choices() != null && !chunk.choices().isEmpty()) {
            ChatCompletionChunk.DeltaMessage delta = chunk.choices().get(0).delta();
            if (delta != null && delta.content() != null) {
                deltaConsumer.accept(delta.content());
            }
        }
    }

    @Override
    public void onComplete() {
        complete(accumulator.getAccumulated());
    }

    @Override
    public void onError(Throwable error) {
        completeExceptionally(error);
    }

    public SseEventAccumulator getAccumulator() {
        return accumulator;
    }
}
