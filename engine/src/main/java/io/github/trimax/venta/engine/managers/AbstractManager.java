package io.github.trimax.venta.engine.managers;

import io.github.trimax.venta.engine.model.instance.AbstractInstance;
import lombok.NonNull;

public interface AbstractManager<I extends AbstractInstance> extends Iterable<I> {
    I get(@NonNull final String id);
}
