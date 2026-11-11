package io.github.trimax.venta.engine.model.common.shared;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

final class AttenuationTest {
    @Test
    void testDefaultAttenuationDoesNotDivideByZero() {
        final var attenuation = Attenuation.createDefault();

        assertTrue(attenuation.getConstant() > 0, "Constant term must be positive, otherwise 1 / attenuation is infinite at distance 0");
    }

    @Test
    void testDefaultAttenuationIsNotShared() {
        assertNotSame(Attenuation.createDefault(), Attenuation.createDefault(), "Default attenuation is mutable and must not be shared");
    }

    @Test
    void testCopyIsIndependent() {
        final var original = new Attenuation(1, 2, 3);
        final var copy = new Attenuation(original);

        copy.setLinear(5);

        assertEquals(new Attenuation(1, 2, 3), original, "Changing a copy must not change the original");
    }
}
