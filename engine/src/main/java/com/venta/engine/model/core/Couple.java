package com.venta.engine.model.core;

import com.venta.engine.managers.AbstractManager;
import com.venta.engine.model.view.AbstractView;

public record Couple<E extends AbstractManager.AbstractEntity, V extends AbstractView>(E entity, V view) {
}
