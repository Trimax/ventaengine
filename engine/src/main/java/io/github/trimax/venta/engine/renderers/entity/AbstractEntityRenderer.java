package io.github.trimax.venta.engine.renderers.entity;

import io.github.trimax.venta.engine.model.entity.AbstractEntity;
import io.github.trimax.venta.engine.renderers.AbstractRenderer;

public abstract class AbstractEntityRenderer<E extends AbstractEntity, C extends AbstractRenderer.AbstractRenderContext<P>,
        P extends AbstractRenderer.AbstractRenderContext<?>> extends AbstractRenderer<E, C, P> {

}
