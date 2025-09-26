package io.github.trimax.venta.engine.model.common.scene;

import org.joml.Vector3f;

import io.github.trimax.venta.engine.model.dto.scene.SceneEmitterDTO;
import io.github.trimax.venta.engine.model.prefabs.EmitterPrefab;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
public final class SceneEmitter {
    @NonNull
    String name;

    @NonNull
    EmitterPrefab prefab;

    @NonNull
    Vector3f position;

    public SceneEmitter(@NonNull final SceneEmitterDTO dto, @NonNull final EmitterPrefab prefab) {
        this(dto.name(), prefab, dto.position());
    }
}
