package io.github.trimax.venta.editor.model.event.archive;

import io.github.trimax.venta.editor.model.event.AbstractEvent;
import lombok.NonNull;

import java.io.File;

public record ArchiveBuildEvent(@NonNull File file) implements AbstractEvent {
}
