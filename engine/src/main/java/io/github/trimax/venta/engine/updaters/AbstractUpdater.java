package io.github.trimax.venta.engine.updaters;

import io.github.trimax.venta.engine.model.instance.AbstractInstance;

public interface AbstractUpdater<T extends AbstractInstance> {
    void update(final T updatable, final float delta);
}
