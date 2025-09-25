package io.github.trimax.venta.engine.model.prefabs.implementation;

import io.github.trimax.venta.engine.model.common.geo.Geometry;
import io.github.trimax.venta.engine.model.prefabs.GridMeshPrefab;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.Value;

@Value
@AllArgsConstructor(access = AccessLevel.PACKAGE)
public class GridMeshPrefabImplementation extends AbstractPrefabImplementation implements GridMeshPrefab {
    @NonNull
    Geometry geometry;
}
