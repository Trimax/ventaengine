package io.github.trimax.venta.engine.managers.implementation;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.engine.exceptions.UnknownInstanceException;
import io.github.trimax.venta.engine.managers.SceneManager;
import io.github.trimax.venta.engine.model.dto.SceneLightDTO;
import io.github.trimax.venta.engine.model.dto.SceneObjectDTO;
import io.github.trimax.venta.engine.model.instance.SceneInstance;
import io.github.trimax.venta.engine.model.instance.implementation.Abettor;
import io.github.trimax.venta.engine.model.instance.implementation.SceneInstanceImplementation;
import io.github.trimax.venta.engine.model.prefabs.ScenePrefab;
import io.github.trimax.venta.engine.model.prefabs.implementation.ScenePrefabImplementation;
import io.github.trimax.venta.engine.registries.implementation.CubemapRegistryImplementation;
import io.github.trimax.venta.engine.repositories.implementation.LightRepositoryImplementation;
import io.github.trimax.venta.engine.repositories.implementation.ObjectRepositoryImplementation;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class SceneManagerImplementation
        extends AbstractManagerImplementation<SceneInstanceImplementation, SceneInstance>
        implements SceneManager {
    private final ObjectRepositoryImplementation objectRepository;
    private final LightRepositoryImplementation lightRepository;
    private final CubemapRegistryImplementation cubemapRegistry;
    private final ObjectManagerImplementation objectManager;
    private final LightManagerImplementation lightManager;
    private final Abettor abettor;

    @Getter(onMethod_ = @__(@Override))
    private SceneInstanceImplementation current;

    @Override
    public SceneInstanceImplementation create(@NonNull final String name, @NonNull final ScenePrefab prefab) {
        if (prefab instanceof ScenePrefabImplementation scene)
            return create(name, scene);

        throw new UnknownInstanceException(prefab.getClass());
    }

    private SceneInstanceImplementation create(@NonNull final String name, @NonNull final ScenePrefabImplementation prefab) {
        final var scene = create(name);

        final var sceneDTO = prefab.getDto();
        if (sceneDTO.hasObjects())
            for (final var sceneObject : sceneDTO.objects()) {
                final var object = objectManager.create(sceneObject.name(), objectRepository.get(sceneObject.object()));
                Optional.of(sceneObject).map(SceneObjectDTO::position).ifPresent(object::setPosition);
                Optional.of(sceneObject).map(SceneObjectDTO::angles).ifPresent(object::setRotation);
                Optional.of(sceneObject).map(SceneObjectDTO::scale).ifPresent(object::setScale);

                scene.add(object);
            }

        if (sceneDTO.hasLights())
            for (final var sceneLight : sceneDTO.lights()) {
                final var light = lightManager.create(sceneLight.name(), lightRepository.get(sceneLight.light()));
                Optional.of(sceneLight).map(SceneLightDTO::position).ifPresent(light::setPosition);
                Optional.of(sceneLight).map(SceneLightDTO::direction).ifPresent(light::setDirection);

                scene.add(light);
            }

        Optional.ofNullable(sceneDTO.ambientLight()).ifPresent(scene::setAmbientLight);

        Optional.ofNullable(sceneDTO.fog()).ifPresent(scene::setFog);
        Optional.ofNullable(sceneDTO.skybox()).map(cubemapRegistry::get).ifPresent(scene::setSkybox);

        return store(scene);
    }

    @Override
    public SceneInstanceImplementation create(@NonNull final String name) {
        log.info("Creating scene {}", name);

        return store(abettor.createScene(name));
    }

    @Override
    public void setCurrent(@NonNull final SceneInstance scene) {
        if (scene instanceof SceneInstanceImplementation entity)
            this.current = entity;
    }

    @Override
    public void delete(@NonNull final SceneInstance scene) {
        if (scene == getCurrent()) {
            log.error("Scene {} can't be deleted because it is currently selected", scene.getID());
            return;
        }

        if (scene instanceof SceneInstanceImplementation entity)
            delete(entity);
    }

    @Override
    protected void destroy(final SceneInstanceImplementation scene) {
        log.info("Destroying scene {} ({})", scene.getID(), scene.getName());
    }
}
