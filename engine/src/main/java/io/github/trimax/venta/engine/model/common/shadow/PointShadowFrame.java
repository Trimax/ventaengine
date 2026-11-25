package io.github.trimax.venta.engine.model.common.shadow;

import java.util.Arrays;

import org.joml.Matrix4f;

import io.github.trimax.venta.engine.model.instance.LightInstance;
import io.github.trimax.venta.engine.model.instance.implementation.LightInstanceImplementation;
import lombok.Getter;

/**
 * The shadows of the point lights of a frame: which lights have a map, the slot of each, and the matrices of the faces
 * of its cube. A slot is a block of faces in the map: the faces of a light are the layers {@code slot * faces} to
 * {@code slot * faces + faces - 1}.
 *
 * <p>It is made once with room for the most slots and filled again every frame, so a frame allocates nothing for it.
 */
public final class PointShadowFrame {
    private final LightInstanceImplementation[] lights;
    private final float[] ranges;
    private final int[] used;
    private final Matrix4f[] matrices;
    private final int faces;

    /** The number of the lights that have a map in this frame */
    @Getter
    private int count;

    public PointShadowFrame(final int capacity, final int faces) {
        if (capacity < 1 || faces < 1)
            throw new IllegalArgumentException(String.format("A frame needs room for a light and a face, got %d and %d", capacity, faces));

        this.lights = new LightInstanceImplementation[capacity];
        this.ranges = new float[capacity];
        this.used = new int[capacity];
        this.matrices = new Matrix4f[capacity * faces];
        this.faces = faces;

        for (int i = 0; i < matrices.length; i++)
            matrices[i] = new Matrix4f();
    }

    /** The most lights the frame can have maps for */
    public int getCapacity() {
        return lights.length;
    }

    /** The number of the faces of a light */
    public int getFaces() {
        return faces;
    }

    /** Whether any light has a map in this frame */
    public boolean isActive() {
        return count > 0;
    }

    /** Forgets the lights of the last frame: they are not kept alive by the frame */
    public void clear() {
        Arrays.fill(lights, 0, count, null);
        count = 0;
    }

    /**
     * Gives the next slot to a light that has all the faces of a cube.
     *
     * @param range how far the shadows of the light reach: the far plane of its faces
     * @return the slot
     */
    public int add(final LightInstanceImplementation light, final float range) {
        return add(light, range, faces);
    }

    /**
     * Gives the next slot to a light.
     *
     * @param range     how far the shadows of the light reach: the far plane of its faces
     * @param usedFaces how many of the faces of the slot are drawn: a spot light has one, from the first
     * @return the slot
     */
    public int add(final LightInstanceImplementation light, final float range, final int usedFaces) {
        if (count >= lights.length)
            throw new IllegalStateException("The frame has no room for another light with shadows: " + lights.length);

        if (usedFaces < 1 || usedFaces > faces)
            throw new IllegalArgumentException(String.format("A slot has 1 to %d faces, got %d", faces, usedFaces));

        lights[count] = light;
        ranges[count] = range;
        used[count] = usedFaces;

        return count++;
    }

    /** How many faces of a slot are drawn */
    public int getUsedFaces(final int slot) {
        return used[slot];
    }

    public LightInstanceImplementation getLight(final int slot) {
        return lights[slot];
    }

    public float getRange(final int slot) {
        return ranges[slot];
    }

    /** The view and the projection of a face of a slot: the matrix is filled by the one that made the slot */
    public Matrix4f getMatrix(final int slot, final int face) {
        return matrices[slot * faces + face];
    }

    /** The slot of a light, -1 when the light has no map in this frame */
    public int getSlot(final LightInstance light) {
        for (int slot = 0; slot < count; slot++)
            if (lights[slot] == light)
                return slot;

        return -1;
    }
}
