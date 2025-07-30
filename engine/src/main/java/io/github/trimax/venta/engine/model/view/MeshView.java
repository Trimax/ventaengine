package io.github.trimax.venta.engine.model.view;

public interface MeshView extends AbstractView {
    MaterialView getMaterial();

    void setMaterial(final MaterialView material);
}
