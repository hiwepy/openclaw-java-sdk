package io.github.hiwepy.openclaw.api.sse;

import io.github.hiwepy.openclaw.api.model.ChatChunk;
import io.github.hiwepy.openclaw.api.model.ChatChunk.DeltaChoice;
import io.github.hiwepy.openclaw.api.model.ChatChunk.DeltaMessage;
import io.github.hiwepy.openclaw.api.model.ChatMessage;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class SseEventAccumulator {

    private String id;
    private String model;
    private String role;
    private final StringBuilder contentBuilder = new StringBuilder();
    private final Map<Integer, ToolPart> toolParts = new LinkedHashMap<>();
    private String finishReason;

    public void merge(ChatChunk chunk) {
        if (chunk.getId() != null) {
            this.id = chunk.getId();
        }
        if (chunk.getModel() != null) {
            this.model = chunk.getModel();
        }
        if (chunk.getChoices() == null || chunk.getChoices().isEmpty()) {
            return;
        }

        DeltaChoice choice = chunk.getChoices().get(0);
        DeltaMessage delta = choice.getDelta();
        if (choice.getFinishReason() != null) {
            this.finishReason = choice.getFinishReason();
        }
        if (delta == null) {
            return;
        }

        if (delta.getRole() != null) {
            this.role = delta.getRole();
        }
        if (delta.getContent() != null) {
            contentBuilder.append(delta.getContent());
        }

        if (delta.getToolCalls() != null) {
            for (int i = 0; i < delta.getToolCalls().size(); i++) {
                ChatMessage.ToolCall tc = delta.getToolCalls().get(i);
                ToolPart tp = toolParts.computeIfAbsent(i, k -> new ToolPart());
                tp.merge(tc);
            }
        }
    }

    public ChatChunk getAccumulated() {
        ChatChunk result = new ChatChunk();
        result.setId(id);
        result.setModel(model);

        DeltaMessage delta = new DeltaMessage();
        delta.setRole(role);
        delta.setContent(contentBuilder.length() > 0 ? contentBuilder.toString() : null);

        if (!toolParts.isEmpty()) {
            List<ChatMessage.ToolCall> merged = new ArrayList<>();
            for (ToolPart tp : toolParts.values()) {
                merged.add(tp.build());
            }
            delta.setToolCalls(merged);
        }

        DeltaChoice choice = new DeltaChoice();
        choice.setIndex(0);
        choice.setDelta(delta);
        choice.setFinishReason(finishReason);

        result.setChoices(java.util.Collections.singletonList(choice));
        return result;
    }

    public boolean isToolCall() { return "tool_calls".equals(finishReason); }
    public boolean isComplete() { return finishReason != null; }
    public String getAccumulatedContent() { return contentBuilder.toString(); }

    public void reset() {
        id = null; model = null; role = null;
        contentBuilder.setLength(0);
        toolParts.clear();
        finishReason = null;
    }

    private static class ToolPart {
        private String id, type, name;
        private final StringBuilder arguments = new StringBuilder();

        void merge(ChatMessage.ToolCall tc) {
            if (tc.getId() != null) {
                this.id = tc.getId();
            }
            if (tc.getType() != null) {
                this.type = tc.getType();
            }
            if (tc.getFunction() != null) {
                if (tc.getFunction().getName() != null) this.name = tc.getFunction().getName();
                if (tc.getFunction().getArguments() != null) this.arguments.append(tc.getFunction().getArguments());
            }
        }

        ChatMessage.ToolCall build() {
            ChatMessage.FunctionCall fn = new ChatMessage.FunctionCall();
            fn.setName(name);
            fn.setArguments(arguments.toString());

            ChatMessage.ToolCall tc = new ChatMessage.ToolCall();
            tc.setId(id);
            tc.setType(type != null ? type : "function");
            tc.setFunction(fn);
            return tc;
        }
    }
}
