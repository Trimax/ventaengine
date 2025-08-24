package io.github.trimax.venta.engine.repositories.implementation;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.engine.model.dto.LightPrefabDTO;
import io.github.trimax.venta.engine.model.prefabs.LightPrefab;
import io.github.trimax.venta.engine.model.prefabs.implementation.Abettor;
import io.github.trimax.venta.engine.model.prefabs.implementation.LightPrefabImplementation;
import io.github.trimax.venta.engine.repositories.LightRepository;
import io.github.trimax.venta.engine.services.ResourceService;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class LightRepositoryImplementation
        extends AbstractRepositoryImplementation<LightPrefabImplementation, LightPrefab>
        implements LightRepository {
    private final ResourceService resourceService;
    private final Abettor abettor;

    @Override
    protected LightPrefabImplementation load(@NonNull final String resourcePath) {
        log.info("Loading light {}", resourcePath);

        return abettor.createLight(resourceService.getAsObject(String.format("/lights/%s", resourcePath), LightPrefabDTO.class));
    }

    @Override
    protected void unload(@NonNull final LightPrefabImplementation entity) {
        log.info("Unloading light {}", entity.getID());
    }
}
