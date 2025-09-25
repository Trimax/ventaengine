package io.github.trimax.venta.engine.model.entity.implementation;

import io.github.trimax.venta.engine.model.common.geo.Geometry;
import io.github.trimax.venta.engine.model.entity.GridMeshEntity;
import io.github.trimax.venta.engine.model.prefabs.implementation.AbstractPrefabImplementation;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.Value;

@Value
@AllArgsConstructor(access = AccessLevel.PACKAGE)
public class GridMeshEntityImplementation extends AbstractPrefabImplementation implements GridMeshEntity {
    @NonNull
    Geometry geometry;
}
