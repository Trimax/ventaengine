package io.github.trimax.venta.engine.model.common.terrain;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import io.github.trimax.venta.engine.model.dto.terrain.TerrainSlopeMaterialDTO;
import io.github.trimax.venta.engine.utils.ParsingUtil;

final class TerrainSlopeTest {
    @Test
    void testSlopeWithoutSettingsCoversTheWholeRange() {
        final var slope = new TerrainSlope(ParsingUtil.asObject("{\"material\": \"terrain-rock.json\"}", TerrainSlopeMaterialDTO.class));

        assertEquals(TerrainSlope.DEFAULT_MINIMUM, slope.getMinimum());
        assertEquals(TerrainSlope.DEFAULT_MAXIMUM, slope.getMaximum());
        assertEquals(TerrainSlope.DEFAULT_BLEND, slope.getBlend());
    }

    @Test
    void testSettingsAreRead() {
        final var slope = new TerrainSlope(ParsingUtil.asObject(
                "{\"material\": \"terrain-rock.json\", \"minSlope\": 35, \"maxSlope\": 80, \"slopeBlend\": 5}", TerrainSlopeMaterialDTO.class));

        assertEquals(35.f, slope.getMinimum());
        assertEquals(80.f, slope.getMaximum());
        assertEquals(5.f, slope.getBlend());
    }

    @Test
    void testBadAnglesAreRefused() {
        assertThrows(IllegalArgumentException.class, () -> new TerrainSlope(-1.f, 50.f, 5.f));
        assertThrows(IllegalArgumentException.class, () -> new TerrainSlope(60.f, 50.f, 5.f), "The minimum is not above the maximum");
        assertThrows(IllegalArgumentException.class, () -> new TerrainSlope(10.f, 91.f, 5.f));
        assertThrows(IllegalArgumentException.class, () -> new TerrainSlope(Float.NaN, 50.f, 5.f));
        assertThrows(IllegalArgumentException.class, () -> new TerrainSlope(10.f, 50.f, -1.f));
        assertThrows(IllegalArgumentException.class, () -> new TerrainSlope(10.f, 50.f, Float.NaN));
    }

    @Test
    void testEdgesAreAllowed() {
        assertDoesNotThrow(() -> new TerrainSlope(0.f, 90.f, 0.f), "The whole range with a hard edge");
        assertDoesNotThrow(() -> new TerrainSlope(45.f, 45.f, 0.f));
    }
}
