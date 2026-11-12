package io.github.trimax.venta.engine.model.dto.scene;

import static org.junit.jupiter.api.Assertions.*;

import org.joml.Vector3f;
import org.junit.jupiter.api.Test;

import io.github.trimax.venta.engine.model.dto.SceneDTO;
import io.github.trimax.venta.engine.utils.ParsingUtil;

final class SceneSurfaceDTOTest {
    @Test
    void testParseSceneSurfaces() {
        final var dto = ParsingUtil.asObject("""
                {
                  "terrainSurfaces": [
                    {
                      "name": "Valley",
                      "terrainSurface": "valley.json",
                      "position": { "x": 0.0, "y": 0.0, "z": 0.0 },
                      "scale": { "x": 1.0, "y": 0.5, "z": 1.0 }
                    }
                  ],
                  "waterSurfaces": [
                    {
                      "name": "Lake",
                      "waterSurface": "lake.json",
                      "position": { "x": 1.0, "y": -10.0, "z": 2.0 }
                    }
                  ]
                }""", SceneDTO.class);

        final var terrain = dto.terrainSurfaces().getFirst();
        assertEquals("Valley", terrain.name(), "Terrain name must be parsed");
        assertEquals("valley.json", terrain.terrainSurface(), "Terrain surface must be parsed from the 'terrainSurface' key");
        assertEquals(new Vector3f(1, 0.5f, 1), terrain.scale(), "Terrain scale must be parsed");
        assertNull(terrain.angles(), "Terrain angles are optional");

        final var water = dto.waterSurfaces().getFirst();
        assertEquals("lake.json", water.waterSurface(), "Water surface must be parsed from the 'waterSurface' key");
        assertEquals(new Vector3f(1, -10, 2), water.position(), "Water position must be parsed");
        assertNull(water.scale(), "Water scale is optional");
    }

    @Test
    void testSurfacesAreOptional() {
        final var dto = ParsingUtil.asObject("{}", SceneDTO.class);

        assertNull(dto.terrainSurfaces(), "A scene without terrain surfaces must parse");
        assertNull(dto.waterSurfaces(), "A scene without water surfaces must parse");
    }
}
