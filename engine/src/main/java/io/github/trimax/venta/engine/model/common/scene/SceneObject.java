package io.github.trimax.venta.engine.model.common.scene;

import org.joml.Vector3f;

import io.github.trimax.venta.engine.model.dto.scene.SceneObjectDTO;
import io.github.trimax.venta.engine.model.prefabs.ObjectPrefab;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
public final class SceneObject {
    @NonNull
    String name;

    @NonNull
    ObjectPrefab prefab;

    @NonNull
    Vector3f position;

    @NonNull
    Vector3f angles;

    @NonNull
    Vector3f scale;

    public SceneObject(@NonNull final SceneObjectDTO dto, @NonNull final ObjectPrefab prefab) {
        this(dto.name(), prefab, dto.position(), dto.angles(), dto.scale());
    }
}
