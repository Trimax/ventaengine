package io.github.trimax.venta.engine.model.common.math;

import static org.junit.jupiter.api.Assertions.*;

import org.joml.Vector3f;
import org.junit.jupiter.api.Test;

final class TransformTest {
    @Test
    void testScaleByVectorMultiplies() {
        final var transform = new Transform();
        transform.setScale(new Vector3f(2, 3, 4));

        transform.scale(new Vector3f(0.5f, 2, 1));

        assertEquals(new Vector3f(1, 6, 4), transform.getScale(), "Scale must be multiplied by the factor");
    }

    @Test
    void testScaleByVectorIsConsistentWithScalar() {
        final var byVector = new Transform();
        byVector.scale(new Vector3f(2));

        final var byScalar = new Transform();
        byScalar.scale(2);

        assertEquals(byScalar.getScale(), byVector.getScale(), "Uniform vector scale must match scalar scale");
    }

    @Test
    void testScaleUpdatesMatrix() {
        final var transform = new Transform();
        transform.getMatrix();

        transform.scale(new Vector3f(2));

        assertEquals(new Vector3f(2), transform.getMatrix().getScale(new Vector3f()), "Matrix must reflect the new scale");
    }
}
