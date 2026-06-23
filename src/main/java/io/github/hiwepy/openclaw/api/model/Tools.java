package io.github.hiwepy.openclaw.api.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.hiwepy.openclaw.api.OpenClawConstants;
import io.github.hiwepy.openclaw.api.model.ChatMessage.ToolCall;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * OpenAI 函数工具调用辅助类。
 * <p>
 * 提供工具定义、参数处理、调用执行等常用操作。
 * </p>
 *
 * <h3>用法示例</h3>
 * <pre>{@code
 * // 定义工具
 * Map<String, Object> getWeatherTool = Tools.function("get_weather", "Get weather info")
 *     .param("city", "string", "City name")
 *     .param("country", "string", "Country code", true)
 *     .build();
 *
 * // 解析工具调用参数
 * ToolCall call = response.getChoices().get(0).getMessage().getToolCalls().get(0);
 * Map<String, Object> args = Tools.parseArgs(call, Map.class);
 * String city = (String) args.get("city")
 *
 * // 执行工具并构建结果消息
 * String result = executeTool(call, args);
 * ChatMessage resultMsg = Tools.toolResult(call.getId(), result);
 * }</pre>
 *
 * @see ChatMessage.ToolCall
 * @see <a href="https://docs.openclaw.ai/gateway/openai-http-api#chat-tool-contract">Chat tool contract</a>
 */
public final class Tools {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private Tools() {}

    /**
     * 创建函数工具定义。
     *
     * @param name 工具名称
     * @param description 工具描述
     */
    public static FunctionBuilder function(String name, String description) {
        return new FunctionBuilder(name, description);
    }

    /**
     * 判断消息是否包含工具调用。
     */
    public static boolean hasToolCalls(ChatMessage message) {
        return message != null
            && message.getToolCalls() != null
            && !message.getToolCalls().isEmpty();
    }

    /**
     * 判断 chunk 是否表示工具调用完成。
     */
    public static boolean isToolCallFinish(String finishReason) {
        return OpenClawConstants.FINISH_REASON_TOOL_CALLS.equals(finishReason);
    }

    /**
     * 解析工具调用参数为指定类型。
     *
     * @param toolCall 工具调用
     * @param clazz 目标类型（如 Map.class 或自定义类）
     * @return 解析后的参数对象
     */
    public static <T> T parseArgs(ToolCall toolCall, Class<T> clazz) {
        Objects.requireNonNull(toolCall, "toolCall");
        Objects.requireNonNull(toolCall.getFunction(), "toolCall.function");
        String args = toolCall.getFunction().getArguments();
        if (args == null || args.isEmpty()) {
            try {
                return clazz.getDeclaredConstructor().newInstance();
            } catch (Exception e) {
                throw new IllegalArgumentException("Cannot instantiate " + clazz.getName(), e);
            }
        }
        try {
            return MAPPER.readValue(args, clazz);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Failed to parse tool arguments: " + args, e);
        }
    }

    /**
     * 解析工具调用参数为 Map。
     */
    public static Map<String, Object> parseArgsAsMap(ToolCall toolCall) {
        return parseArgs(toolCall, Map.class);
    }

    /**
     * 创建工具结果消息。
     *
     * @param toolCallId 对应的工具调用 ID
     * @param output 工具执行结果（可以是任意可序列化对象，会自动转为 JSON）
     */
    public static ChatMessage toolResult(String toolCallId, Object output) {
        String content;
        if (output instanceof String) {
            content = (String) output;
        } else {
            try {
                content = MAPPER.writeValueAsString(output);
            } catch (JsonProcessingException e) {
                content = String.valueOf(output);
            }
        }
        return ChatMessage.ofTool(toolCallId, content);
    }

    /**
     * 从消息中提取所有工具调用。
     */
    public static List<ToolCall> extractToolCalls(ChatMessage message) {
        if (!hasToolCalls(message)) {
            return Collections.emptyList();
        }
        return message.getToolCalls();
    }

    /**
     * 工具函数定义构建器。
     */
    public static class FunctionBuilder {
        private final String name;
        private final String description;
        private final Map<String, Parameter> parameters = new java.util.LinkedHashMap<>();
        private boolean required = false;

        FunctionBuilder(String name, String description) {
            this.name = Objects.requireNonNull(name, "name");
            this.description = Objects.requireNonNull(description, "description");
        }

        /**
         * 添加可选参数。
         */
        public FunctionBuilder param(String name, String type, String description) {
            return param(name, type, description, false);
        }

        /**
         * 添加参数。
         *
         * @param name 参数名
         * @param type 参数类型：string, number, integer, boolean, array, object
         * @param description 参数描述
         * @param required 是否必填
         */
        public FunctionBuilder param(String name, String type, String description, boolean required) {
            parameters.put(name, new Parameter(name, type, description, required));
            if (required) {
                this.required = true;
            }
            return this;
        }

        /**
         * 构建工具定义为 Map（用于 HTTP 请求）。
         */
        @SuppressWarnings("unchecked")
        public Map<String, Object> build() {
            Map<String, Object> properties = new java.util.LinkedHashMap<>();
            List<String> requiredList = new java.util.ArrayList<>();
            for (Map.Entry<String, Parameter> entry : parameters.entrySet()) {
                Map<String, Object> param = new java.util.LinkedHashMap<>();
                param.put("type", entry.getValue().type);
                param.put("description", entry.getValue().description);
                properties.put(entry.getKey(), param);
                if (entry.getValue().required) {
                    requiredList.add(entry.getKey());
                }
            }
            Map<String, Object> function = new java.util.LinkedHashMap<>();
            function.put("name", name);
            function.put("description", description);
            Map<String, Object> params = new java.util.LinkedHashMap<>();
            params.put("type", "object");
            params.put("properties", properties);
            if (!requiredList.isEmpty()) {
                params.put("required", requiredList);
            }
            function.put("parameters", params);
            Map<String, Object> tool = new java.util.LinkedHashMap<>();
            tool.put("type", OpenClawConstants.TOOL_TYPE_FUNCTION);
            tool.put("function", function);
            return tool;
        }

        private static class Parameter {
            final String name;
            final String type;
            final String description;
            final boolean required;

            Parameter(String name, String type, String description, boolean required) {
                this.name = name;
                this.type = type;
                this.description = description;
                this.required = required;
            }
        }
    }
}
