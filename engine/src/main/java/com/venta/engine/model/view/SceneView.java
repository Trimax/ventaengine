package com.venta.engine.model.view;

import org.joml.Vector4f;

import java.util.List;

public interface SceneView extends AbstractView {
    Vector4f getAmbientLight();

    void setAmbientLight(final Vector4f ambientLight);

    void add(final ObjectView object);

    void add(final MeshView object);

    void add(final LightView light);

    List<? extends ObjectView> getObjects();

    List<? extends MeshView> getMeshes();

    List<? extends LightView> getLights();
}
