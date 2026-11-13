package io.github.trimax.venta.engine.model.dto.scene;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import io.github.trimax.venta.engine.enums.ToneMapping;
import io.github.trimax.venta.engine.model.dto.SceneDTO;
import io.github.trimax.venta.engine.utils.ParsingUtil;

final class SceneToneMappingDTOTest {
    @Test
    void testParseExposureAndToneMapping() {
        final var dto = ParsingUtil.asObject("""
                {
                  "exposure": 1.5,
                  "toneMapping": "ACES"
                }""", SceneDTO.class);

        assertEquals(1.5f, dto.exposure(), "Exposure must be parsed");
        assertEquals(ToneMapping.ACES, dto.toneMapping(), "Tone mapping must be parsed by its name");
    }

    @Test
    void testExposureAndToneMappingAreOptional() {
        final var dto = ParsingUtil.asObject("{}", SceneDTO.class);

        assertNull(dto.exposure(), "A scene without an exposure keeps the default one");
        assertNull(dto.toneMapping(), "A scene without a tone mapping keeps the default one");
    }

    @Test
    void testShaderValuesAreDistinct() {
        assertEquals(0, ToneMapping.None.getShaderValue(), "None");
        assertEquals(1, ToneMapping.Neutral.getShaderValue(), "Neutral");
        assertEquals(2, ToneMapping.ACES.getShaderValue(), "ACES");
    }
}
