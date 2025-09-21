package io.github.trimax.venta.engine.repositories.implementation;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.engine.layouts.BillboardVertexLayout;
import io.github.trimax.venta.engine.memory.Memory;
import io.github.trimax.venta.engine.model.common.geo.Buffer;
import io.github.trimax.venta.engine.model.common.geo.Geometry;
import io.github.trimax.venta.engine.model.dto.BillboardDTO;
import io.github.trimax.venta.engine.model.prefabs.BillboardPrefab;
import io.github.trimax.venta.engine.model.prefabs.implementation.Abettor;
import io.github.trimax.venta.engine.model.prefabs.implementation.BillboardPrefabImplementation;
import io.github.trimax.venta.engine.registries.implementation.ProgramRegistryImplementation;
import io.github.trimax.venta.engine.registries.implementation.SpriteRegistryImplementation;
import io.github.trimax.venta.engine.repositories.BillboardRepository;
import io.github.trimax.venta.engine.services.ResourceService;
import io.github.trimax.venta.engine.utils.LayoutUtil;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import static io.github.trimax.venta.engine.definitions.Definitions.COUNT_VERTICES_PER_FACET;
import static io.github.trimax.venta.engine.definitions.GeometryDefinitions.PARTICLE_INDICES;
import static io.github.trimax.venta.engine.definitions.GeometryDefinitions.PARTICLE_VERTICES;
import static org.lwjgl.opengl.GL15C.*;
import static org.lwjgl.opengl.GL30C.glBindVertexArray;

@Slf4j
@Component
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class BillboardRepositoryImplementation
        extends AbstractRepositoryImplementation<BillboardPrefabImplementation, BillboardPrefab>
        implements BillboardRepository {
    private final ProgramRegistryImplementation programRegistry;
    private final SpriteRegistryImplementation spriteRegistry;
    private final ResourceService resourceService;
    private final Abettor abettor;
    private final Memory memory;

    @Override
    protected BillboardPrefabImplementation load(@NonNull final String resourcePath) {
        log.info("Loading billboard {}", resourcePath);

        final var billboardDTO = resourceService.getAsObject(String.format("/billboards/%s", resourcePath), BillboardDTO.class);

        final int vertexArrayObjectID = memory.getVertexArrays().create("Billboard %s VAO", resourcePath);
        final int verticesBufferID = memory.getBuffers().create("Billboard %s vertex buffer", resourcePath);
        final int facetsBufferID = memory.getBuffers().create("Billboard %s face buffer", resourcePath);

        glBindVertexArray(vertexArrayObjectID);

        // vertex buffer
        glBindBuffer(GL_ARRAY_BUFFER, verticesBufferID);
        glBufferData(GL_ARRAY_BUFFER, PARTICLE_VERTICES, GL_STATIC_DRAW);

        LayoutUtil.bind(BillboardVertexLayout.class);

        // index buffer
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, facetsBufferID);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, PARTICLE_INDICES, GL_STATIC_DRAW);

        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindVertexArray(0);

        return abettor.createBillboard(programRegistry.get(billboardDTO.program()),
                spriteRegistry.get(billboardDTO.sprite()),
                new Geometry(vertexArrayObjectID,
                        new Buffer(verticesBufferID, PARTICLE_VERTICES.length / BillboardVertexLayout.getFloatsCount(), PARTICLE_VERTICES.length),
                        new Buffer(facetsBufferID, PARTICLE_INDICES.length / COUNT_VERTICES_PER_FACET, PARTICLE_INDICES.length),
                        null),
                billboardDTO.scale());
    }

    @Override
    protected void unload(@NonNull final BillboardPrefabImplementation entity) {
        log.info("Unloading billboard {}", entity.getID());

        memory.getVertexArrays().delete(entity.getGeometry().objectID());
        unload(entity.getGeometry().vertices());
        unload(entity.getGeometry().facets());
    }

    private void unload(@NonNull final Buffer buffer) {
        memory.getBuffers().delete(buffer.id());
    }
}
