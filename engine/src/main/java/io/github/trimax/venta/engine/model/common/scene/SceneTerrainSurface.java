package io.github.trimax.venta.engine.model.common.scene;

import org.joml.Vector3f;

import io.github.trimax.venta.engine.model.dto.scene.SceneTerrainSurfaceDTO;
import io.github.trimax.venta.engine.model.prefabs.TerrainSurfacePrefab;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
public final class SceneTerrainSurface {
    @NonNull
    String name;

    @NonNull
    TerrainSurfacePrefab prefab;

    @NonNull
    Vector3f position;

    Vector3f angles;

    Vector3f scale;

    public SceneTerrainSurface(@NonNull final SceneTerrainSurfaceDTO dto, @NonNull final TerrainSurfacePrefab prefab) {
        this(dto.name(), prefab, dto.position(), dto.angles(), dto.scale());
    }
}
