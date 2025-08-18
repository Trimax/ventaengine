package io.github.trimax.venta.engine.parsers;

import lombok.NonNull;

public interface AbstractParsingStrategy<T> {
    T parse(@NonNull final String resourcePath);
}
