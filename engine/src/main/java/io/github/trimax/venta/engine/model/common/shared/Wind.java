package io.github.trimax.venta.engine.model.common.shared;

import org.joml.Vector2f;
import org.joml.Vector2fc;

import io.github.trimax.venta.engine.model.dto.scene.SceneWindDTO;
import lombok.Getter;
import lombok.NonNull;

/**
 * The wind of a scene. It sways the surfaces whose material follows the wind (the wind influence of the material): trees
 * lean along the direction and the gusts swing them, the more, the higher a point is on its mesh.
 */
public final class Wind {
    private static final float DEFAULT_STRENGTH = 0.5f;
    private static final float DEFAULT_SPEED = 1.f;

    /** The unit direction the wind blows to in the horizontal plane: x of the world and z of the world */
    private final Vector2f direction = new Vector2f(1.f, 0.f);

    /** How far (in the units of the world) the top of a surface that fully follows the wind is pushed, 0 is a calm */
    @Getter
    private float strength;

    /** How fast the gusts follow each other, 1 is a gust every few seconds */
    @Getter
    private float speed;

    public Wind(@NonNull final SceneWindDTO dto) {
        this(dto.direction() == null ? 1.f : dto.direction().x, dto.direction() == null ? 0.f : dto.direction().z,
                dto.strength() == null ? DEFAULT_STRENGTH : dto.strength(), dto.speed() == null ? DEFAULT_SPEED : dto.speed());
    }

    public Wind(final float directionX, final float directionZ, final float strength, final float speed) {
        setDirection(directionX, directionZ);
        setStrength(strength);
        setSpeed(speed);
    }

    public Vector2fc getDirection() {
        return direction;
    }

    /** Sets the direction the wind blows to; it is normalized, and a zero vector is refused: it has no direction */
    public void setDirection(final float x, final float z) {
        if (!Float.isFinite(x) || !Float.isFinite(z) || (x == 0.f && z == 0.f))
            throw new IllegalArgumentException("The wind needs a direction in the horizontal plane, got (" + x + ", " + z + ")");

        direction.set(x, z).normalize();
    }

    public void setStrength(final float strength) {
        if (!Float.isFinite(strength) || strength < 0.f)
            throw new IllegalArgumentException("The strength of the wind must not be negative, got " + strength);

        this.strength = strength;
    }

    public void setSpeed(final float speed) {
        if (!Float.isFinite(speed) || speed < 0.f)
            throw new IllegalArgumentException("The speed of the wind must not be negative, got " + speed);

        this.speed = speed;
    }
}
