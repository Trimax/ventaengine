package io.github.trimax.venta.engine.model.prefabs.implementation;

import io.github.trimax.venta.engine.model.prefabs.AbstractPrefab;
import io.github.trimax.venta.engine.utils.IdentifierUtil;

public abstract class AbstractPrefabImplementation implements AbstractPrefab {
    private final String id = IdentifierUtil.generate(5);

    @Override
    public final String getID() {
        return id;
    }
}
