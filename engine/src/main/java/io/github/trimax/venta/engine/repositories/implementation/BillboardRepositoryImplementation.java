package io.github.trimax.venta.engine.repositories.implementation;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.engine.definitions.DefinitionsGeometry;
import io.github.trimax.venta.engine.helpers.GeometryHelper;
import io.github.trimax.venta.engine.layouts.BillboardVertexLayout;
import io.github.trimax.venta.engine.model.dto.BillboardDTO;
import io.github.trimax.venta.engine.model.prefabs.BillboardPrefab;
import io.github.trimax.venta.engine.model.prefabs.implementation.Abettor;
import io.github.trimax.venta.engine.model.prefabs.implementation.BillboardPrefabImplementation;
import io.github.trimax.venta.engine.registries.implementation.ProgramRegistryImplementation;
import io.github.trimax.venta.engine.registries.implementation.SpriteRegistryImplementation;
import io.github.trimax.venta.engine.repositories.BillboardRepository;
import io.github.trimax.venta.engine.services.ResourceService;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class BillboardRepositoryImplementation
        extends AbstractRepositoryImplementation<BillboardPrefabImplementation, BillboardPrefab>
        implements BillboardRepository {
    private final ProgramRegistryImplementation programRegistry;
    private final SpriteRegistryImplementation spriteRegistry;
    private final ResourceService resourceService;
    private final GeometryHelper geometryHelper;
    private final Abettor abettor;

    @Override
    protected BillboardPrefabImplementation load(@NonNull final String resourcePath) {
        log.info("Loading billboard {}", resourcePath);

        return create(resourcePath, resourceService.getAsObject(String.format("/billboards/%s", resourcePath), BillboardDTO.class));
    }

    private BillboardPrefabImplementation create(final String resourcePath, final BillboardDTO billboardDTO) {
        return abettor.createBillboard(programRegistry.get(billboardDTO.program()),
                spriteRegistry.get(billboardDTO.sprite()),
                geometryHelper.create(resourcePath, BillboardVertexLayout.class,
                        DefinitionsGeometry.Particle.VERTICES, DefinitionsGeometry.Particle.FACETS, null),
                billboardDTO.scale());
    }

    @Override
    protected void unload(@NonNull final BillboardPrefabImplementation entity) {
        log.info("Unloading billboard {}", entity.getID());

        geometryHelper.delete(entity.getGeometry());
    }
}
