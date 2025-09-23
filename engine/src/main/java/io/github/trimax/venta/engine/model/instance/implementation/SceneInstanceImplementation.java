package io.github.trimax.venta.engine.model.instance.implementation;

import java.util.HashSet;
import java.util.Set;

import org.joml.Vector3f;
import org.joml.Vector3fc;

import io.github.trimax.venta.engine.definitions.DefinitionsCommon;
import io.github.trimax.venta.engine.model.common.scene.Fog;
import io.github.trimax.venta.engine.model.entity.CubemapEntity;
import io.github.trimax.venta.engine.model.entity.implementation.CubemapEntityImplementation;
import io.github.trimax.venta.engine.model.instance.BillboardInstance;
import io.github.trimax.venta.engine.model.instance.EmitterInstance;
import io.github.trimax.venta.engine.model.instance.GridMeshInstance;
import io.github.trimax.venta.engine.model.instance.LightInstance;
import io.github.trimax.venta.engine.model.instance.ObjectInstance;
import io.github.trimax.venta.engine.model.instance.SceneInstance;
import io.github.trimax.venta.engine.model.instance.SoundSourceInstance;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
public final class SceneInstanceImplementation extends AbstractInstanceImplementation implements SceneInstance {
    private final Vector3f ambientLight = new Vector3f(0.3f);
    private final Set<ObjectInstanceImplementation> objects = new HashSet<>();
    private final Set<LightInstanceImplementation> lights = new HashSet<>();
    private final Set<EmitterInstanceImplementation> emitters = new HashSet<>();
    private final Set<GridMeshInstanceImplementation> gridMeshes = new HashSet<>();
    private final Set<BillboardInstanceImplementation> billboards = new HashSet<>();
    private final Set<SoundSourceInstanceImplementation> soundSources = new HashSet<>();

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
    public void add(@NonNull final ObjectInstance object) {
        if (object instanceof ObjectInstanceImplementation instance)
            objects.add(instance);
    }

    @Override
    public void add(@NonNull final LightInstance light) {
        if (this.lights.size() >= DefinitionsCommon.LIGHT_MAX) {
            log.warn("There are maximum amount of lights ({}) in the scene {}", DefinitionsCommon.LIGHT_MAX, this.lights.size());
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
