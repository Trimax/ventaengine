package io.github.trimax.venta.engine.model.entity;

import io.github.trimax.venta.engine.enums.TextureType;
import lombok.NonNull;
import org.joml.Vector2f;
import org.joml.Vector2fc;

public interface MaterialEntity extends AbstractEntity {
    Vector2fc getTiling();

    TextureEntity getTexture(@NonNull final TextureType texture);

    void setTiling(@NonNull final Vector2f tiling);

    void setTexture(@NonNull final TextureType type, @NonNull final TextureEntity texture);
}
