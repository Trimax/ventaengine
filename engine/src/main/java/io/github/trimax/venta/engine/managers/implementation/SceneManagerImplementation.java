package io.github.trimax.venta.engine.managers.implementation;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.engine.managers.SceneManager;
import io.github.trimax.venta.engine.model.dto.SceneDTO;
import io.github.trimax.venta.engine.model.dto.SceneLightDTO;
import io.github.trimax.venta.engine.model.dto.SceneObjectDTO;
import io.github.trimax.venta.engine.model.entity.SceneInstance;
import io.github.trimax.venta.engine.model.view.SceneView;
import io.github.trimax.venta.engine.utils.ResourceUtil;
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
        extends AbstractManagerImplementation<SceneInstance, SceneView>
        implements SceneManager {
    private final ObjectManagerImplementation objectManager;
    private final LightManagerImplementation lightManager;

    @Getter(onMethod_ = @__(@Override))
    private SceneInstance current;

    @Override
    public SceneInstance load(@NonNull final String name) {
        log.info("Loading scene {}", name);

        final var sceneDTO = ResourceUtil.loadAsObject(String.format("/scenes/%s.json", name), SceneDTO.class);
        final var scene = new SceneInstance(name);

        if (sceneDTO.hasObjects())
            for (final var sceneObject : sceneDTO.objects()) {
                final var object = objectManager.load(sceneObject.name());
                Optional.of(sceneObject).map(SceneObjectDTO::position).ifPresent(object::setPosition);
                Optional.of(sceneObject).map(SceneObjectDTO::angles).ifPresent(object::setRotation);
                Optional.of(sceneObject).map(SceneObjectDTO::scale).ifPresent(object::setScale);

                scene.add(object);
            }

        if (sceneDTO.hasLights())
            for (final var sceneLight : sceneDTO.lights()) {
                final var object = lightManager.load(sceneLight.name());
                Optional.of(sceneLight).map(SceneLightDTO::position).ifPresent(object::setPosition);
                Optional.of(sceneLight).map(SceneLightDTO::direction).ifPresent(object::setDirection);

                scene.add(object);
            }

        Optional.ofNullable(sceneDTO.ambientLight()).ifPresent(scene::setAmbientLight);

        return store(scene);
    }

    @Override
    public SceneInstance create(@NonNull final String name) {
        log.info("Creating scene {}", name);

        return store(new SceneInstance(name));
    }

    @Override
    public void setCurrent(@NonNull final SceneView scene) {
        if (scene instanceof SceneInstance entity)
            this.current = entity;
    }

    @Override
    public void delete(@NonNull final SceneView scene) {
        if (scene == getCurrent()) {
            log.error("Scene {} can't be deleted because it is currently selected", scene.getID());
            return;
        }

        if (scene instanceof SceneInstance entity)
            delete(entity);
    }

    @Override
    protected void destroy(final SceneInstance scene) {
        log.info("Destroying scene {} ({})", scene.getID(), scene.getName());
    }

    @Override
    protected boolean shouldCache() {
        return false;
    }
}
