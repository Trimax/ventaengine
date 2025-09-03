package io.github.trimax.venta.engine.model.common.effects;

import lombok.Data;
import org.joml.Vector3f;
import org.joml.Vector4f;

@Data
public final class Particle {
    private final Vector3f position;
    private final Vector3f velocity;
    private final Vector3f acceleration;

    private final Vector4f color = new Vector4f(1f);
    private float size = 1.f;
    private float rotation;

    private float maxLife;
    private float life;

    public boolean alive() {
        return life > 0;
    }

    public void update(final float delta) {
        velocity.fma(delta, acceleration);      // velocity += a*dt
        position.fma(delta, velocity);          // position += v*dt

        life = Math.max(life - delta, 0);
        color.w = life / maxLife;
    }
}
