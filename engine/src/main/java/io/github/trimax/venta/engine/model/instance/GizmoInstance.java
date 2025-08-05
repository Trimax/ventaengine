package io.github.trimax.venta.engine.model.instance;

import org.joml.Vector3f;

public interface GizmoInstance extends AbstractInstance {
    MeshInstance getMesh();

    Vector3f getPosition();

    Vector3f getRotation();

    Vector3f getScale();
}
