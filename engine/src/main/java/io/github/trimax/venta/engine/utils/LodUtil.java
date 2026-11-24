package io.github.trimax.venta.engine.utils;

import lombok.experimental.UtilityClass;

/**
 * The arithmetic of the levels of detail: how big an object is on the screen and which level is drawn for that size.
 */
@UtilityClass
public final class LodUtil {
    /** An object nearer than this is as big as it can be: the size doesn't grow to infinity inside of it */
    private static final float MINIMAL_DISTANCE = 1e-3f;

    /**
     * The share of the height of the screen that a sphere takes.
     *
     * @param radius          the radius of the sphere in the units of the world
     * @param distance        the distance from the camera to the center of the sphere
     * @param projectionScale the focal length of the projection: {@code 1 / tan(fov / 2)}, the element of the projection matrix at the second row and column
     * @return the share, 1 is the whole height of the screen
     */
    public float screenSize(final float radius, final float distance, final float projectionScale) {
        return radius * projectionScale / Math.max(distance, MINIMAL_DISTANCE);
    }

    /**
     * The level to draw for a size on the screen. The level i + 1 replaces the level i when the size is below {@code sizes[i]}.
     * The border of a level is moved by the hysteresis in the direction of the level that is drawn, so an object whose size
     * floats around a border doesn't change its level with every frame: it goes to the simpler level when it is smaller
     * than the border by the share of the hysteresis and back when it is bigger by the same.
     *
     * @param sizes      the sizes below which the levels 1, 2, ... are used, they decrease
     * @param current    the level that is drawn now
     * @param size       the size of the object on the screen
     * @param hysteresis the share of a border that the size must pass to change the level, from 0 to 1
     * @return the level to draw, the current one when the size is not a number
     */
    public int select(final float[] sizes, final int current, final float size, final float hysteresis) {
        if (Float.isNaN(size))
            return current;

        var level = Math.clamp(current, 0, sizes.length);
        while (level < sizes.length && size < sizes[level] * (1.f - hysteresis))
            level++;

        while (level > 0 && size > sizes[level - 1] * (1.f + hysteresis))
            level--;

        return level;
    }
}
