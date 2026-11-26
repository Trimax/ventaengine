package io.github.trimax.venta.engine.model.common.pick;

import io.github.trimax.venta.engine.model.instance.AbstractInstance;

/**
 * The thing that is under a point of the screen.
 *
 * @param instance the thing: an object, a light, an emitter, a billboard or a sound source
 * @param distance how far it is from the camera along the ray
 */
public record PickResult(AbstractInstance instance, float distance) {
}
