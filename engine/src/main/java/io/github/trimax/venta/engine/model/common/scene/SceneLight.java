package io.github.trimax.venta.engine.model.common.scene;

import org.joml.Vector3f;

import io.github.trimax.venta.engine.model.dto.scene.SceneLightDTO;
import io.github.trimax.venta.engine.model.prefabs.LightPrefab;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
public final class SceneLight {
    @NonNull
    String name;

    @NonNull
    LightPrefab prefab;

    @NonNull
    Vector3f position;

    Vector3f direction;

    public SceneLight(@NonNull final SceneLightDTO dto, @NonNull final LightPrefab prefab) {
        this(dto.name(), prefab, dto.position(), dto.direction());
    }
}
