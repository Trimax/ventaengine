package io.github.trimax.venta.editor.model.event.group;

import io.github.trimax.venta.editor.model.event.AbstractEvent;
import lombok.NonNull;

public record GroupAddEvent(@NonNull String name) implements AbstractEvent {
}
