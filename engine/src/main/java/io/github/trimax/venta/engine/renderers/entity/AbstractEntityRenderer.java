package io.github.trimax.venta.engine.renderers.entity;

import io.github.trimax.venta.engine.model.entity.AbstractInstance;
import io.github.trimax.venta.engine.renderers.AbstractRenderer;

public abstract class AbstractEntityRenderer<E extends AbstractInstance, C extends AbstractRenderer.AbstractRenderContext<P>,
        P extends AbstractRenderer.AbstractRenderContext<?>> extends AbstractRenderer<E, C, P> {

}
