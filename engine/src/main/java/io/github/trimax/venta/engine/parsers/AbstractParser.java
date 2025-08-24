package io.github.trimax.venta.engine.parsers;

import io.github.trimax.venta.engine.enums.MeshFormat;
import lombok.NonNull;

public interface AbstractParser<T> {
    T parse(@NonNull final String resourcePath);

    MeshFormat format();
}
