package io.github.trimax.venta.engine.repositories.implementation;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.engine.model.dto.EmitterDTO;
import io.github.trimax.venta.engine.model.prefabs.EmitterPrefab;
import io.github.trimax.venta.engine.model.prefabs.implementation.Abettor;
import io.github.trimax.venta.engine.model.prefabs.implementation.EmitterPrefabImplementation;
import io.github.trimax.venta.engine.repositories.EmitterRepository;
import io.github.trimax.venta.engine.services.ResourceService;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class EmitterRepositoryImplementation
        extends AbstractRepositoryImplementation<EmitterPrefabImplementation, EmitterPrefab>
        implements EmitterRepository {
    private final ResourceService resourceService;
    private final Abettor abettor;

    @Override
    protected EmitterPrefabImplementation load(@NonNull final String resourcePath) {
        log.info("Loading emitter {}", resourcePath);

        return abettor.createEmitter(resourceService.getAsObject(String.format("/emitters/%s", resourcePath), EmitterDTO.class));
    }

    @Override
    protected void unload(@NonNull final EmitterPrefabImplementation prefab) {
        log.info("Unloading emitter {}", prefab.getID());
    }
}
