package io.github.trimax.venta.engine.model.common.shared;

import org.joml.Vector2f;

import io.github.trimax.venta.engine.model.dto.common.WaveDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
public final class Wave {
    @NonNull
    private Vector2f direction;

    private float amplitude;
    private float steepness;
    private float length;
    private float speed;

    public Wave(@NonNull final WaveDTO wave) {
        this(wave.direction(), wave.amplitude(), wave.steepness(), wave.length(), wave.speed());
    }
}