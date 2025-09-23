package io.github.trimax.venta.engine.repositories.implementation;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.engine.helpers.GeometryHelper;
import io.github.trimax.venta.engine.layouts.GridMeshVertexLayout;
import io.github.trimax.venta.engine.model.common.shared.Wave;
import io.github.trimax.venta.engine.model.dto.GridMeshDTO;
import io.github.trimax.venta.engine.model.prefabs.GridMeshPrefab;
import io.github.trimax.venta.engine.model.prefabs.implementation.Abettor;
import io.github.trimax.venta.engine.model.prefabs.implementation.GridMeshPrefabImplementation;
import io.github.trimax.venta.engine.registries.implementation.MaterialRegistryImplementation;
import io.github.trimax.venta.engine.registries.implementation.ProgramRegistryImplementation;
import io.github.trimax.venta.engine.repositories.GridMeshRepository;
import io.github.trimax.venta.engine.services.ResourceService;
import io.github.trimax.venta.engine.utils.GeometryUtil;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import one.util.streamex.StreamEx;

@Slf4j
@Component
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class GridMeshRepositoryImplementation
        extends AbstractRepositoryImplementation<GridMeshPrefabImplementation, GridMeshPrefab>
        implements GridMeshRepository {
    private final MaterialRegistryImplementation materialRegistry;
    private final ProgramRegistryImplementation programRegistry;
    private final ResourceService resourceService;
    private final GeometryHelper geometryHelper;
    private final Abettor abettor;

    @Override
    protected GridMeshPrefabImplementation load(@NonNull final String resourcePath) {
        log.info("Loading grid mesh {}", resourcePath);

        final var gridMeshDTO = resourceService.getAsObject(String.format("/gridmeshes/%s", resourcePath), GridMeshDTO.class);
        final var grid = GeometryUtil.createGrid(gridMeshDTO.size(), gridMeshDTO.segments());

        return abettor.createGridMesh(programRegistry.get(gridMeshDTO.program()), materialRegistry.get(gridMeshDTO.material()),
                geometryHelper.create(resourcePath, GridMeshVertexLayout.class, grid.vertices(), grid.facets(), null),
                StreamEx.of(gridMeshDTO.waves()).map(Wave::new).toList());
    }

    @Override
    protected void unload(@NonNull final GridMeshPrefabImplementation entity) {
        log.info("Unloading grid mesh {}", entity.getID());

        geometryHelper.delete(entity.getGeometry());
    }
}
