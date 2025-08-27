package io.github.trimax.venta.engine.model.instance.implementation;

import java.util.ArrayList;
import java.util.List;

import org.joml.Vector3f;
import org.joml.Vector3fc;

import io.github.trimax.venta.engine.definitions.Definitions;
import io.github.trimax.venta.engine.model.entity.CubemapEntity;
import io.github.trimax.venta.engine.model.entity.implementation.CubemapEntityImplementation;
import io.github.trimax.venta.engine.model.instance.LightInstance;
import io.github.trimax.venta.engine.model.instance.ObjectInstance;
import io.github.trimax.venta.engine.model.instance.SceneInstance;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
public final class SceneInstanceImplementation extends AbstractInstanceImplementation implements SceneInstance {
    private final Vector3f ambientLight = new Vector3f(0.3f);
    private final List<ObjectInstanceImplementation> objects = new ArrayList<>();
    private final List<LightInstanceImplementation> lights = new ArrayList<>();

    private CubemapEntityImplementation skybox;

    SceneInstanceImplementation(@NonNull final String name) {
        super(name);
    }

    @Override
    public void setAmbientLight(final Vector3fc ambientLight) {
        this.ambientLight.set(ambientLight);
    }

    @Override
    public void setSkybox(final CubemapEntity skybox) {
        if (skybox instanceof CubemapEntityImplementation entity)
            this.skybox = entity;
    }

    @Override
    public void add(final ObjectInstance object) {
        if (object instanceof ObjectInstanceImplementation entity)
            objects.add(entity);
    }

    @Override
    public void add(final LightInstance light) {
        if (this.lights.size() >= Definitions.LIGHT_MAX) {
            log.warn("There are maximum amount of lights ({}) in the scene {}", Definitions.LIGHT_MAX, this.lights.size());
            return;
        }

        if (light instanceof LightInstanceImplementation entity)
            lights.add(entity);
    }
}
