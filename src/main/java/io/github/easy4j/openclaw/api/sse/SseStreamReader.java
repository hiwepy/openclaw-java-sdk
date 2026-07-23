package io.github.easy4j.openclaw.api.sse;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.easy4j.openclaw.exception.OpenClawHttpException;
import io.github.easy4j.openclaw.api.model.ChatChunk;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/**
 * SSE 流解析器。
 * <p>
 * 从 HTTP 响应的 {@code text/event-stream} 中逐行读取并解析 SSE 事件。
 * 支持两种格式：
 * </p>
 * <ul>
 *   <li>Chat Completions SSE：{@code data: <json>} + {@code data: [DONE]}</li>
 *   <li>OpenResponses SSE：{@code event: <type>} + {@code data: <json>} + {@code data: [DONE]}</li>
 * </ul>
 *
 * @see <a href="https://docs.openclaw.ai/gateway/openai-http-api#streaming-sse">OpenAI Streaming (SSE)</a>
 * @see <a href="https://docs.openclaw.ai/gateway/openresponses-http-api#streaming-sse">OpenResponses Streaming (SSE)</a>
 */
@Slf4j
public class SseStreamReader {

    private static final String DONE_MARKER = "[DONE]";

    private final ObjectMapper objectMapper;

    public SseStreamReader(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper != null ? objectMapper : new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public SseStreamReader() {
        this(null);
    }

    /**
     * 同步读取 SSE 流，将每个事件分发给处理器。
     *
     * @param inputStream HTTP 响应的输入流
     * @param handler     事件处理器
     * @param chunkClass  Chat Completions chunk 解析目标类（传 {@code null} 则不解析 data）
     */
    public <T> void readStream(InputStream inputStream, SseEventHandler handler, Class<T> chunkClass) {
        if (inputStream == null) {
            handler.onError(new OpenClawHttpException("SSE input stream is null", null));
            return;
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            String currentEvent = null;
            String line;

            while ((line = reader.readLine()) != null) {
                if (line.isEmpty()) {
                    // 空行表示事件边界，重置 event type
                    currentEvent = null;
                    continue;
                }

                if (line.startsWith("event:")) {
                    currentEvent = line.substring("event:".length()).trim();
                    continue;
                }

                if (line.startsWith("data:")) {
                    String data = line.substring("data:".length()).trim();

                    if (DONE_MARKER.equals(data)) {
                        handler.onComplete();
                        return;
                    }

                    SseEvent sseEvent = SseEvent.of(currentEvent, data);

                    // 尝试解析为 ChatCompletionChunk
                    if (chunkClass != null) {
                        try {
                            T chunk = objectMapper.readValue(data, chunkClass);
                            sseEvent.setParsed(chunk);
                        } catch (Exception parseEx) {
                            log.debug("Failed to parse SSE data as {}: {}", chunkClass.getSimpleName(), parseEx.getMessage());
                        }
                    }

                    try {
                        handler.onEvent(sseEvent);
                    } catch (Exception handlerEx) {
                        log.warn("SSE event handler threw exception: {}", handlerEx.getMessage(), handlerEx);
                    }
                }
                // 忽略其他行（如 id:, retry:, 注释）
            }

            // 流正常关闭但未收到 [DONE]
            handler.onComplete();
        } catch (Exception e) {
            log.error("SSE stream read error: {}", e.getMessage(), e);
            handler.onError(e);
        }
    }

    /**
     * 读取 Chat Completions SSE 流。
     */
    public void readChatCompletionStream(InputStream inputStream, SseEventHandler handler) {
        readStream(inputStream, handler, ChatChunk.class);
    }

    /**
     * 读取 OpenResponses SSE 流（不解析 data，仅传递原始 event + data）。
     */
    public void readResponseStream(InputStream inputStream, SseEventHandler handler) {
        readStream(inputStream, handler, null);
    }
}
