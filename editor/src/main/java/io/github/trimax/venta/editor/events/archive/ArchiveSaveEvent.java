package io.github.trimax.venta.editor.events.archive;

import java.io.File;

import lombok.NonNull;

public record ArchiveSaveEvent(@NonNull File file) {
}
