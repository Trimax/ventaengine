package io.github.trimax.venta.engine.model.common.terrain;

import io.github.trimax.venta.engine.model.entity.MaterialEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
public final class TerrainMaterial {
    @NonNull
    private MaterialEntity material;

    private float elevation;
}
