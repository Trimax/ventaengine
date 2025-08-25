package io.github.trimax.venta.engine.model.instance;

import java.util.List;

import org.joml.Vector3fc;

import io.github.trimax.venta.engine.model.entity.CubemapEntity;

public interface SceneInstance extends AbstractInstance {
    Vector3fc getAmbientLight();

    void setAmbientLight(final Vector3fc ambientLight);

    void setSkybox(final CubemapEntity skybox);

    void add(final ObjectInstance object);

    void add(final LightInstance light);

    List<? extends ObjectInstance> getObjects();

    List<? extends LightInstance> getLights();

    void setFog(final Fog fog);

    Fog getFog();
}
