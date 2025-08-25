package io.github.trimax.venta.engine.model.instance;

import io.github.trimax.venta.engine.model.common.scene.Fog;
import org.joml.Vector4f;
import org.joml.Vector4fc;

import java.util.List;

public interface SceneInstance extends AbstractInstance {
    Vector4fc getAmbientLight();

    void setAmbientLight(final Vector4f ambientLight);

    void add(final ObjectInstance object);

    void add(final LightInstance light);

    List<? extends ObjectInstance> getObjects();

    List<? extends LightInstance> getLights();

    void setFog(final Fog fog);

    Fog getFog();
}
