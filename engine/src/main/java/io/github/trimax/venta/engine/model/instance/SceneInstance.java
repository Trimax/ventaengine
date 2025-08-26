package io.github.trimax.venta.engine.model.instance;

import io.github.trimax.venta.engine.model.entity.CubemapEntity;
import org.joml.Vector4f;

import java.util.List;

public interface SceneInstance extends AbstractInstance {
    Vector4f getAmbientLight();

    void setAmbientLight(final Vector4f ambientLight);

    void setCubemap(final CubemapEntity cubemap);

    void add(final ObjectInstance object);

    void add(final LightInstance light);

    List<? extends ObjectInstance> getObjects();

    List<? extends LightInstance> getLights();
}
