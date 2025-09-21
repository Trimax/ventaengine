package io.github.trimax.venta.engine.model.entity;

public interface SpriteEntity extends AbstractEntity {
    TextureEntity getTexture();

    float getDuration();

    boolean isLooping();
}
