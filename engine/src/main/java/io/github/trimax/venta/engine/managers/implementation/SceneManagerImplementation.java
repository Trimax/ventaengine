package io.github.trimax.venta.engine.managers.implementation;

import java.util.Optional;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.engine.exceptions.UnknownInstanceException;
import io.github.trimax.venta.engine.managers.SceneManager;
import io.github.trimax.venta.engine.model.dto.scene.SceneBillboardDTO;
import io.github.trimax.venta.engine.model.dto.scene.SceneEmitterDTO;
import io.github.trimax.venta.engine.model.dto.scene.SceneLightDTO;
import io.github.trimax.venta.engine.model.dto.scene.SceneObjectDTO;
import io.github.trimax.venta.engine.model.dto.scene.SceneSoundSourceDTO;
import io.github.trimax.venta.engine.model.instance.SceneInstance;
import io.github.trimax.venta.engine.model.instance.implementation.Abettor;
import io.github.trimax.venta.engine.model.instance.implementation.SceneInstanceImplementation;
import io.github.trimax.venta.engine.model.instance.implementation.SoundSourceInstanceImplementation;
import io.github.trimax.venta.engine.model.prefabs.ScenePrefab;
import io.github.trimax.venta.engine.model.prefabs.implementation.ScenePrefabImplementation;
import io.github.trimax.venta.engine.registries.implementation.CubemapRegistryImplementation;
import io.github.trimax.venta.engine.repositories.implementation.BillboardRepositoryImplementation;
import io.github.trimax.venta.engine.repositories.implementation.EmitterRepositoryImplementation;
import io.github.trimax.venta.engine.repositories.implementation.LightRepositoryImplementation;
import io.github.trimax.venta.engine.repositories.implementation.ObjectRepositoryImplementation;
import io.github.trimax.venta.engine.repositories.implementation.SoundSourceRepositoryImplementation;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class SceneManagerImplementation
        extends AbstractManagerImplementation<SceneInstanceImplementation, SceneInstance>
        implements SceneManager {
    private final SoundSourceRepositoryImplementation soundSourceRepository;
    private final BillboardRepositoryImplementation billboardRepository;
    private final SoundSourceManagerImplementation soundSourceManager;
    private final EmitterRepositoryImplementation emitterRepository;
    private final ObjectRepositoryImplementation objectRepository;
    private final BillboardManagerImplementation billboardManager;
    private final LightRepositoryImplementation lightRepository;
    private final CubemapRegistryImplementation cubemapRegistry;
    private final EmitterManagerImplementation emitterManager;
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
            for (final var dto : sceneDTO.objects()) {
                final var object = objectManager.create(dto.name(), objectRepository.get(dto.object()));
                Optional.of(dto).map(SceneObjectDTO::position).ifPresent(object::setPosition);
                Optional.of(dto).map(SceneObjectDTO::angles).ifPresent(object::setRotation);
                Optional.of(dto).map(SceneObjectDTO::scale).ifPresent(object::setScale);

                scene.add(object);
            }

        if (sceneDTO.hasLights())
            for (final var dto : sceneDTO.lights()) {
                final var light = lightManager.create(dto.name(), lightRepository.get(dto.light()));
                Optional.of(dto).map(SceneLightDTO::position).ifPresent(light::setPosition);
                Optional.of(dto).map(SceneLightDTO::direction).ifPresent(light::setDirection);

                scene.add(light);
            }

        if (sceneDTO.hasEmitters())
            for (final var dto : sceneDTO.emitters()) {
                final var emitter = emitterManager.create(dto.name(), emitterRepository.get(dto.emitter()));
                Optional.of(dto).map(SceneEmitterDTO::position).ifPresent(emitter::setPosition);

                scene.add(emitter);
            }

        if (sceneDTO.hasBillboards())
            for (final var dto : sceneDTO.billboards()) {
                final var billboard = billboardManager.create(dto.name(), billboardRepository.get(dto.billboard()));
                Optional.of(dto).map(SceneBillboardDTO::position).ifPresent(billboard::setPosition);
                Optional.of(dto).map(SceneBillboardDTO::size).ifPresent(billboard::setSize);

                scene.add(billboard);
            }

        if (sceneDTO.hasSoundSources())
            for (final var dto : sceneDTO.soundSources()) {
                final var soundSource = soundSourceManager.create(dto.name(), soundSourceRepository.get(dto.sourceSource()));
                Optional.of(dto).map(SceneSoundSourceDTO::position).ifPresent(soundSource::setPosition);

                scene.add(soundSource);
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
        if (scene instanceof SceneInstanceImplementation entity) {
            if (this.current != null)
                this.current.getSoundSources().forEach(SoundSourceInstanceImplementation::stop);

            this.current = entity;
        }
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
