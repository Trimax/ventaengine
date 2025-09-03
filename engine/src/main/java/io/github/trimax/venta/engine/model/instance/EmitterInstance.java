package io.github.trimax.venta.engine.model.instance;

import io.github.trimax.venta.engine.model.entity.TextureEntity;
import lombok.NonNull;
import org.joml.Vector3fc;

public interface EmitterInstance extends AbstractInstance {
    Vector3fc getPosition();

    Vector3fc getVelocity();

    Vector3fc getDeviation();

    TextureEntity getTexture();

    float getEmissionRate();

    void setPosition(@NonNull final Vector3fc position);

    void setVelocity(@NonNull final Vector3fc velocity);

    void setDeviation(@NonNull final Vector3fc deviation);

    void setEmissionRate(final float rate);

    void setTexture(@NonNull final TextureEntity texture);
}
