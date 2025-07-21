package com.venta.engine.adapters;

import java.lang.reflect.Type;
import java.util.function.Function;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor(access = AccessLevel.PROTECTED)
abstract class AbstractAdapter<T extends Enum<T>> implements JsonDeserializer<T> {
    @Getter
    private final Class<T> type;
    private final Function<T, String> valueExtractor;

    @Override
    public T deserialize(final JsonElement element, final Type typeOfT, final JsonDeserializationContext context) throws JsonParseException {
        final var key = element.getAsString();

        for (final var value : type.getEnumConstants())
            if (valueExtractor.apply(value).equals(key))
                return value;

        throw new JsonParseException("Unknown + " + type.getSimpleName() + " + type key: " + key);
    }
}
