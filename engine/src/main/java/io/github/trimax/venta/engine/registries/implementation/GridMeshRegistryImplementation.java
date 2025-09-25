package io.github.trimax.venta.engine.registries.implementation;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.engine.helpers.GeometryHelper;
import io.github.trimax.venta.engine.layouts.GridMeshVertexLayout;
import io.github.trimax.venta.engine.model.dto.GridMeshDTO;
import io.github.trimax.venta.engine.model.entity.GridMeshEntity;
import io.github.trimax.venta.engine.model.entity.implementation.Abettor;
import io.github.trimax.venta.engine.model.entity.implementation.GridMeshEntityImplementation;
import io.github.trimax.venta.engine.registries.GridMeshRegistry;
import io.github.trimax.venta.engine.services.ResourceService;
import io.github.trimax.venta.engine.utils.GeometryUtil;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class GridMeshRegistryImplementation
        extends AbstractRegistryImplementation<GridMeshEntityImplementation, GridMeshEntity, Void>
        implements GridMeshRegistry {
    private final ResourceService resourceService;
    private final GeometryHelper geometryHelper;
    private final Abettor abettor;

    @Override
    protected GridMeshEntityImplementation load(@NonNull final String resourcePath, final Void argument) {
        log.info("Loading grid mesh {}", resourcePath);

        final var gridMeshDTO = resourceService.getAsObject(String.format("/gridmeshes/%s", resourcePath), GridMeshDTO.class);
        final var grid = GeometryUtil.createGrid(gridMeshDTO.size(), gridMeshDTO.segments());

        return abettor.createGridMesh(geometryHelper.create(resourcePath, GridMeshVertexLayout.class, grid.vertices(), grid.facets(), null));
    }

    @Override
    protected void unload(@NonNull final GridMeshEntityImplementation entity) {
        log.info("Unloading grid mesh {}", entity.getID());

        geometryHelper.delete(entity.getGeometry());
    }
}
