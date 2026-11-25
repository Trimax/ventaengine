package io.github.trimax.venta.engine.model.prefabs.implementation;

import io.github.trimax.venta.engine.enums.TextureType;
import io.github.trimax.venta.engine.model.common.geo.Geometry;
import io.github.trimax.venta.engine.model.common.terrain.TerrainChunkLayout;
import io.github.trimax.venta.engine.model.common.terrain.TerrainSlope;
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

    /** The materials by the steepness, they are the last ones of the materials: after those of the heights */
    @NonNull
    List<TerrainSlope> slopes;

    float factor;

    /** The chunks of the terrain: where its grid is cut and the geometry of it, which the terrains of this prefab share */
    @NonNull
    TerrainChunkLayout chunkLayout;

    @NonNull
    Geometry chunkGeometry;

    /** The distance (in the world) the first level of detail changes at, 0 for a bit more than the size of a chunk */
    float lodDistance;
}
