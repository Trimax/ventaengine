package io.github.trimax.venta.studio.model.event;

import io.github.trimax.venta.engine.model.dto.SceneDTO;
import io.github.trimax.venta.studio.model.outline.OutlineNode;

/**
 * The scene that is open was changed (a thing was edited, or the change was undone): the panels show it as it is now.
 *
 * @param scene   what the file says now
 * @param outline the hierarchy of it, made again
 */
public record SceneChangedEvent(SceneDTO scene, OutlineNode outline) {
}
