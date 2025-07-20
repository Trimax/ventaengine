package com.venta.engine.model;

import com.venta.engine.model.dto.ObjectDTO;
import com.venta.engine.utils.Generator;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public final class ObjectDTOTest {
    @Test
    public void testBaking() {
        final var object = new ObjectDTO("Object", "Test",
                Generator.createRandomVertexList(10), Generator.createRandomFacetList(20));

        final var bakedObject = object.bake();
        assertNotNull(bakedObject, "Baked object should be created");

        assertNotNull(bakedObject.vertices(), "Baked object should contain vertices");
        assertEquals(12 * object.vertices().size(), bakedObject.vertices().length, "The number of vertices must be correct");

        assertNotNull(bakedObject.facets(), "Baked object should contain facets");
        assertEquals(3 * object.facets().size(), bakedObject.facets().length, "The number of facets must be correct");
    }
}
