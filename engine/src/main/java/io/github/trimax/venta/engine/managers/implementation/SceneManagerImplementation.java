package io.github.trimax.venta.engine.managers.implementation;

import java.util.Optional;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.engine.exceptions.UnknownInstanceException;
import io.github.trimax.venta.engine.managers.SceneManager;
import io.github.trimax.venta.engine.model.common.scene.SceneBillboard;
import io.github.trimax.venta.engine.model.common.scene.SceneEmitter;
import io.github.trimax.venta.engine.model.common.scene.SceneLight;
import io.github.trimax.venta.engine.model.common.scene.SceneObject;
import io.github.trimax.venta.engine.model.common.scene.SceneSoundSource;
import io.github.trimax.venta.engine.model.instance.SceneInstance;
import io.github.trimax.venta.engine.model.instance.implementation.Abettor;
import io.github.trimax.venta.engine.model.instance.implementation.SceneInstanceImplementation;
import io.github.trimax.venta.engine.model.instance.implementation.SoundSourceInstanceImplementation;
import io.github.trimax.venta.engine.model.prefabs.ScenePrefab;
import io.github.trimax.venta.engine.model.prefabs.implementation.ScenePrefabImplementation;
import io.github.trimax.venta.engine.repositories.implementation.BillboardRepositoryImplementation;
import io.github.trimax.venta.engine.repositories.implementation.EmitterRepositoryImplementation;
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
    private final BillboardManagerImplementation billboardManager;
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

        for (final var objectPrefab : prefab.getObjects()) {
            final var object = objectManager.create(String.format("%s-%s", name, objectPrefab.getName()), objectPrefab.getPrefab());
            Optional.of(objectPrefab).map(SceneObject::getPosition).ifPresent(object::setPosition);
            Optional.of(objectPrefab).map(SceneObject::getAngles).ifPresent(object::setRotation);
            Optional.of(objectPrefab).map(SceneObject::getScale).ifPresent(object::setScale);

            scene.add(object);
        }

        for (final var sceneLight : prefab.getLights()) {
            final var light = lightManager.create(String.format("%s-%s", name, sceneLight.getName()), sceneLight.getPrefab());
            Optional.of(sceneLight).map(SceneLight::getPosition).ifPresent(light::setPosition);
            Optional.of(sceneLight).map(SceneLight::getDirection).ifPresent(light::setDirection);

            scene.add(light);
        }

        for (final var emitterPrefab : prefab.getEmitters()) {
            final var emitter = emitterManager.create(String.format("%s-%s", name, emitterPrefab.getName()), emitterPrefab.getPrefab());
            Optional.of(emitterPrefab).map(SceneEmitter::getPosition).ifPresent(emitter::setPosition);

            scene.add(emitter);
        }

        for (final var dto : prefab.getBillboards()) {
            final var billboard = billboardManager.create(String.format("%s-%s", name, dto.getName()), dto.getPrefab());
            Optional.of(dto).map(SceneBillboard::getPosition).ifPresent(billboard::setPosition);
            Optional.of(dto).map(SceneBillboard::getSize).ifPresent(billboard::setSize);

            scene.add(billboard);
        }

        for (final var dto : prefab.getSoundSources()) {
            final var soundSource = soundSourceManager.create(String.format("%s-%s", name, dto.getName()), dto.getPrefab());
            Optional.of(dto).map(SceneSoundSource::getPosition).ifPresent(soundSource::setPosition);

            scene.add(soundSource);
        }
        Optional.ofNullable(prefab.getAmbientLight()).ifPresent(scene::setAmbientLight);
        Optional.ofNullable(prefab.getFog()).ifPresent(scene::setFog);
        Optional.ofNullable(prefab.getSkybox()).ifPresent(scene::setSkybox);

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
