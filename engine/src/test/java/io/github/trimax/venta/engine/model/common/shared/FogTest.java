package io.github.trimax.venta.engine.model.common.shared;

import static org.junit.jupiter.api.Assertions.*;

import org.joml.Vector3f;
import org.junit.jupiter.api.Test;

import io.github.trimax.venta.engine.model.dto.common.ColorDTO;
import io.github.trimax.venta.engine.model.dto.scene.SceneFogDTO;
import io.github.trimax.venta.engine.utils.ParsingUtil;

final class FogTest {
    private static final ColorDTO WHITE = new ColorDTO(1, 1, 1, 1);

    /** The fog of a layer of a scene file */
    private static SceneFogDTO layer(final Float height) {
        return new SceneFogDTO(new Range<>(10.f, 20.f), WHITE, height, null, null, null, null, null);
    }

    private static SceneFogDTO parse(final String json) {
        return ParsingUtil.asObject(json, SceneFogDTO.class);
    }

    @Test
    void testInvalidDistancesAreRejected() {
        assertThrows(IllegalArgumentException.class, () -> new Fog(-1, 10, new Vector3f()), "Negative minimal distance");
        assertThrows(IllegalArgumentException.class, () -> new Fog(10, 10, new Vector3f()), "Equal distances");
        assertThrows(IllegalArgumentException.class, () -> new Fog(10, 5, new Vector3f()), "Maximal below minimal");
    }

    @Test
    void testMaximalDistanceIsKeptAboveMinimal() {
        final var fog = new Fog(10, 20, new Vector3f());

        fog.setMaximalDistance(10);
        assertTrue(fog.getMaximalDistance() > fog.getMinimalDistance(), "Maximal distance must stay above minimal");

        fog.setMaximalDistance(5);
        assertTrue(fog.getMaximalDistance() > fog.getMinimalDistance(), "Maximal distance must stay above minimal");
    }

    @Test
    void testMovingMinimalDistanceForwardMovesMaximal() {
        final var fog = new Fog(10, 20, new Vector3f());

        fog.setMinimalDistance(30);

        assertEquals(30, fog.getMinimalDistance(), "Minimal distance must be set");
        assertTrue(fog.getMaximalDistance() > 30, "Maximal distance must be moved forward");
    }

    @Test
    void testMovingFogForwardAsTheExampleDoes() {
        final var fog = new Fog(10, 15, new Vector3f());

        fog.setMinimalDistance(40);
        fog.setMaximalDistance(45);

        assertEquals(40, fog.getMinimalDistance(), "Minimal distance");
        assertEquals(45, fog.getMaximalDistance(), "Maximal distance");
    }

    @Test
    void testNegativeMinimalDistanceIsClamped() {
        final var fog = new Fog(10, 20, new Vector3f());

        fog.setMinimalDistance(-5);

        assertEquals(0, fog.getMinimalDistance(), "Minimal distance must not be negative");
        assertEquals(20, fog.getMaximalDistance(), "Maximal distance must not change");
    }

    @Test
    void testFogHasNoLayerByDefault() {
        assertEquals(Float.POSITIVE_INFINITY, new Fog(10, 20, new Vector3f()).getHeight(), "The whole way must be fogged");
        assertEquals(Float.POSITIVE_INFINITY, new Fog(layer(null)).getHeight(),
                "A scene fog without a height must have no layer");
    }

    @Test
    void testFogLayerHeightIsKept() {
        assertEquals(15, new Fog(10, 20, 15, new Vector3f()).getHeight());
        assertEquals(-3, new Fog(layer(-3.f)).getHeight());
    }

    @Test
    void testFogOfALayerIsNotExponential() {
        final var fog = new Fog(layer(15.f));

        assertFalse(fog.isExponential());
        assertEquals(0.f, fog.getDensity());
        assertEquals(0.f, fog.getSunScattering(), "A scene without the sun settings keeps the color of its fog");
    }

    @Test
    void testDensityMakesTheFogExponentialWithoutDistances() {
        final var fog = new Fog(parse("{\"color\": {\"r\": 0.5, \"g\": 0.5, \"b\": 0.5, \"a\": 1.0}, \"density\": 0.004}"));

        assertTrue(fog.isExponential());
        assertEquals(0.004f, fog.getDensity());
        assertEquals(Fog.DEFAULT_FALLOFF, fog.getFalloff());
        assertEquals(0.f, fog.getBaseHeight());
        assertEquals(Fog.DEFAULT_SUN_SHARPNESS, fog.getSunSharpness());
    }

    @Test
    void testExponentialSettingsAreRead() {
        final var fog = new Fog(parse("{\"color\": {\"r\": 1, \"g\": 1, \"b\": 1, \"a\": 1}, \"density\": 0.01, \"falloff\": 0.05, "
                + "\"baseHeight\": 20.0, \"sunScattering\": 0.6, \"sunSharpness\": 4.0}"));

        assertEquals(0.01f, fog.getDensity());
        assertEquals(0.05f, fog.getFalloff());
        assertEquals(20.f, fog.getBaseHeight());
        assertEquals(0.6f, fog.getSunScattering());
        assertEquals(4.f, fog.getSunSharpness());
    }

    @Test
    void testFogWithNeitherDistancesNorDensityIsRefused() {
        assertThrows(IllegalArgumentException.class, () -> new Fog(parse("{\"color\": {\"r\": 1, \"g\": 1, \"b\": 1, \"a\": 1}}")));
    }

    @Test
    void testLayerKeepsItsDistancesNextToADensity() {
        final var fog = new Fog(parse("{\"distance\": {\"min\": 10, \"max\": 20}, \"color\": {\"r\": 1, \"g\": 1, \"b\": 1, \"a\": 1}, \"density\": 0.01}"));

        assertEquals(10.f, fog.getMinimalDistance());
        assertEquals(20.f, fog.getMaximalDistance());
        assertTrue(fog.isExponential());
    }

    @Test
    void testBadExponentialSettingsAreRefused() {
        final var fog = new Fog(10, 20, new Vector3f());

        assertThrows(IllegalArgumentException.class, () -> fog.setDensity(-0.1f));
        assertThrows(IllegalArgumentException.class, () -> fog.setDensity(Float.NaN));
        assertThrows(IllegalArgumentException.class, () -> fog.setFalloff(0.f));
        assertThrows(IllegalArgumentException.class, () -> fog.setFalloff(-1.f));
        assertThrows(IllegalArgumentException.class, () -> fog.setBaseHeight(Float.POSITIVE_INFINITY));
        assertThrows(IllegalArgumentException.class, () -> fog.setSunScattering(1.5f));
        assertThrows(IllegalArgumentException.class, () -> fog.setSunScattering(-0.1f));
        assertThrows(IllegalArgumentException.class, () -> fog.setSunSharpness(0.5f));
        assertEquals(0.f, fog.getDensity(), "A refused value changes nothing");
    }

    @Test
    void testDensityOfZeroTurnsTheFogBackIntoALayer() {
        final var fog = new Fog(10, 20, new Vector3f());
        fog.setDensity(0.01f);
        assertTrue(fog.isExponential());

        fog.setDensity(0.f);

        assertFalse(fog.isExponential());
    }
}
