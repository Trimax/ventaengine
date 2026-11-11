package io.github.trimax.venta.engine.model.dto.scene;

import static org.junit.jupiter.api.Assertions.*;

import org.joml.Vector3f;
import org.junit.jupiter.api.Test;

import io.github.trimax.venta.engine.utils.ParsingUtil;

final class SceneSoundSourceDTOTest {
    @Test
    void testParseSceneSoundSource() {
        final var dto = ParsingUtil.asObject("""
                {
                  "name": "campfire",
                  "soundSource": "fire.json",
                  "position": { "x": 1.0, "y": 2.0, "z": 3.0 }
                }""", SceneSoundSourceDTO.class);

        assertEquals("campfire", dto.name(), "Name must be parsed");
        assertEquals("fire.json", dto.soundSource(), "Sound source must be parsed from the 'soundSource' key");
        assertEquals(new Vector3f(1, 2, 3), dto.position(), "Position must be parsed");
    }
}
