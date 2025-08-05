package io.github.trimax.venta.engine.model.entity.implementation;

import io.github.trimax.venta.engine.model.entity.AbstractEntity;
import io.github.trimax.venta.engine.utils.IdentifierUtil;

public abstract class AbstractEntityImplementation implements AbstractEntity {
    private final String id = IdentifierUtil.generate(20);

    public final String getID() {
        return id;
    }
}
