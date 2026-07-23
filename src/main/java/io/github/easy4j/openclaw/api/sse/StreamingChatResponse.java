package io.github.easy4j.openclaw.api.sse;

import io.github.easy4j.openclaw.api.model.ChatChunk;
import io.github.easy4j.openclaw.api.model.ChatMessage;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * SSE 流式响应封装。
 * <p>
 * 提供流畅的 API 用于注册增量回调，支持：
 * </p>
 * <ul>
 *   <li>{@code onDelta} - 文本增量（类似 Spring AI 的 {@code Flux<ChatResponse>}）</li>
 *   <li>{@code onChunk} - 原始 chunk（完整的 SSE 数据块）</li>
 *   <li>{@code onToolCall} - 工具调用增量</li>
 *   <li>{@code onComplete} - 流结束</li>
 *   <li>{@code onError} - 错误处理</li>
 * </ul>
 *
 * <h3>用法示例</h3>
 * <pre>{@code
 * // 方式 1：链式调用（推荐）
 * StreamingChatResponse stream = client.chatCompletionStream(request);
 * stream.onDelta(delta -> System.out.print(delta))
 *       .onComplete(fullText -> System.out.println("\\n完成: " + fullText))
 *       .onError(error -> error.printStackTrace());
 *
 * // 等待完整结果
 * ChatChunk result = stream.get();
 *
 * // 方式 2：Builder 模式
 * StreamingChatResponse stream2 = client.chatCompletionStream(request,
 *     StreamingChatResponse.builder()
 *         .onDelta(delta -> System.out.print(delta))
 *         .onChunk(chunk -> accumulate(chunk))
 *         .onComplete(accumulator::get)
 *         .onError(System.err::println)
 *         .build()
 * );
 * }</pre>
 *
 * @see SseEventHandler
 * @see <a href="https://docs.openclaw.ai/gateway/openai-http-api#streaming-sse">Streaming SSE</a>
 */
@Getter
public class StreamingChatResponse extends CompletableFuture<ChatChunk>
        implements SseEventHandler {

    private final SseEventAccumulator accumulator = new SseEventAccumulator();

    @Setter
    private Consumer<String> deltaConsumer;

    private Consumer<ChatChunk> chunkConsumer;
    private Consumer<List<ChatMessage.ToolCall>> toolCallConsumer;
    private Consumer<String> completeConsumer;
    private Consumer<Throwable> errorConsumer;

    /**
     * 创建 Builder。
     */
    public static Builder builder() {
        return new Builder();
    }

    // ==================== 回调注册 ====================

    /**
     * 注册文本增量回调。
     * <p>
     * 每次收到 delta content 时触发。
     * </p>
     *
     * @param callback 回调，参数为增量文本
     * @return this
     */
    public StreamingChatResponse onDelta(Consumer<String> callback) {
        this.deltaConsumer = callback;
        return this;
    }

    /**
     * 注册原始 chunk 回调。
     * <p>
     * 每次收到完整的 SSE 数据块时触发，包括所有字段。
     * </p>
     *
     * @param callback 回调，参数为完整的 {@link ChatChunk}
     * @return this
     */
    public StreamingChatResponse onChunk(Consumer<ChatChunk> callback) {
        this.chunkConsumer = callback;
        return this;
    }

    /**
     * 注册工具调用回调。
     * <p>
     * 当流中出现工具调用时触发，完整收集后调用。
     * </p>
     *
     * @param callback 回调，参数为工具调用列表
     * @return this
     */
    public StreamingChatResponse onToolCall(Consumer<List<ChatMessage.ToolCall>> callback) {
        this.toolCallConsumer = callback;
        return this;
    }

    /**
     * 注册流完成回调。
     *
     * @param callback 回调，参数为完整文本
     * @return this
     */
    public StreamingChatResponse onComplete(Consumer<String> callback) {
        this.completeConsumer = callback;
        return this;
    }

    /**
     * 注册错误回调。
     *
     * @param callback 回调
     * @return this
     */
    public StreamingChatResponse onError(Consumer<Throwable> callback) {
        this.errorConsumer = callback;
        return this;
    }

    // ==================== SseEventHandler 实现 ====================

    @Override
    public void onEvent(SseEvent event) {
        if (event.isTerminal()) {
            return;
        }
        Object p = event.getParsed();
        if (!(p instanceof ChatChunk)) {
            return;
        }
        final ChatChunk chunk = (ChatChunk) p;

        // 触发 chunk 回调
        if (chunkConsumer != null) {
            chunkConsumer.accept(chunk);
        }

        // 合并到累加器
        accumulator.merge(chunk);

        // 提取 delta
        if (chunk.getChoices() != null && !chunk.getChoices().isEmpty()) {
            ChatChunk.DeltaChoice choice = chunk.getChoices().get(0);
            ChatChunk.DeltaMessage delta = choice.getDelta();

            if (delta != null) {
                // 文本增量
                if (delta.getContent() != null && deltaConsumer != null) {
                    deltaConsumer.accept(delta.getContent());
                }

                // 工具调用增量
                if (delta.getToolCalls() != null && !delta.getToolCalls().isEmpty() && toolCallConsumer != null) {
                    // 直接传递原始 toolCalls
                    toolCallConsumer.accept(delta.getToolCalls());
                }
            }
        }
    }

    @Override
    public void onComplete() {
        ChatChunk result = accumulator.getAccumulated();
        complete(result);
        if (completeConsumer != null) {
            String fullText = result != null && result.getChoices() != null && !result.getChoices().isEmpty()
                ? result.getChoices().get(0).getDelta().getContent()
                : "";
            completeConsumer.accept(fullText);
        }
    }

    @Override
    public void onError(Throwable error) {
        completeExceptionally(error);
        if (errorConsumer != null) {
            errorConsumer.accept(error);
        }
    }

    // ==================== Builder ====================

    public static class Builder {
        private Consumer<String> deltaConsumer;
        private Consumer<ChatChunk> chunkConsumer;
        private Consumer<List<ChatMessage.ToolCall>> toolCallConsumer;
        private Consumer<String> completeConsumer;
        private Consumer<Throwable> errorConsumer;

        public Builder onDelta(Consumer<String> callback) {
            this.deltaConsumer = callback;
            return this;
        }

        public Builder onChunk(Consumer<ChatChunk> callback) {
            this.chunkConsumer = callback;
            return this;
        }

        public Builder onToolCall(Consumer<List<ChatMessage.ToolCall>> callback) {
            this.toolCallConsumer = callback;
            return this;
        }

        public Builder onComplete(Consumer<String> callback) {
            this.completeConsumer = callback;
            return this;
        }

        public Builder onError(Consumer<Throwable> callback) {
            this.errorConsumer = callback;
            return this;
        }

        public StreamingChatResponse build() {
            StreamingChatResponse response = new StreamingChatResponse();
            response.deltaConsumer = this.deltaConsumer;
            response.chunkConsumer = this.chunkConsumer;
            response.toolCallConsumer = this.toolCallConsumer;
            response.completeConsumer = this.completeConsumer;
            response.errorConsumer = this.errorConsumer;
            return response;
        }
    }
}
