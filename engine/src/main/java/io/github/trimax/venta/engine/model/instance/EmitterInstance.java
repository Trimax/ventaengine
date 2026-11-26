package io.github.trimax.venta.engine.model.instance;

import org.joml.Vector3fc;

import io.github.trimax.venta.engine.enums.DrawMode;
import io.github.trimax.venta.engine.model.entity.TextureEntity;
import lombok.NonNull;

public interface EmitterInstance extends AbstractInstance {
    Vector3fc getPosition();

    Vector3fc getParticleVelocity();

    Vector3fc getParticleVelocityDeviation();

    TextureEntity getTexture();

    float getEmissionRate();

    boolean isLit();

    /** How the particles are drawn: filled, as lines or as points */
    DrawMode getDrawMode();

    void setPosition(@NonNull final Vector3fc position);

    void setParticleVelocity(@NonNull final Vector3fc particleVelocity);

    void setParticleVelocityDeviation(@NonNull final Vector3fc particleVelocityDeviation);

    void setEmissionRate(final float rate);

    void setLit(final boolean lit);

    void setDrawMode(@NonNull final DrawMode drawMode);

    void setTexture(@NonNull final TextureEntity texture);
}
