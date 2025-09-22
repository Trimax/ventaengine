package io.github.trimax.venta.engine.model.entity;

import org.joml.Vector4fc;

public interface SpriteEntity extends AbstractEntity {
    TextureEntity getTexture();

    float getDuration();

    Vector4fc getColor();

    boolean isLooping();
}
