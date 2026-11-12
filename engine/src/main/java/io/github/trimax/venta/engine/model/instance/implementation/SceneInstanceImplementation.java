package io.github.trimax.venta.engine.model.instance.implementation;

import io.github.trimax.venta.engine.definitions.DefinitionsCommon;
import io.github.trimax.venta.engine.model.common.shared.DirectionalLight;
import io.github.trimax.venta.engine.model.common.shared.Fog;
import io.github.trimax.venta.engine.model.entity.CubemapEntity;
import io.github.trimax.venta.engine.model.entity.implementation.CubemapEntityImplementation;
import io.github.trimax.venta.engine.model.instance.*;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import one.util.streamex.StreamEx;
import org.joml.Vector3f;
import org.joml.Vector3fc;

import java.util.HashSet;
import java.util.Set;

@Slf4j
@Getter
public final class SceneInstanceImplementation extends AbstractInstanceImplementation implements SceneInstance {
    private final Vector3f ambientLight = new Vector3f(0.3f);
    private final Set<TerrainSurfaceInstanceImplementation> terrainSurfaces = new HashSet<>();
    private final Set<WaterSurfaceInstanceImplementation> waterSurfaces = new HashSet<>();
    private final Set<SoundSourceInstanceImplementation> soundSources = new HashSet<>();
    private final Set<BillboardInstanceImplementation> billboards = new HashSet<>();
    private final Set<EmitterInstanceImplementation> emitters = new HashSet<>();
    private final Set<ObjectInstanceImplementation> objects = new HashSet<>();
    private final Set<LightInstanceImplementation> lights = new HashSet<>();

    private CubemapEntityImplementation skybox;

    @Setter
    private Fog fog;

    @Setter
    private DirectionalLight directionalLight;

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
    public void add(@NonNull final LightInstance light) {
        if (this.lights.size() >= DefinitionsCommon.MAX_LIGHTS) {
            log.warn("There are maximum amount of lights ({}) in the scene {}", DefinitionsCommon.MAX_LIGHTS, this.lights.size());
            return;
        }

        if (light instanceof LightInstanceImplementation instance)
            attach(lights, instance);
    }

    @Override
    public void add(@NonNull final ObjectInstance object) {
        if (object instanceof ObjectInstanceImplementation instance)
            attach(objects, instance);
    }

    @Override
    public void add(@NonNull final EmitterInstance emitter) {
        if (emitter instanceof EmitterInstanceImplementation instance)
            attach(emitters, instance);
    }

    @Override
    public void add(@NonNull final WaterSurfaceInstance surface) {
        if (surface instanceof WaterSurfaceInstanceImplementation instance)
            attach(waterSurfaces, instance);
    }

    @Override
    public void add(@NonNull final TerrainSurfaceInstance surface) {
        if (surface instanceof TerrainSurfaceInstanceImplementation instance)
            attach(terrainSurfaces, instance);
    }

    @Override
    public void add(@NonNull final BillboardInstance billboard) {
        if (billboard instanceof BillboardInstanceImplementation instance)
            attach(billboards, instance);
    }

    @Override
    public void add(@NonNull final SoundSourceInstance soundSource) {
        if (soundSource instanceof SoundSourceInstanceImplementation instance)
            attach(soundSources, instance);
    }

    @Override
    public void remove(@NonNull final AbstractInstance instance) {
        if (!(instance instanceof AbstractInstanceImplementation object) || object.getParent() != this)
            return;

        detach(object);

        if (instance instanceof SoundSourceInstanceImplementation soundSource)
            soundSource.stop();
    }

    /** Checks whether the instance has been added to this scene */
    public boolean contains(@NonNull final AbstractInstance instance) {
        return instance instanceof AbstractInstanceImplementation object && object.getParent() == this;
    }

    /** Detaches all the instances, so they can be added to other scenes; used when the scene is deleted */
    public void detachAll() {
        StreamEx.of(terrainSurfaces, waterSurfaces, soundSources, billboards, emitters, objects, lights)
                .flatMap(Set::stream)
                .forEach(AbstractInstanceImplementation::removeParent);

        StreamEx.of(terrainSurfaces, waterSurfaces, soundSources, billboards, emitters, objects, lights)
                .forEach(Set::clear);
    }

    /** Adds the instance; setParent rejects an instance that is already in another scene */
    private <T extends AbstractInstanceImplementation> void attach(final Set<T> instances, final T instance) {
        // The parent is set first: if the instance is in another scene, this scene stays unchanged
        instance.setParent(this);
        instances.add(instance);
    }

    private void detach(final AbstractInstanceImplementation instance) {
        StreamEx.of(terrainSurfaces, waterSurfaces, soundSources, billboards, emitters, objects, lights)
                .forEach(instances -> instances.remove(instance));
        instance.removeParent();
    }
}
