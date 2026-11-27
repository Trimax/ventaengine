package io.github.trimax.venta.engine.registries;

import io.github.trimax.venta.engine.model.entity.AbstractEntity;
import lombok.NonNull;

public interface AbstractRegistry<E extends AbstractEntity, A> extends Iterable<E> {
    E get(@NonNull final String resourcePath, final A argument);

    E get(@NonNull final String resourcePath);

    /**
     * Forgets the entity of a resource (with every argument it was loaded with): the next request loads it again, from the resource as it is
     * then. What was made of the old entity keeps it until it is made again.
     */
    void forget(@NonNull final String resourcePath);
}