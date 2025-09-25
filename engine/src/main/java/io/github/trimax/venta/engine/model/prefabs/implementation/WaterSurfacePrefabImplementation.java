package io.github.trimax.venta.engine.model.prefabs.implementation;

import io.github.trimax.venta.engine.model.common.shared.Wave;
import io.github.trimax.venta.engine.model.entity.implementation.MaterialEntityImplementation;
import io.github.trimax.venta.engine.model.entity.implementation.ProgramEntityImplementation;
import io.github.trimax.venta.engine.model.prefabs.WaterSurfacePrefab;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.Value;

import java.util.List;

@Value
@AllArgsConstructor(access = AccessLevel.PACKAGE)
public class WaterSurfacePrefabImplementation extends AbstractPrefabImplementation implements WaterSurfacePrefab {
    @NonNull
    GridMeshPrefabImplementation gridMesh; //TODO: Should be entity

    @NonNull
    ProgramEntityImplementation program;

    @NonNull
    MaterialEntityImplementation material;

    List<Wave> waves;
}
