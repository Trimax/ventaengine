package io.github.trimax.venta.engine.model.common.scene;

import org.joml.Vector3f;

import io.github.trimax.venta.engine.model.dto.scene.SceneSoundSourceDTO;
import io.github.trimax.venta.engine.model.prefabs.SoundSourcePrefab;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
public final class SceneSoundSource {
    @NonNull
    String name;

    @NonNull
    SoundSourcePrefab prefab;

    @NonNull
    Vector3f position;

    public SceneSoundSource(@NonNull final SceneSoundSourceDTO dto, @NonNull final SoundSourcePrefab prefab) {
        this(dto.name(), prefab, dto.position());
    }
}
