package io.github.trimax.venta.editor.events.archive;

import io.github.trimax.venta.editor.events.AbstractEvent;
import lombok.NonNull;

import java.io.File;

public record ArchiveSaveEvent(@NonNull File file) implements AbstractEvent {
}
