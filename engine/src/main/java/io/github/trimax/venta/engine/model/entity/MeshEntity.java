package io.github.trimax.venta.engine.model.entity;

public interface MeshEntity extends AbstractEntity {
    MaterialEntity getMaterial();

    //TODO: mOVE to node
    void setMaterial(final MaterialEntity material);

    boolean hasMaterial();
}
