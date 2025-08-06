package io.github.trimax.venta.engine.model.common.hierarchy;

import io.github.trimax.venta.engine.model.entity.MaterialEntity;
import io.github.trimax.venta.engine.model.entity.MeshEntity;
import io.github.trimax.venta.engine.model.common.math.Transform;

public record MeshReference(MeshEntity mesh, MaterialEntity material, Transform transform) {
}
