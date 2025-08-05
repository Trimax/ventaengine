package io.github.trimax.venta.engine.model.entity;

import io.github.trimax.venta.engine.enums.TextureType;
import lombok.NonNull;

public interface MaterialEntity extends AbstractEntity {
    void setTexture(@NonNull final TextureType type, @NonNull final TextureEntity texture);

    TextureEntity getTexture(@NonNull final TextureType texture);
}
