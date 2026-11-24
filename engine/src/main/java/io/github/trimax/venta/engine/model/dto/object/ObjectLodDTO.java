package io.github.trimax.venta.engine.model.dto.object;

import io.github.trimax.venta.container.tree.Node;
import lombok.NonNull;

/**
 * A level of detail of an object: a simpler model that is drawn instead of the main one when the object is small on the screen.
 *
 * @param screenSize the share of the height of the screen below which the level is used, from 0 to 1 (the levels of an
 *                   object go from the nearest to the farthest, so the sizes decrease)
 * @param root       the model of the level, it is placed like the main model of the object
 * @param facing     whether the level is a picture on a flat card that is turned to the camera around the vertical axis
 *                   (a billboard): the card is a quad in the plane of x and y of the model and needs a cutout material
 */
public record ObjectLodDTO(@NonNull Float screenSize,
                           @NonNull Node<ObjectMeshDTO> root,
                           Boolean facing) {
}
