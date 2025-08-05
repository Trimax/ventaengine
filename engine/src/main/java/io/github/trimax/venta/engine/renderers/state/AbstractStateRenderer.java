package io.github.trimax.venta.engine.renderers.state;

import io.github.trimax.venta.engine.model.states.AbstractState;
import io.github.trimax.venta.engine.renderers.AbstractRenderer;

public abstract class AbstractStateRenderer<S extends AbstractState, C extends AbstractRenderer.AbstractRenderContext<P>,
        P extends AbstractRenderer.AbstractRenderContext<?>> extends AbstractRenderer<S, C, P> {
}
