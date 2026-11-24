package io.github.trimax.venta.engine.model.common.hierarchy;

import io.github.trimax.venta.container.tree.Node;
import lombok.NonNull;

/**
 * A level of detail of an object, the main model of the object is the level 0 and is not described by this record.
 *
 * @param root       the model of the level
 * @param screenSize the share of the height of the screen below which the level is used
 * @param facing     whether the level is a card that is turned to the camera, see {@code ObjectLodDTO}
 */
public record LodLevel(@NonNull Node<MeshReference> root, float screenSize, boolean facing) {
}
