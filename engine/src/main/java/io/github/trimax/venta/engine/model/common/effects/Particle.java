package io.github.trimax.venta.engine.model.common.effects;

import org.joml.Vector3f;
import org.joml.Vector4f;

import lombok.Data;

@Data
public final class Particle {
    private final Vector3f position;
    private final Vector3f velocity;
    private final Vector3f acceleration;

    private final Vector4f color = new Vector4f(1f);

    private float angularVelocity;
    private float rotation;

    private float minimalSize = 0.f;
    private float maximalSize = 1.f;
    private float size = 0.f;

    private float maxLife;
    private float life;

    public boolean alive() {
        return life > 0;
    }

    public void update(final float delta) {
        size = Math.clamp(size + delta, minimalSize, maximalSize);
        velocity.fma(delta, acceleration);      // velocity += a*dt
        position.fma(delta, velocity);          // position += v*dt

        rotation += angularVelocity * delta;

        life = Math.max(life - delta, 0);
        color.w = life / maxLife;
    }
}
