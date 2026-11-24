package io.github.trimax.venta.engine.model.common.shadow;

import org.joml.Vector3f;
import org.joml.Vector3fc;

import lombok.Getter;
import lombok.NonNull;

/**
 * The shadows of a frame: the cascades of the shadow map and what the shaders need to choose a cascade of a point.
 *
 * <p>It is made once with room for the most cascades and filled again every frame, so a frame allocates nothing for it.
 * It is {@link #isActive() active} only in a frame where the sun casts shadows.
 */
public final class ShadowFrame {
    private final ShadowCascade[] cascades;
    private final Vector3f cameraForward = new Vector3f();

    /** The number of the cascades of this frame */
    @Getter
    private int count;

    /** How far from the camera there are shadows, they fade out at the end */
    @Getter
    private float distance;

    /** Whether the sun casts shadows in this frame: otherwise there is nothing to give to the shaders */
    @Getter
    private boolean active;

    public ShadowFrame(final int capacity) {
        if (capacity < 1)
            throw new IllegalArgumentException("A frame needs room for a cascade, got " + capacity);

        cascades = new ShadowCascade[capacity];
        for (int i = 0; i < capacity; i++)
            cascades[i] = new ShadowCascade();
    }

    /** The most cascades the frame can have */
    public int getCapacity() {
        return cascades.length;
    }

    /**
     * The cascade by its number, from the nearest (0) to the farthest.
     *
     * <p>Only the first {@link #getCount()} cascades belong to this frame.
     */
    public ShadowCascade getCascade(final int index) {
        return cascades[index];
    }

    /** The view direction of the camera: the distance of a point along it chooses the cascade */
    public Vector3fc getCameraForward() {
        return cameraForward;
    }

    /**
     * Starts a frame with the shadows: the cascades are filled through {@link #getCascade(int)} after this.
     */
    public void begin(final int count, final float distance) {
        if (count < 1 || count > cascades.length)
            throw new IllegalArgumentException(String.format("The frame has room for 1 to %d cascades, got %d", cascades.length, count));

        this.count = count;
        this.distance = distance;
    }

    /**
     * Finishes the frame with the shadows: the shaders can take it.
     */
    public void activate(@NonNull final Vector3fc cameraForward) {
        this.cameraForward.set(cameraForward);
        this.active = true;
    }

    /** The sun casts no shadows in this frame */
    public void deactivate() {
        this.active = false;
        this.count = 0;
    }
}
