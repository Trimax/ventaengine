package io.github.trimax.venta.editor.model.event.folder;

import io.github.trimax.venta.editor.model.event.AbstractEvent;
import lombok.NonNull;

public record FolderAddEvent(@NonNull String name) implements AbstractEvent {
}
