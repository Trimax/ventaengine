package io.github.trimax.venta.engine.model.instance;

import io.github.trimax.venta.engine.model.entity.MeshEntity;
import org.joml.Vector3fc;

public interface GizmoInstance extends AbstractInstance {
    MeshEntity getMesh();

    Vector3fc getPosition();

    Vector3fc getRotation();

    Vector3fc getScale();
}
