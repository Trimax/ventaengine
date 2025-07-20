package com.venta.engine.model.view;

import com.venta.engine.managers.SceneManager;
import com.venta.engine.renderers.AbstractRenderer;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public final class SceneView extends AbstractRenderer.AbstractView<SceneManager.SceneEntity> {
    private final List<ObjectView> objects = new ArrayList<>();

    public SceneView(final SceneManager.SceneEntity entity) {
        super(entity);
    }

    public void add(final ObjectView object) {
        this.objects.add(object);
    }
}
