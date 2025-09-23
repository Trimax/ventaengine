package io.github.trimax.venta.engine.registries.implementation;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.engine.factories.MeshParserFactory;
import io.github.trimax.venta.engine.helpers.GeometryHelper;
import io.github.trimax.venta.engine.layouts.MeshVertexLayout;
import io.github.trimax.venta.engine.model.common.geo.BoundingBox;
import io.github.trimax.venta.engine.model.dto.common.Mesh;
import io.github.trimax.venta.engine.model.entity.MeshEntity;
import io.github.trimax.venta.engine.model.entity.implementation.Abettor;
import io.github.trimax.venta.engine.model.entity.implementation.MeshEntityImplementation;
import io.github.trimax.venta.engine.registries.MeshRegistry;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class MeshRegistryImplementation
        extends AbstractRegistryImplementation<MeshEntityImplementation, MeshEntity, Void>
        implements MeshRegistry {
    private final MeshParserFactory meshParserFactory;
    private final GeometryHelper geometryHelper;
    private final Abettor abettor;

    @Override
    protected MeshEntityImplementation load(@NonNull final String resourcePath, final Void argument) {
        log.info("Loading mesh {}", resourcePath);

        return createMesh(resourcePath, meshParserFactory.get(resourcePath).parse(String.format("/meshes/%s", resourcePath)));
    }

    private MeshEntityImplementation createMesh(final String resourcePath, final Mesh mesh) {
        return abettor.createMesh(geometryHelper.create(resourcePath, MeshVertexLayout.class, mesh.getVerticesArray(),
                        mesh.hasFacets() ? mesh.getFacesArray() : null,
                        mesh.hasEdges() ? mesh.getEdgesArray() : null),
                BoundingBox.of(mesh));
    }

    @Override
    protected void unload(@NonNull final MeshEntityImplementation entity) {
        log.info("Unloading mesh {}", entity.getID());

        geometryHelper.delete(entity.getGeometry());
    }
}