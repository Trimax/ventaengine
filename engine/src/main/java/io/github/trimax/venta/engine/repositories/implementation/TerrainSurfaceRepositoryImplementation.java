package io.github.trimax.venta.engine.repositories.implementation;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.engine.model.dto.TerrainSurfaceDTO;
import io.github.trimax.venta.engine.model.prefabs.TerrainSurfacePrefab;
import io.github.trimax.venta.engine.model.prefabs.implementation.Abettor;
import io.github.trimax.venta.engine.model.prefabs.implementation.TerrainSurfacePrefabImplementation;
import io.github.trimax.venta.engine.registries.implementation.GridMeshRegistryImplementation;
import io.github.trimax.venta.engine.registries.implementation.ProgramRegistryImplementation;
import io.github.trimax.venta.engine.registries.implementation.TextureRegistryImplementation;
import io.github.trimax.venta.engine.repositories.TerrainSurfaceRepository;
import io.github.trimax.venta.engine.services.ResourceService;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class TerrainSurfaceRepositoryImplementation
        extends AbstractRepositoryImplementation<TerrainSurfacePrefabImplementation, TerrainSurfacePrefab>
        implements TerrainSurfaceRepository {
    private final GridMeshRegistryImplementation gridMeshRegistry;
    private final ProgramRegistryImplementation programRegistry;
    private final TextureRegistryImplementation textureRegistry;
    private final ResourceService resourceService;
    private final Abettor abettor;

    @Override
    protected TerrainSurfacePrefabImplementation load(@NonNull final String resourcePath) {
        log.info("Loading terrain surface {}", resourcePath);

        final var terrainSurfaceDTO = resourceService.getAsObject(String.format("/surfaces/terrain/%s", resourcePath), TerrainSurfaceDTO.class);

        return abettor.createTerrainSurface(gridMeshRegistry.get(terrainSurfaceDTO.gridMesh()),
                programRegistry.get(terrainSurfaceDTO.program()),
                textureRegistry.get(terrainSurfaceDTO.elevation().heightmap()),
                terrainSurfaceDTO.elevation().factor());
    }

    @Override
    protected void unload(@NonNull final TerrainSurfacePrefabImplementation entity) {
        log.info("Unloading terrain surface {}", entity.getID());
    }
}
