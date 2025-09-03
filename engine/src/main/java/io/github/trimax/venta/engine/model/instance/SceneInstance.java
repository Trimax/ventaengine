package io.github.trimax.venta.engine.model.instance;

import io.github.trimax.venta.engine.model.common.scene.Fog;
import io.github.trimax.venta.engine.model.entity.CubemapEntity;
import lombok.NonNull;
import org.joml.Vector3fc;

import java.util.List;

public interface SceneInstance extends AbstractInstance {
    Vector3fc getAmbientLight();

    void setAmbientLight(@NonNull final Vector3fc ambientLight);

    void setSkybox(final CubemapEntity skybox);

    void add(@NonNull final ObjectInstance object);

    void add(@NonNull final LightInstance light);

    void add(@NonNull final EmitterInstance emitter);

    List<? extends EmitterInstance> getEmitters();

    List<? extends ObjectInstance> getObjects();

    List<? extends LightInstance> getLights();

    void setFog(final Fog fog);

    Fog getFog();
}
