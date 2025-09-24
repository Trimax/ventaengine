package io.github.trimax.venta.editor.events.group;

import io.github.trimax.venta.editor.events.AbstractEvent;
import lombok.NonNull;

public record GroupAddEvent(@NonNull String name) implements AbstractEvent {
}
