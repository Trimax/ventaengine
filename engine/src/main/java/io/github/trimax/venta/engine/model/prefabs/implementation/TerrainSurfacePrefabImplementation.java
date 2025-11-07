package io.github.trimax.venta.engine.model.prefabs.implementation;

import io.github.trimax.venta.engine.enums.TextureType;
import io.github.trimax.venta.engine.model.entity.implementation.*;
import io.github.trimax.venta.engine.model.prefabs.TerrainSurfacePrefab;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.Value;

import java.util.List;
import java.util.Map;

@Value
@AllArgsConstructor(access = AccessLevel.PACKAGE)
public class TerrainSurfacePrefabImplementation extends AbstractPrefabImplementation implements TerrainSurfacePrefab {
    @NonNull
    GridMeshEntityImplementation gridMesh;

    @NonNull
    ProgramEntityImplementation program;

    @NonNull
    TextureEntityImplementation heightmap;

    @NonNull
    List<MaterialEntityImplementation> materials;

    @NonNull
    Map<TextureType, TextureArrayEntityImplementation> textureArrays;

    float[] elevations;

    float factor;
}
