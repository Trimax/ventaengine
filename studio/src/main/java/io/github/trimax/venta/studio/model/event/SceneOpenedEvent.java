package io.github.trimax.venta.studio.model.event;

import io.github.trimax.venta.engine.model.dto.SceneDTO;
import io.github.trimax.venta.studio.model.outline.OutlineNode;

/**
 * A scene of the project is open in the viewport.
 *
 * @param name    the name of the scene file, like {@code beach.json}
 * @param scene   what the file says
 * @param outline the hierarchy of the scene: the panels and the viewport share its items
 */
public record SceneOpenedEvent(String name, SceneDTO scene, OutlineNode outline) {
}
