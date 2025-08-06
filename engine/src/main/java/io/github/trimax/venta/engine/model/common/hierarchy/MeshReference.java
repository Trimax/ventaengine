package io.github.trimax.venta.engine.model.common.hierarchy;

import io.github.trimax.venta.engine.model.common.math.Transform;
import io.github.trimax.venta.engine.model.entity.implementation.MaterialEntityImplementation;
import io.github.trimax.venta.engine.model.entity.implementation.MeshEntityImplementation;

public record MeshReference(MeshEntityImplementation mesh,
                            MaterialEntityImplementation material,
                            Transform transform) {
    public boolean hasMesh() {
        return mesh != null;
    }

    public boolean hasTransform() {
        return transform != null;
    }
}
