package io.github.trimax.venta.engine.model.instance;

public interface MeshInstance extends AbstractInstance {
    MaterialInstance getMaterial();

    void setMaterial(final MaterialInstance material);

    public boolean hasMaterial();
}
