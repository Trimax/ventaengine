package io.github.trimax.venta.engine.repositories.implementation;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.engine.model.dto.SceneDTO;
import io.github.trimax.venta.engine.model.prefabs.ScenePrefab;
import io.github.trimax.venta.engine.model.prefabs.implementation.ScenePrefabImplementation;
import io.github.trimax.venta.engine.repositories.SceneRepository;
import io.github.trimax.venta.engine.utils.ResourceUtil;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class SceneRepositoryImplementation
        extends AbstractRepositoryImplementation<ScenePrefabImplementation, ScenePrefab>
        implements SceneRepository {
    @Override
    protected ScenePrefabImplementation load(@NonNull final String resourcePath) {
        log.info("Loading scene {}", resourcePath);

        return new ScenePrefabImplementation(ResourceUtil.loadAsObject(String.format("/scenes/%s", resourcePath), SceneDTO.class));
    }

    @Override
    protected void unload(@NonNull final ScenePrefabImplementation entity) {
        log.info("Unloading scene {}", entity.getID());
    }
}
