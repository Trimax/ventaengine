package com.venta.engine.renderer;

import com.venta.engine.manager.AbstractManager;

public interface AbstractRenderer<V extends AbstractManager.AbstractEntity> {
    void render(final V entity);
}
