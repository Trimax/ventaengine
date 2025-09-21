package io.github.trimax.venta.engine.repositories.implementation;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.engine.memory.Memory;
import io.github.trimax.venta.engine.model.common.geo.Buffer;
import io.github.trimax.venta.engine.model.common.geo.Geometry;
import io.github.trimax.venta.engine.model.dto.GridMeshDTO;
import io.github.trimax.venta.engine.model.prefabs.GridMeshPrefab;
import io.github.trimax.venta.engine.model.prefabs.implementation.Abettor;
import io.github.trimax.venta.engine.model.prefabs.implementation.GridMeshPrefabImplementation;
import io.github.trimax.venta.engine.registries.implementation.ProgramRegistryImplementation;
import io.github.trimax.venta.engine.repositories.GridMeshRepository;
import io.github.trimax.venta.engine.services.ResourceService;
import io.github.trimax.venta.engine.utils.GeometryUtil;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.lwjgl.system.MemoryUtil;

import static io.github.trimax.venta.engine.definitions.Definitions.*;
import static org.lwjgl.opengl.GL15C.*;
import static org.lwjgl.opengl.GL20C.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20C.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30C.glBindVertexArray;

@Slf4j
@Component
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class GridMeshRepositoryImplementation
        extends AbstractRepositoryImplementation<GridMeshPrefabImplementation, GridMeshPrefab>
        implements GridMeshRepository {
    private final ProgramRegistryImplementation programRegistry;
    private final ResourceService resourceService;
    private final Abettor abettor;
    private final Memory memory;

    @Override
    protected GridMeshPrefabImplementation load(@NonNull final String resourcePath) {
        log.info("Loading grid mesh {}", resourcePath);

        final var gridMeshDTO = resourceService.getAsObject(String.format("/gridmeshes/%s", resourcePath), GridMeshDTO.class);
        final var grid = GeometryUtil.createGrid(gridMeshDTO.size(), gridMeshDTO.segments());

        final var vertexArrayObjectID = memory.getVertexArrays().create("Grid mesh %s vertex array buffer", resourcePath);
        final var verticesBufferID = memory.getBuffers().create("Grid mesh %s vertex buffer", resourcePath);
        final var facetsBufferID = memory.getBuffers().create("Grid mesh %s facet buffer", resourcePath);

        glBindVertexArray(vertexArrayObjectID);

        // vertex buffer
        final var vertexBuffer = MemoryUtil.memAllocFloat(grid.vertices().length);
        vertexBuffer.put(grid.vertices()).flip();

        glBindBuffer(GL_ARRAY_BUFFER, verticesBufferID);
        glBufferData(GL_ARRAY_BUFFER, vertexBuffer, GL_STATIC_DRAW);

        // index buffer
        final var indexBuffer = MemoryUtil.memAllocInt(grid.indices().length);
        indexBuffer.put(grid.indices()).flip();

        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, facetsBufferID);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indexBuffer, GL_STATIC_DRAW);

        final var stride = COUNT_FLOATS_PER_VERTEX_GRIDMESH * Float.BYTES;

        // layout(location = 0) -> position
        glEnableVertexAttribArray(0);
        glVertexAttribPointer(0, COUNT_FLOATS_PER_POSITION, GL_FLOAT, false, stride, 0);

        // layout(location = 1) -> texture coordinates
        glEnableVertexAttribArray(1);
        glVertexAttribPointer(1, COUNT_FLOATS_PER_TEXTURE_COORDINATES, GL_FLOAT, false, stride, 3 * Float.BYTES);

        glBindVertexArray(0);

        MemoryUtil.memFree(vertexBuffer);
        MemoryUtil.memFree(indexBuffer);

        return abettor.createGridMesh(programRegistry.get(gridMeshDTO.program()),
                new Geometry(vertexArrayObjectID,
                        new Buffer(verticesBufferID, grid.vertices().length / COUNT_FLOATS_PER_VERTEX_GRIDMESH, grid.vertices().length),
                        new Buffer(facetsBufferID, grid.indices().length / COUNT_VERTICES_PER_FACET, grid.indices().length),
                        null),
                gridMeshDTO.waves());
    }

    @Override
    protected void unload(@NonNull final GridMeshPrefabImplementation entity) {
        log.info("Unloading grid mesh {}", entity.getID());

        memory.getBuffers().delete(entity.getGeometry().facets().id());
        memory.getBuffers().delete(entity.getGeometry().vertices().id());
        memory.getVertexArrays().delete(entity.getGeometry().objectID());
    }
}
