package io.github.trimax.examples.utils;

import lombok.experimental.UtilityClass;
import org.joml.Vector3f;

import java.util.Random;

@UtilityClass
public final class VectorUtil {
    public Vector3f createRandomVector3(final float min, final float max) {
        final Random random = new Random();
        random.setSeed(System.nanoTime());
        return new Vector3f(random.nextFloat(), random.nextFloat(), random.nextFloat()).mul(2 * max).add(min, min, min);
    }

    public Vector3f createRandomVector3XZ(final float min, final float max) {
        final Random random = new Random();
        random.setSeed(System.nanoTime());
        return new Vector3f(random.nextFloat(), random.nextFloat(), random.nextFloat()).mul(2 * max).add(min, min, min).setComponent(1, 0.f);
    }
}
