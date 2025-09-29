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
import io.github.trimax.venta.engine.model.instance.BillboardInstance;
import io.github.trimax.venta.engine.model.instance.ObjectInstance;
import io.github.trimax.venta.engine.model.instance.SceneInstance;
import io.github.trimax.venta.engine.model.instance.implementation.Abettor;
import io.github.trimax.venta.engine.model.instance.implementation.EmitterInstanceImplementation;
import io.github.trimax.venta.engine.model.instance.implementation.LightInstanceImplementation;
import io.github.trimax.venta.engine.model.instance.implementation.SceneInstanceImplementation;
import io.github.trimax.venta.engine.model.instance.implementation.SoundSourceInstanceImplementation;
import io.github.trimax.venta.engine.model.prefabs.ScenePrefab;
import io.github.trimax.venta.engine.model.prefabs.implementation.ScenePrefabImplementation;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import one.util.streamex.StreamEx;

@Slf4j
@Component
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class SceneManagerImplementation
        extends AbstractManagerImplementation<SceneInstanceImplementation, SceneInstance>
        implements SceneManager {
    private final SoundSourceManagerImplementation soundSourceManager;
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

    @Override
    public SceneInstanceImplementation create(@NonNull final String name) {
        log.info("Creating scene {}", name);

        return store(abettor.createScene(name));
    }

    private SceneInstanceImplementation create(@NonNull final String name, @NonNull final ScenePrefabImplementation prefab) {
        final var scene = create(name);

        StreamEx.of(prefab.getObjects()).map(this::createObject).forEach(scene::add);
        StreamEx.of(prefab.getLights()).map(this::createLight).forEach(scene::add);
        StreamEx.of(prefab.getEmitters()).map(this::createEmitter).forEach(scene::add);
        StreamEx.of(prefab.getBillboards()).map(this::createBillboard).forEach(scene::add);
        StreamEx.of(prefab.getSoundSources()).map(this::createSoundSource).forEach(scene::add);

        Optional.ofNullable(prefab.getDirectionalLight()).ifPresent(scene::setDirectionalLight);
        Optional.ofNullable(prefab.getAmbientLight()).ifPresent(scene::setAmbientLight);
        Optional.ofNullable(prefab.getSkybox()).ifPresent(scene::setSkybox);
        Optional.ofNullable(prefab.getFog()).ifPresent(scene::setFog);

        return store(scene);
    }

    private SoundSourceInstanceImplementation createSoundSource(@NonNull final SceneSoundSource sceneSoundSource) {
        final var soundSource = soundSourceManager.create(sceneSoundSource.getName(), sceneSoundSource.getPrefab());
        Optional.of(sceneSoundSource).map(SceneSoundSource::getPosition).ifPresent(soundSource::setPosition);

        return soundSource;
    }

    private BillboardInstance createBillboard(@NonNull final SceneBillboard sceneBillboard) {
        final var billboard = billboardManager.create(sceneBillboard.getName(), sceneBillboard.getPrefab());
        Optional.of(sceneBillboard).map(SceneBillboard::getPosition).ifPresent(billboard::setPosition);
        Optional.of(sceneBillboard).map(SceneBillboard::getSize).ifPresent(billboard::setSize);

        return billboard;
    }

    private EmitterInstanceImplementation createEmitter(@NonNull final SceneEmitter sceneEmitter) {
        final var emitter = emitterManager.create(sceneEmitter.getName(), sceneEmitter.getPrefab());
        Optional.of(sceneEmitter).map(SceneEmitter::getPosition).ifPresent(emitter::setPosition);

        return emitter;
    }

    private ObjectInstance createObject(@NonNull final SceneObject sceneObject) {
        final var object = objectManager.create(sceneObject.getName(), sceneObject.getPrefab());
        Optional.of(sceneObject).map(SceneObject::getPosition).ifPresent(object::setPosition);
        Optional.of(sceneObject).map(SceneObject::getAngles).ifPresent(object::setRotation);
        Optional.of(sceneObject).map(SceneObject::getScale).ifPresent(object::setScale);

        return object;
    }

    private LightInstanceImplementation createLight(@NonNull final SceneLight sceneLight) {
        final var light = lightManager.create(sceneLight.getName(), sceneLight.getPrefab());
        Optional.of(sceneLight).map(SceneLight::getPosition).ifPresent(light::setPosition);

        return light;
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
