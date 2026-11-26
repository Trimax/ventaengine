package io.github.trimax.venta.engine.model.common.geo;

import org.joml.Vector3f;

/**
 * A half line: where it starts and where it goes (a unit vector).
 */
public record Ray(Vector3f origin, Vector3f direction) {
}
