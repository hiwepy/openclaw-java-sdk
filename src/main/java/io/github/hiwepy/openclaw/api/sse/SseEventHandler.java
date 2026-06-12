package io.github.hiwepy.openclaw.api.sse;

/**
 * SSE 流式事件处理器。
 * <p>
 * 用于处理 Gateway OpenAI 兼容端点和 OpenResponses 端点的 SSE 流式响应。
 * </p>
 *
 * <h3>Chat Completions 流式用法</h3>
 * <pre>{@code
 * client.chatCompletionStream(request, headers, new SseEventHandler() {
 *     public void onEvent(SseEvent event) {
 *         ChatCompletionChunk chunk = (ChatCompletionChunk) event.getParsed();
 *         if (chunk != null && chunk.getChoices() != null) {
 *             chunk.getChoices().forEach(c -> {
 *                 if (c.getDelta() != null && c.getDelta().getContent() != null) {
 *                     System.out.print(c.getDelta().getContent());
 *                 }
 *             });
 *         }
 *     }
 *     public void onComplete() { System.out.println("\n[完成]"); }
 *     public void onError(Throwable error) { error.printStackTrace(); }
 * });
 * }</pre>
 *
 * <h3>OpenResponses 流式用法</h3>
 * <pre>{@code
 * client.createResponseStream(request, headers, new SseEventHandler() {
 *     public void onEvent(SseEvent event) {
 *         System.out.println("[" + event.getEvent() + "] " + event.getData());
 *     }
 *     public void onComplete() { System.out.println("[完成]"); }
 *     public void onError(Throwable error) { error.printStackTrace(); }
 * });
 * }</pre>
 *
 * @see SseEvent
 * @see <a href="https://docs.openclaw.ai/gateway/openai-http-api">OpenAI Chat Completions</a>
 * @see <a href="https://docs.openclaw.ai/gateway/openresponses-http-api">OpenResponses API</a>
 */
public interface SseEventHandler {

    /**
     * 收到一个 SSE 事件。
     * <p>
     * 对于 Chat Completions 流，{@code event.getParsed()} 包含 {@link io.github.hiwepy.openclaw.api.model.ChatCompletionChunk}。
     * 对于 OpenResponses 流，{@code event.getEvent()} 包含事件类型（如 {@code response.output_text.delta}）。
     * </p>
     *
     * @param event SSE 事件（不为 null，{@code isDone()} 为 true 时不会调用此方法）
     */
    void onEvent(SseEvent event);

    /**
     * 流正常结束（收到 {@code data: [DONE]}）。
     */
    void onComplete();

    /**
     * 流异常终止。
     *
     * @param error 异常信息
     */
    void onError(Throwable error);
}
