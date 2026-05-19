package io.github.trimax.venta.engine.model.prefabs.implementation;

import java.util.List;
import java.util.Map;

import io.github.trimax.venta.engine.enums.TextureType;
import io.github.trimax.venta.engine.model.entity.implementation.GridMeshEntityImplementation;
import io.github.trimax.venta.engine.model.entity.implementation.MaterialEntityImplementation;
import io.github.trimax.venta.engine.model.entity.implementation.ProgramEntityImplementation;
import io.github.trimax.venta.engine.model.entity.implementation.TextureArrayEntityImplementation;
import io.github.trimax.venta.engine.model.entity.implementation.TextureEntityImplementation;
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
    ProgramEntityImplementation program;

    @NonNull
    TextureEntityImplementation heightmap;

    @NonNull
    List<MaterialEntityImplementation> materials;

    @NonNull
    Map<TextureType, TextureArrayEntityImplementation> textureArrays;

    float[] elevations;

    float factor;

    float blendWidth;
}
