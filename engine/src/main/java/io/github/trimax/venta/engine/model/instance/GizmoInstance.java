package io.github.trimax.venta.engine.model.instance;

import io.github.trimax.venta.engine.model.entity.MeshPrefab;
import org.joml.Vector3f;

public interface GizmoInstance extends AbstractInstance {
    MeshPrefab getMesh();

    Vector3f getPosition();

    Vector3f getRotation();

    Vector3f getScale();
}
