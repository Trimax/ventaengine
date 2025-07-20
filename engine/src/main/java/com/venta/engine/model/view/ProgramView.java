package com.venta.engine.model.view;

import com.venta.engine.managers.ProgramManager;
import com.venta.engine.renderers.AbstractRenderer;

public final class ProgramView extends AbstractRenderer.AbstractView<ProgramManager.ProgramEntity> {
    public ProgramView(final ProgramManager.ProgramEntity entity) {
        super(entity);
    }
}
