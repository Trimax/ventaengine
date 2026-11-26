package io.github.trimax.venta.studio.model.event;

import java.nio.file.Path;

/**
 * A project is open: the folder is where the resources of the application are.
 */
public record ProjectOpenedEvent(Path folder) {
}
