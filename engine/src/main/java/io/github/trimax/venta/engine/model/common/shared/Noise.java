package io.github.trimax.venta.engine.model.common.shared;

import io.github.trimax.venta.engine.model.dto.common.NoiseDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
public final class Noise {
    private float scale;
    private float speed;
    private float offset;
    private float strength;

    public Noise(@NonNull final NoiseDTO noise) {
        this(noise.scale(), noise.speed(), noise.offset(), noise.strength());
    }
}
