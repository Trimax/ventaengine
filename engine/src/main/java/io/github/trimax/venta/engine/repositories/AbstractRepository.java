package io.github.trimax.venta.engine.repositories;

import io.github.trimax.venta.engine.model.prefabs.AbstractPrefab;
import lombok.NonNull;

public interface AbstractRepository<P extends AbstractPrefab> extends Iterable<P> {
    P get(@NonNull final String resourcePath);
}
