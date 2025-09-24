package io.github.trimax.venta.editor.events.archive;

import io.github.trimax.venta.editor.events.AbstractEvent;
import lombok.NonNull;

import java.io.File;

public record ArchiveBuildEvent(@NonNull File file) implements AbstractEvent {
}
