package io.github.trimax.venta.engine.model.common.water;

import io.github.trimax.venta.engine.model.dto.water.WaterFoamDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
public final class WaterFoam {
    private float threshold;
    private float intensity;

    public WaterFoam(@NonNull final WaterFoamDTO dto) {
        this(dto.threshold(), dto.intensity());
    }

    public WaterFoam(@NonNull final WaterFoam foam) {
        this(foam.getThreshold(), foam.getIntensity());
    }
}
