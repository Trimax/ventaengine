package io.github.trimax.venta.engine.model.entity;

import io.github.trimax.venta.engine.model.common.dto.Frame;

import java.util.List;

public interface SpriteEntity extends AbstractEntity {
    TextureEntity getTexture();

    List<Frame> getFrames();

    float getDuration();

    boolean isLooping();
}
