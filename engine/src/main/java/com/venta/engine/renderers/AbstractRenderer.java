package com.venta.engine.renderers;

import com.venta.engine.managers.AbstractManager;

public interface AbstractRenderer<V extends AbstractManager.AbstractEntity> {
    void render(final V entity);
}
