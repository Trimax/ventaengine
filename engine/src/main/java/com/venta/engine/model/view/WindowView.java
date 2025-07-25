package com.venta.engine.model.view;

import com.venta.engine.managers.WindowManager;
import com.venta.engine.renderers.AbstractRenderer;
import lombok.Getter;

@Getter
public final class WindowView extends AbstractRenderer.AbstractView<WindowManager.WindowEntity> {
    public WindowView(final WindowManager.WindowEntity entity) {
        super(entity);
    }
}
