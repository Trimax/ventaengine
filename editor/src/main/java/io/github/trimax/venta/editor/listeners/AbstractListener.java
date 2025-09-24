package io.github.trimax.venta.editor.listeners;

import io.github.trimax.venta.editor.events.AbstractEvent;
import lombok.NonNull;

public interface AbstractListener<E extends AbstractEvent> {
    void handle(@NonNull final E event);
}
