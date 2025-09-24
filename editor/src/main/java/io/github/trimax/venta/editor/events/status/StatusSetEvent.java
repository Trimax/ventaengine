package io.github.trimax.venta.editor.events.status;

import lombok.NonNull;

public record StatusSetEvent(@NonNull String status) {
    public StatusSetEvent(@NonNull final String format, final Object... args) {
        this(String.format(format, args));
    }
}
