package io.github.trimax.venta.engine.model.common.shared;

import org.joml.Vector3f;

import io.github.trimax.venta.engine.model.dto.scene.SceneDirectionalLightDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
public final class DirectionalLight {
    @NonNull
    private Vector3f color;

    @NonNull
    private Vector3f direction;

    private float intensity;

    public DirectionalLight(@NonNull final SceneDirectionalLightDTO dto) {
        this(dto.color().toVector3f(), dto.direction(), dto.intensity());
    }
}
