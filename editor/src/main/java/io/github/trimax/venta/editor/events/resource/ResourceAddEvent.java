package io.github.trimax.venta.editor.events.resource;

import io.github.trimax.venta.editor.events.AbstractEvent;
import lombok.NonNull;

import java.io.File;

public record ResourceAddEvent(@NonNull File file) implements AbstractEvent {
}
