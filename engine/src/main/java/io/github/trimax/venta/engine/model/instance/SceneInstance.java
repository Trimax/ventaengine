package io.github.trimax.venta.engine.model.instance;

import java.util.Collection;

import org.joml.Vector3fc;

import io.github.trimax.venta.engine.model.common.effects.Fog;
import io.github.trimax.venta.engine.model.entity.CubemapEntity;
import lombok.NonNull;

public interface SceneInstance extends AbstractInstance {
    Collection<? extends SoundSourceInstance> getSoundSources();

    Collection<? extends GridMeshInstance> getGridMeshes();

    Collection<? extends BillboardInstance> getBillboards();

    Collection<? extends EmitterInstance> getEmitters();

    Collection<? extends ObjectInstance> getObjects();

    Collection<? extends LightInstance> getLights();

    Vector3fc getAmbientLight();

    CubemapEntity getSkybox();

    Fog getFog();

    void setAmbientLight(@NonNull final Vector3fc ambientLight);

    void setSkybox(final CubemapEntity skybox);

    void setFog(final Fog fog);

    void add(@NonNull final ObjectInstance object);

    void add(@NonNull final LightInstance light);

    void add(@NonNull final EmitterInstance emitter);

    void add(@NonNull final GridMeshInstance gridMesh);

    void add(@NonNull final SoundSourceInstance sound);

    void add(@NonNull final BillboardInstance billboard);
}
