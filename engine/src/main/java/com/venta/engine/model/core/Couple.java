package com.venta.engine.model.core;

import com.venta.engine.managers.AbstractManager;
import com.venta.engine.renderers.AbstractRenderer;

public record Couple<E extends AbstractManager.AbstractEntity, V extends AbstractRenderer.AbstractView<E>>(E entity, V view) {
}
