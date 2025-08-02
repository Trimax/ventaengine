package io.github.trimax.venta.engine.managers;

import io.github.trimax.venta.engine.model.view.AbstractView;
import lombok.NonNull;

public interface AbstractManager<V extends AbstractView> {
    V get(@NonNull final String id);
}
