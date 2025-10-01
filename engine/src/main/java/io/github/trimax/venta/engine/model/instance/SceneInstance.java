package io.github.trimax.venta.engine.model.instance;

import io.github.trimax.venta.engine.model.common.shared.DirectionalLight;
import io.github.trimax.venta.engine.model.common.shared.Fog;
import io.github.trimax.venta.engine.model.entity.CubemapEntity;
import lombok.NonNull;
import org.joml.Vector3fc;

import java.util.Collection;

public interface SceneInstance extends AbstractInstance {
    Collection<? extends TerrainSurfaceInstance> getTerrainSurfaces();

    Collection<? extends WaterSurfaceInstance> getWaterSurfaces();

    Collection<? extends SoundSourceInstance> getSoundSources();

    Collection<? extends BillboardInstance> getBillboards();

    Collection<? extends EmitterInstance> getEmitters();

    Collection<? extends ObjectInstance> getObjects();

    Collection<? extends LightInstance> getLights();

    DirectionalLight getDirectionalLight();

    Vector3fc getAmbientLight();

    CubemapEntity getSkybox();

    Fog getFog();

    void setDirectionalLight(@NonNull final DirectionalLight directionalLight);

    void setAmbientLight(@NonNull final Vector3fc ambientLight);

    void setSkybox(final CubemapEntity skybox);

    void setFog(final Fog fog);

    void add(@NonNull final LightInstance light);

    void add(@NonNull final ObjectInstance object);

    void add(@NonNull final EmitterInstance emitter);

    void add(@NonNull final SoundSourceInstance sound);

    void add(@NonNull final BillboardInstance billboard);

    void add(@NonNull final WaterSurfaceInstance surface);

    void add(@NonNull final TerrainSurfaceInstance surface);
}
