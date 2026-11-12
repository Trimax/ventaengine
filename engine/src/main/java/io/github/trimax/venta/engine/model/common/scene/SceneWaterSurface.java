package io.github.trimax.venta.engine.model.common.scene;

import org.joml.Vector3f;

import io.github.trimax.venta.engine.model.dto.scene.SceneWaterSurfaceDTO;
import io.github.trimax.venta.engine.model.prefabs.WaterSurfacePrefab;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
public final class SceneWaterSurface {
    @NonNull
    String name;

    @NonNull
    WaterSurfacePrefab prefab;

    @NonNull
    Vector3f position;

    Vector3f angles;

    Vector3f scale;

    public SceneWaterSurface(@NonNull final SceneWaterSurfaceDTO dto, @NonNull final WaterSurfacePrefab prefab) {
        this(dto.name(), prefab, dto.position(), dto.angles(), dto.scale());
    }
}
