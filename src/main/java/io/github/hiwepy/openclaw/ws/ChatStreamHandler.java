package io.github.hiwepy.openclaw.ws;

/**
 * {@code chat.send} 流式回复处理器。
 * <p>Gateway 在处理 {@code chat.send} 时会推送多个 {@code event: "chat"} 事件帧，
 * 其中 {@code delta: true} 表示增量文本，{@code done: true} 表示回复完成。</p>
 */
public interface ChatStreamHandler {

    /**
     * 收到增量文本。
     *
     * @param text 增量文本片段
     */
    void onDelta(String text);

    /**
     * 回复完成。
     *
     * @param fullText 完整回复文本
     */
    void onComplete(String fullText);

    /**
     * 回复出错。
     *
     * @param error 错误信息
     */
    void onError(String error);
}
