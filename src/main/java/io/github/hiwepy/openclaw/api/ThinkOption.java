package io.github.hiwepy.openclaw.api;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.io.IOException;
import java.util.List;

/**
 * Controls whether thinking/reasoning models expose their reasoning trace.
 * <p>
 * Most models (Qwen 3, DeepSeek-v3.1, DeepSeek R1) accept boolean enable/disable.
 * GPT-OSS model requires string levels: "low", "medium", or "high".
 * </p>
 */
@JsonSerialize(using = ThinkOption.ThinkOptionSerializer.class)
@JsonDeserialize(using = ThinkOption.ThinkOptionDeserializer.class)
public sealed interface ThinkOption {

    Object toJsonValue();

    /** Boolean-style think for models supporting simple enable/disable. */
    record ThinkBoolean(boolean enabled) implements ThinkOption {
        public static final ThinkBoolean ENABLED = new ThinkBoolean(true);
        public static final ThinkBoolean DISABLED = new ThinkBoolean(false);

        @Override
        public Object toJsonValue() { return enabled; }
    }

    /** String-level think for GPT-OSS model. */
    record ThinkLevel(String level) implements ThinkOption {
        private static final List<String> VALID = List.of("low", "medium", "high");
        public static final ThinkLevel LOW = new ThinkLevel("low");
        public static final ThinkLevel MEDIUM = new ThinkLevel("medium");
        public static final ThinkLevel HIGH = new ThinkLevel("high");

        public ThinkLevel {
            if (level != null && !VALID.contains(level)) {
                throw new IllegalArgumentException("think level must be one of " + VALID + ", got: " + level);
            }
        }

        @Override
        public Object toJsonValue() { return level; }
    }

    class ThinkOptionSerializer extends JsonSerializer<ThinkOption> {
        @Override
        public void serialize(ThinkOption value, JsonGenerator gen, SerializerProvider serializers)
                throws IOException {
            if (value == null) gen.writeNull();
            else gen.writeObject(value.toJsonValue());
        }
    }

    class ThinkOptionDeserializer extends JsonDeserializer<ThinkOption> {
        @Override
        public ThinkOption deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            JsonToken token = p.currentToken();
            if (token == JsonToken.VALUE_TRUE) return ThinkBoolean.ENABLED;
            if (token == JsonToken.VALUE_FALSE) return ThinkBoolean.DISABLED;
            if (token == JsonToken.VALUE_STRING) return new ThinkLevel(p.getValueAsString());
            if (token == JsonToken.VALUE_NULL) return null;
            throw new IOException("Cannot deserialize ThinkOption from token: " + token);
        }
    }
}
