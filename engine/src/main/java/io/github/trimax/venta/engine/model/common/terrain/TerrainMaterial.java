package io.github.trimax.venta.engine.model.common.terrain;

import io.github.trimax.venta.engine.enums.TextureType;
import io.github.trimax.venta.engine.model.entity.MaterialEntity;
import io.github.trimax.venta.engine.model.entity.implementation.TextureArrayEntityImplementation;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public final class TerrainMaterial {
    @NonNull
    List<MaterialEntity> materials;

    @NonNull
    Map<TextureType, TextureArrayEntityImplementation> textureArrays = new HashMap<>();

    private float[] elevations;
}
