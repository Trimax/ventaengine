package io.github.trimax.venta.engine.model.prefabs.implementation;

import java.util.List;

import io.github.trimax.venta.engine.model.common.geo.Geometry;
import io.github.trimax.venta.engine.model.common.shared.Wave;
import io.github.trimax.venta.engine.model.entity.implementation.MaterialEntityImplementation;
import io.github.trimax.venta.engine.model.entity.implementation.ProgramEntityImplementation;
import io.github.trimax.venta.engine.model.prefabs.GridMeshPrefab;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.Value;

@Value
@AllArgsConstructor(access = AccessLevel.PACKAGE)
public class GridMeshPrefabImplementation extends AbstractPrefabImplementation implements GridMeshPrefab {
    @NonNull
    ProgramEntityImplementation program;

    @NonNull
    MaterialEntityImplementation material;

    @NonNull
    Geometry geometry;

    List<Wave> waves;
}
