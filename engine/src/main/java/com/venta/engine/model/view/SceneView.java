package com.venta.engine.model.view;

import java.util.ArrayList;
import java.util.List;

import org.joml.Vector4f;

import com.venta.engine.definitions.Definitions;
import com.venta.engine.managers.SceneManager;
import com.venta.engine.renderers.AbstractRenderer;
import lombok.Getter;

@Getter
public final class SceneView extends AbstractRenderer.AbstractView<SceneManager.SceneEntity> {
    private final Vector4f ambientLight = new Vector4f(0.3f, 0.3f, 0.3f, 1.0f);
    private final List<ObjectView> objects = new ArrayList<>();
    private final List<LightView> lights = new ArrayList<>();

    public SceneView(final SceneManager.SceneEntity entity) {
        super(entity);
    }

    public void setAmbientLight(final Vector4f ambientLight) {
        this.ambientLight.set(ambientLight);
    }

    public void add(final ObjectView object) {
        if (this.objects.size() >= Definitions.LIGHT_MAX)
            throw new RuntimeException("Too many lights");

        this.objects.add(object);
    }

    public void add(final LightView light) {
        this.lights.add(light);
    }
}
