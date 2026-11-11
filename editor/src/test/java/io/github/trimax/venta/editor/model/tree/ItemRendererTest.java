package io.github.trimax.venta.editor.model.tree;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import io.github.trimax.venta.engine.enums.GroupType;

final class ItemRendererTest {
    @Test
    void testEveryGroupHasRenderer() {
        for (final var type : GroupType.values())
            assertNotNull(ItemRenderer.of(type), type + " must have a renderer, otherwise its properties panel fails");
    }
}
