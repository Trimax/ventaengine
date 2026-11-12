package io.github.trimax.venta.engine.model.dto;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import io.github.trimax.venta.engine.utils.ParsingUtil;

final class SoundSourceDTOTest {
    @Test
    void testParseDistances() {
        final var dto = ParsingUtil.asObject("""
                {
                  "sound": "fire.ogg",
                  "referenceDistance": 10.0,
                  "maximalDistance": 200.0,
                  "rolloff": 0.5
                }""", SoundSourceDTO.class);

        assertEquals(10.f, dto.referenceDistance(), "Reference distance must be parsed");
        assertEquals(200.f, dto.maximalDistance(), "Maximal distance must be parsed");
        assertEquals(0.5f, dto.rolloff(), "Rolloff must be parsed");
    }

    @Test
    void testDistancesAreOptional() {
        final var dto = ParsingUtil.asObject("""
                {
                  "sound": "fire.ogg"
                }""", SoundSourceDTO.class);

        assertNull(dto.referenceDistance(), "Missing reference distance must be null, the repository uses the OpenAL default");
        assertNull(dto.maximalDistance(), "Missing maximal distance must be null, the repository uses the OpenAL default");
        assertNull(dto.rolloff(), "Missing rolloff must be null, the repository uses the OpenAL default");
        assertNull(dto.pitch(), "Missing pitch must be null, the repository uses 1");
    }
}
