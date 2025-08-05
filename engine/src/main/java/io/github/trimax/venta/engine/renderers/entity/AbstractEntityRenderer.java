package io.github.trimax.venta.engine.renderers.entity;

import io.github.trimax.venta.engine.model.instance.implementation.AbstractInstanceImplementation;
import io.github.trimax.venta.engine.renderers.AbstractRenderer;

public abstract class AbstractEntityRenderer<E extends AbstractInstanceImplementation, C extends AbstractRenderer.AbstractRenderContext<P>,
        P extends AbstractRenderer.AbstractRenderContext<?>> extends AbstractRenderer<E, C, P> {

}
