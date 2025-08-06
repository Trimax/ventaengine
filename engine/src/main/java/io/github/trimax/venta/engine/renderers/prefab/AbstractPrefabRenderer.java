package io.github.trimax.venta.engine.renderers.prefab;

import io.github.trimax.venta.engine.model.prefabs.implementation.AbstractPrefabImplementation;
import io.github.trimax.venta.engine.renderers.AbstractRenderer;

public abstract class AbstractPrefabRenderer<E extends AbstractPrefabImplementation, C extends AbstractRenderer.AbstractRenderContext<P>,
        P extends AbstractRenderer.AbstractRenderContext<?>> extends AbstractRenderer<E, C, P> {
}
