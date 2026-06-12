package io.github.hiwepy.openclaw.api.sse;

import io.github.hiwepy.openclaw.api.model.ChatCompletionChunk;
import io.github.hiwepy.openclaw.api.model.ChatCompletionChunk.DeltaChoice;
import io.github.hiwepy.openclaw.api.model.ChatCompletionChunk.DeltaMessage;
import io.github.hiwepy.openclaw.api.model.ChatCompletionMessage;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Accumulates SSE streaming delta chunks into a complete response.
 *
 * <pre>{@code
 * SseEventAccumulator acc = new SseEventAccumulator();
 * client.chatCompletionStream(req, new SseEventHandler() {
 *     public void onEvent(SseEvent event) {
 *         ChatCompletionChunk chunk = (ChatCompletionChunk) event.parsed();
 *         if (chunk != null) acc.merge(chunk);
 *     }
 *     public void onComplete() {
 *         ChatCompletionChunk full = acc.getAccumulated();
 *     }
 *     public void onError(Throwable error) { ... }
 * });
 * }</pre>
 */
public class SseEventAccumulator {

    private String id;
    private String object;
    private Long created;
    private String model;
    private String role;
    private final StringBuilder contentBuilder = new StringBuilder();
    private final Map<Integer, ToolCallAccumulator> toolCallsByIndex = new LinkedHashMap<>();
    private String finishReason;

    public void merge(ChatCompletionChunk chunk) {
        if (chunk.id() != null) this.id = chunk.id();
        if (chunk.object() != null) this.object = chunk.object();
        if (chunk.created() != null) this.created = chunk.created();
        if (chunk.model() != null) this.model = chunk.model();

        if (chunk.choices() == null || chunk.choices().isEmpty()) return;

        DeltaChoice choice = chunk.choices().get(0);
        DeltaMessage delta = choice.delta();
        if (choice.finishReason() != null) this.finishReason = choice.finishReason();
        if (delta == null) return;

        if (delta.role() != null) this.role = delta.role();
        if (delta.content() != null) contentBuilder.append(delta.content());

        if (delta.toolCalls() != null) {
            for (int i = 0; i < delta.toolCalls().size(); i++) {
                ChatCompletionMessage.ToolCall tc = delta.toolCalls().get(i);
                ToolCallAccumulator tca = toolCallsByIndex.computeIfAbsent(i,
                        k -> new ToolCallAccumulator());
                tca.merge(tc);
            }
        }
    }

    public ChatCompletionChunk getAccumulated() {
        List<ChatCompletionMessage.ToolCall> mergedTools = null;
        if (!toolCallsByIndex.isEmpty()) {
            mergedTools = new ArrayList<>();
            for (ToolCallAccumulator tca : toolCallsByIndex.values()) {
                mergedTools.add(tca.build());
            }
        }

        DeltaMessage delta = new DeltaMessage(role,
                contentBuilder.length() > 0 ? contentBuilder.toString() : null,
                mergedTools);

        DeltaChoice choice = new DeltaChoice(0, delta, finishReason);

        return new ChatCompletionChunk(id, object, created, model,
                List.of(choice), null);
    }

    public boolean isToolCall() {
        return "tool_calls".equals(finishReason);
    }

    public boolean isComplete() {
        return finishReason != null;
    }

    public String getAccumulatedContent() {
        return contentBuilder.toString();
    }

    public void reset() {
        id = null;
        object = null;
        created = null;
        model = null;
        role = null;
        contentBuilder.setLength(0);
        toolCallsByIndex.clear();
        finishReason = null;
    }

    private static class ToolCallAccumulator {
        private String id;
        private String type;
        private String name;
        private final StringBuilder arguments = new StringBuilder();

        void merge(ChatCompletionMessage.ToolCall tc) {
            if (tc.id() != null) this.id = tc.id();
            if (tc.type() != null) this.type = tc.type();
            if (tc.function() != null) {
                if (tc.function().name() != null) this.name = tc.function().name();
                if (tc.function().arguments() != null) this.arguments.append(tc.function().arguments());
            }
        }

        ChatCompletionMessage.ToolCall build() {
            return new ChatCompletionMessage.ToolCall(id, type != null ? type : "function",
                    new ChatCompletionMessage.FunctionCall(name, arguments.toString()), null);
        }
    }
}
