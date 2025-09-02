package io.github.trimax.venta.engine.model.common.effects;

import lombok.Data;
import org.joml.Vector3f;
import org.joml.Vector4f;

@Data
public final class Particle {
    private final Vector3f position;
    private final Vector3f velocity;
    private final Vector3f acceleration;

    private float maxLife;
    private float life;

    private Vector4f color;
    private float size;
    private float rotation;

    public boolean alive() {
        return life > 0;
    }
}
