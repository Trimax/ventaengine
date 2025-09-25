package io.github.trimax.venta.editor.model.event.status;

import io.github.trimax.venta.editor.model.event.AbstractEvent;
import lombok.NonNull;

public record StatusSetEvent(@NonNull String status) implements AbstractEvent {
    public StatusSetEvent(@NonNull final String format, final Object... args) {
        this(String.format(format, args));
    }
}
