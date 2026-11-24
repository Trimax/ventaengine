package io.github.trimax.venta.engine.model.dto;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import io.github.trimax.venta.engine.utils.ParsingUtil;

final class ObjectDtoLodTest {
    private static final String MODEL = "{\"name\": \"tree\", \"value\": {\"mesh\": \"%s\", \"transform\": {"
            + "\"position\": {\"x\": 0, \"y\": 0, \"z\": 0}, \"rotation\": {\"x\": 0, \"y\": 0, \"z\": 0}, \"scale\": {\"x\": 1, \"y\": 1, \"z\": 1}}}}";

    private static ObjectDTO parse(final String lods) {
        return ParsingUtil.asObject("{\"program\": \"default.json\", \"root\": " + MODEL.formatted("tree.obj") + lods + "}", ObjectDTO.class);
    }

    @Test
    void testObjectWithoutLevelsIsReadAsBefore() {
        final var object = parse("");

        assertNull(object.lods());
        assertEquals("default.json", object.program());
    }

    @Test
    void testLevelsAreReadWithTheirSizesModelsAndKind() {
        final var object = parse(", \"lods\": [{\"screenSize\": 0.25, \"root\": " + MODEL.formatted("tree-1.obj")
                + "}, {\"screenSize\": 0.05, \"facing\": true, \"root\": " + MODEL.formatted("card.obj") + "}]");

        assertEquals(2, object.lods().size());
        assertEquals(0.25f, object.lods().get(0).screenSize());
        assertNull(object.lods().get(0).facing(), "A level is a model unless it says it is a card");
        assertEquals("tree-1.obj", object.lods().get(0).root().value().mesh());
        assertEquals(0.05f, object.lods().get(1).screenSize());
        assertTrue(object.lods().get(1).facing());
        assertEquals("card.obj", object.lods().get(1).root().value().mesh());
    }

    @Test
    void testLevelWithoutASizeIsRefused() {
        assertThrows(RuntimeException.class, () -> parse(", \"lods\": [{\"root\": " + MODEL.formatted("tree-1.obj") + "}]"));
    }
}
