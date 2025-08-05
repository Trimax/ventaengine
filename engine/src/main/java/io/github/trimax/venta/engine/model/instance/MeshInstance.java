package io.github.trimax.venta.engine.model.instance;

import io.github.trimax.venta.engine.model.entity.MaterialEntity;

public interface MeshInstance extends AbstractInstance {
    MaterialEntity getMaterial();

    void setMaterial(final MaterialEntity material);

    public boolean hasMaterial();
}
