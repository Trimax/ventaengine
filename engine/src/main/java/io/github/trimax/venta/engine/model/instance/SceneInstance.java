package io.github.trimax.venta.engine.model.instance;

import java.util.List;

import org.joml.Vector3fc;

import io.github.trimax.venta.engine.model.common.scene.Fog;
import io.github.trimax.venta.engine.model.entity.CubemapEntity;
import lombok.NonNull;

public interface SceneInstance extends AbstractInstance {
    Vector3fc getAmbientLight();

    CubemapEntity getSkybox();

    void setAmbientLight(@NonNull final Vector3fc ambientLight);

    void setSkybox(final CubemapEntity skybox);

    void add(@NonNull final ObjectInstance object);

    void add(@NonNull final LightInstance light);

    void add(@NonNull final EmitterInstance emitter);

    void add(@NonNull final GridMeshInstance gridMesh);

    List<? extends EmitterInstance> getEmitters();

    List<? extends ObjectInstance> getObjects();

    List<? extends LightInstance> getLights();

    List<? extends GridMeshInstance> getGridMeshes();

    void setFog(final Fog fog);

    Fog getFog();
}
