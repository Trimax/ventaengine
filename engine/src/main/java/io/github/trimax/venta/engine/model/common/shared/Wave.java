package io.github.trimax.venta.engine.model.common.shared;

import org.joml.Vector2f;

import io.github.trimax.venta.engine.model.dto.common.WaveDTO;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Data
@NoArgsConstructor
public final class Wave {
    @NonNull
    private Vector2f direction;

    private float amplitude;
    private float steepness;
    private float length;
    private float speed;

    public Wave(@NonNull final Vector2f direction, final float amplitude, final float steepness, final float length, final float speed) {
        setDirection(direction);
        setLength(length);

        this.amplitude = amplitude;
        this.steepness = steepness;
        this.speed = speed;
    }

    public Wave(@NonNull final WaveDTO wave) {
        this(wave.direction(), wave.amplitude(), wave.steepness(), wave.length(), wave.speed());
    }

    /** The shader expects a unit direction, otherwise the wavelength and the displacement are scaled */
    public void setDirection(@NonNull final Vector2f direction) {
        if (direction.lengthSquared() == 0.f)
            throw new IllegalArgumentException("Wave direction must not be zero");

        this.direction = new Vector2f(direction).normalize();
    }

    /** The shader divides by the length, so it must be positive */
    public void setLength(final float length) {
        if (length <= 0.f)
            throw new IllegalArgumentException("Wave length must be positive, got " + length);

        this.length = length;
    }
}