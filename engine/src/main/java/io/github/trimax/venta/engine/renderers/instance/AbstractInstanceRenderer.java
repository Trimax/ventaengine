package io.github.trimax.venta.engine.renderers.instance;

import io.github.trimax.venta.engine.model.instance.implementation.AbstractInstanceImplementation;
import io.github.trimax.venta.engine.renderers.AbstractRenderer;

public abstract class AbstractInstanceRenderer<E extends AbstractInstanceImplementation, C extends AbstractRenderer.AbstractRenderContext<P>,
        P extends AbstractRenderer.AbstractRenderContext<?>> extends AbstractRenderer<E, C, P> {

}
