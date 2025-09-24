package io.github.trimax.venta.editor.events.resource;

import java.io.File;

import lombok.NonNull;

public record ResourceAddEvent(@NonNull File file) {
}
