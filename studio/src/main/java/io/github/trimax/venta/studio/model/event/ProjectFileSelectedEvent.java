package io.github.trimax.venta.studio.model.event;

import java.nio.file.Path;

/**
 * A file of the project is chosen in the project panel, or none (null).
 */
public record ProjectFileSelectedEvent(Path file) {
}
