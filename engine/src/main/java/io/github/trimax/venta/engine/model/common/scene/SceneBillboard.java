package io.github.trimax.venta.engine.model.common.scene;

import org.joml.Vector2f;
import org.joml.Vector3f;

import io.github.trimax.venta.engine.model.dto.scene.SceneBillboardDTO;
import io.github.trimax.venta.engine.model.prefabs.BillboardPrefab;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
public final class SceneBillboard {
    @NonNull
    String name;

    @NonNull
    BillboardPrefab prefab;

    @NonNull
    Vector3f position;

    @NonNull
    Vector2f size;

    public SceneBillboard(@NonNull final SceneBillboardDTO dto, @NonNull final BillboardPrefab prefab) {
        this(dto.name(), prefab, dto.position(), dto.size());
    }
}
