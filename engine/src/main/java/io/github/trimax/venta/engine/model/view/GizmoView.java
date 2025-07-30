package io.github.trimax.venta.engine.model.view;

import org.joml.Vector3f;

public interface GizmoView extends AbstractView {
    MeshView getMesh();

    Vector3f getPosition();

    Vector3f getRotation();

    Vector3f getScale();
}
