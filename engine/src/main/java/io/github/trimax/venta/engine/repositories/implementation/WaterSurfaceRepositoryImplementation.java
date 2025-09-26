package io.github.trimax.venta.engine.repositories.implementation;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.engine.model.common.shared.Noise;
import io.github.trimax.venta.engine.model.common.shared.Wave;
import io.github.trimax.venta.engine.model.common.water.WaterFoam;
import io.github.trimax.venta.engine.model.common.water.WaterMaterial;
import io.github.trimax.venta.engine.model.dto.WaterSurfaceDTO;
import io.github.trimax.venta.engine.model.prefabs.WaterSurfacePrefab;
import io.github.trimax.venta.engine.model.prefabs.implementation.Abettor;
import io.github.trimax.venta.engine.model.prefabs.implementation.WaterSurfacePrefabImplementation;
import io.github.trimax.venta.engine.registries.implementation.GridMeshRegistryImplementation;
import io.github.trimax.venta.engine.registries.implementation.ProgramRegistryImplementation;
import io.github.trimax.venta.engine.repositories.WaterSurfaceRepository;
import io.github.trimax.venta.engine.services.ResourceService;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import one.util.streamex.StreamEx;

@Slf4j
@Component
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class WaterSurfaceRepositoryImplementation
        extends AbstractRepositoryImplementation<WaterSurfacePrefabImplementation, WaterSurfacePrefab>
        implements WaterSurfaceRepository {
    private final GridMeshRegistryImplementation gridMeshRegistry;
    private final ProgramRegistryImplementation programRegistry;
    private final ResourceService resourceService;
    private final Abettor abettor;

    @Override
    protected WaterSurfacePrefabImplementation load(@NonNull final String resourcePath) {
        log.info("Loading grid mesh {}", resourcePath);

        final var waterSurfaceDTO = resourceService.getAsObject(String.format("/surfaces/water/%s", resourcePath), WaterSurfaceDTO.class);

        return abettor.createWaterSurface(gridMeshRegistry.get(waterSurfaceDTO.gridMesh()),
                programRegistry.get(waterSurfaceDTO.program()),
                new WaterMaterial(waterSurfaceDTO.material()),
                new WaterFoam(waterSurfaceDTO.foam()),
                StreamEx.of(waterSurfaceDTO.noises()).map(Noise::new).toList(),
                StreamEx.of(waterSurfaceDTO.waves()).map(Wave::new).toList());
    }

    @Override
    protected void unload(@NonNull final WaterSurfacePrefabImplementation entity) {
        log.info("Unloading water surface {}", entity.getID());
    }
}
