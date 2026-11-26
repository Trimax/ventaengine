package io.github.trimax.venta.studio.model.event;

import io.github.trimax.venta.engine.model.dto.SceneDTO;

/**
 * A scene of the project is open in the viewport.
 *
 * @param name  the name of the scene file, like {@code beach.json}
 * @param scene what the file says
 */
public record SceneOpenedEvent(String name, SceneDTO scene) {
}
