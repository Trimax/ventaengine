package io.github.trimax.venta.engine.model.common.overlay;

import org.joml.Vector3fc;

import io.github.trimax.venta.engine.model.instance.AbstractInstance;

/**
 * The mark of a thing that has no shape of its own (a light, an emitter, a billboard, a sound source): a small cross at its place,
 * that is seen in an editor and can be chosen.
 *
 * @param instance the thing
 * @param kind     what it is
 * @param position where it is
 */
public record Mark(AbstractInstance instance, Kind kind, Vector3fc position) {
    /**
     * What a mark stands for, and its color.
     */
    public enum Kind {
        Light(1.f, 0.9f, 0.35f),
        Emitter(1.f, 0.5f, 0.3f),
        Billboard(0.5f, 0.85f, 1.f),
        Sound(0.5f, 1.f, 0.6f);

        private final float[] color;

        Kind(final float red, final float green, final float blue) {
            this.color = new float[]{red, green, blue};
        }

        public float[] getColor() {
            return color.clone();
        }
    }
}
