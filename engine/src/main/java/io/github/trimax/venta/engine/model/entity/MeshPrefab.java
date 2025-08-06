package io.github.trimax.venta.engine.model.entity;

import io.github.trimax.venta.engine.model.prefabs.AbstractPrefab;

public interface MeshPrefab extends AbstractPrefab {
    MaterialEntity getMaterial();

    //TODO: mOVE to node
    void setMaterial(final MaterialEntity material);

    boolean hasMaterial();
}
