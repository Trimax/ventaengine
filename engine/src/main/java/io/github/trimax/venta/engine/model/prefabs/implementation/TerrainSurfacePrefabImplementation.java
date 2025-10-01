package io.github.trimax.venta.engine.model.prefabs.implementation;

import io.github.trimax.venta.engine.model.entity.implementation.GridMeshEntityImplementation;
import io.github.trimax.venta.engine.model.entity.implementation.MaterialEntityImplementation;
import io.github.trimax.venta.engine.model.entity.implementation.ProgramEntityImplementation;
import io.github.trimax.venta.engine.model.prefabs.TerrainSurfacePrefab;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.Value;

@Value
@AllArgsConstructor(access = AccessLevel.PACKAGE)
public class TerrainSurfacePrefabImplementation extends AbstractPrefabImplementation implements TerrainSurfacePrefab {
    @NonNull
    GridMeshEntityImplementation gridMesh;

    @NonNull
    MaterialEntityImplementation material;

    @NonNull
    ProgramEntityImplementation program;
}
