package com.venta.engine.model.view;

import java.util.List;

import org.joml.Vector4f;

public interface SceneView extends AbstractView {
    Vector4f getAmbientLight();

    void setAmbientLight(final Vector4f ambientLight);

    void add(final ObjectView object);

    void add(final LightView light);

    List<? extends ObjectView> getObjects();

    List<? extends LightView> getLights();
}
