package io.github.trimax.venta.engine.model.instance;

import io.github.trimax.venta.engine.model.entity.MeshEntity;
import org.joml.Vector3f;

public interface GizmoInstance extends AbstractInstance {
    MeshEntity getMesh();

    Vector3f getPosition();

    Vector3f getRotation();

    Vector3f getScale();
}
