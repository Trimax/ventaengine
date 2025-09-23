package io.github.trimax.venta.engine.utils;

import java.util.Random;

import org.joml.Vector3f;
import org.joml.Vector3fc;

import lombok.experimental.UtilityClass;

@UtilityClass
public final class RandomUtil {
    private final Random random = new Random(System.currentTimeMillis());

    static {
        random.setSeed(System.nanoTime());
    }

    public Vector3fc vector3() {
        return new Vector3f(random.nextFloat(), random.nextFloat(), random.nextFloat());
    }
}
