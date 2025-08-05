package io.github.trimax.venta.engine.registries;

import io.github.trimax.venta.engine.model.entity.AbstractEntity;
import lombok.NonNull;

public interface AbstractRegistry<E extends AbstractEntity, A> extends Iterable<E> {
    E get(@NonNull final String resourcePath, final A argument);

    E get(@NonNull final String resourcePath);
}