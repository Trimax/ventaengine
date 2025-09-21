package io.github.trimax.venta.engine.model.instance.implementation;

import io.github.trimax.venta.engine.definitions.Definitions;
import io.github.trimax.venta.engine.model.common.scene.Fog;
import io.github.trimax.venta.engine.model.entity.CubemapEntity;
import io.github.trimax.venta.engine.model.entity.implementation.CubemapEntityImplementation;
import io.github.trimax.venta.engine.model.instance.*;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.joml.Vector3f;
import org.joml.Vector3fc;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Getter
public final class SceneInstanceImplementation extends AbstractInstanceImplementation implements SceneInstance {
    private final Vector3f ambientLight = new Vector3f(0.3f);
    private final List<ObjectInstanceImplementation> objects = new ArrayList<>();
    private final List<LightInstanceImplementation> lights = new ArrayList<>();
    private final List<EmitterInstanceImplementation> emitters = new ArrayList<>();
    private final List<GridMeshInstanceImplementation> gridMeshes = new ArrayList<>();
    private final List<BillboardInstanceImplementation> billboards = new ArrayList<>();
    private final List<SoundSourceInstanceImplementation> soundSources = new ArrayList<>();

    private CubemapEntityImplementation skybox;

    @Setter(onMethod_ = @__(@Override))
    private Fog fog;

    SceneInstanceImplementation(@NonNull final String name) {
        super(name);
    }

    @Override
    public CubemapEntityImplementation getSkybox() {
        return skybox;
    }

    @Override
    public void setAmbientLight(@NonNull final Vector3fc ambientLight) {
        this.ambientLight.set(ambientLight);
    }

    @Override
    public void setSkybox(final CubemapEntity skybox) {
        if (skybox instanceof CubemapEntityImplementation entity)
            this.skybox = entity;
    }

    @Override
    public void add(final ObjectInstance object) {
        if (object instanceof ObjectInstanceImplementation instance)
            objects.add(instance);
    }

    @Override
    public void add(final LightInstance light) {
        if (this.lights.size() >= Definitions.LIGHT_MAX) {
            log.warn("There are maximum amount of lights ({}) in the scene {}", Definitions.LIGHT_MAX, this.lights.size());
            return;
        }

        if (light instanceof LightInstanceImplementation instance)
            lights.add(instance);
    }

    @Override
    public void add(@NonNull final EmitterInstance emitter) {
        if (emitter instanceof EmitterInstanceImplementation instance)
            emitters.add(instance);
    }

    @Override
    public void add(@NonNull final GridMeshInstance gridMesh) {
        if (gridMesh instanceof GridMeshInstanceImplementation instance)
            gridMeshes.add(instance);
    }

    @Override
    public void add(@NonNull final SoundSourceInstance soundSource) {
        if (soundSource instanceof SoundSourceInstanceImplementation instance)
            soundSources.add(instance);
    }

    @Override
    public void add(@NonNull final BillboardInstance billboard) {
        if (billboard instanceof BillboardInstanceImplementation instance)
            billboards.add(instance);
    }
}
