package com.venta.engine.model.view;

import com.venta.engine.managers.ResourceManager;
import com.venta.engine.renderers.AbstractRenderer;

public final class ResourceView extends AbstractRenderer.AbstractView<ResourceManager.ResourceEntity> {
    public ResourceView(final String id, final ResourceManager.ResourceEntity entity) {
        super(id, entity);
    }
}
