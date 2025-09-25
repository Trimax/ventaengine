package io.github.trimax.venta.editor.model.event.resource;

import io.github.trimax.venta.editor.model.event.AbstractEvent;
import lombok.NonNull;

import java.io.File;

public record ResourceAddEvent(@NonNull File file) implements AbstractEvent {
}
