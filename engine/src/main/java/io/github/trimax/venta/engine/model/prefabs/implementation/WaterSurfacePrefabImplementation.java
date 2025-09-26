package io.github.trimax.venta.engine.model.prefabs.implementation;

import java.util.List;

import io.github.trimax.venta.engine.model.common.shared.Noise;
import io.github.trimax.venta.engine.model.common.shared.Wave;
import io.github.trimax.venta.engine.model.common.water.WaterFoam;
import io.github.trimax.venta.engine.model.common.water.WaterMaterial;
import io.github.trimax.venta.engine.model.entity.implementation.GridMeshEntityImplementation;
import io.github.trimax.venta.engine.model.entity.implementation.ProgramEntityImplementation;
import io.github.trimax.venta.engine.model.prefabs.WaterSurfacePrefab;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.Value;

@Value
@AllArgsConstructor(access = AccessLevel.PACKAGE)
public class WaterSurfacePrefabImplementation extends AbstractPrefabImplementation implements WaterSurfacePrefab {
    @NonNull
    GridMeshEntityImplementation gridMesh;

    @NonNull
    ProgramEntityImplementation program;

    @NonNull
    WaterMaterial material;

    @NonNull
    WaterFoam foam;

    List<Noise> noises;

    List<Wave> waves;
}
