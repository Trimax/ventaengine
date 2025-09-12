package io.github.trimax.venta.engine.model.entity;

import io.github.trimax.venta.engine.enums.TextureType;
import lombok.NonNull;
import org.joml.Vector2fc;
import org.joml.Vector4fc;

public interface MaterialEntity extends AbstractEntity {
    Vector2fc getTiling();

    Vector2fc getOffset();

    Vector4fc getColor();

    TextureEntity getTexture(@NonNull final TextureType texture);

    void setTiling(@NonNull final Vector2fc tiling);

    void setOffset(@NonNull final Vector2fc tiling);

    void setColor(@NonNull final Vector4fc color);

    void setTexture(@NonNull final TextureType type, @NonNull final TextureEntity texture);
}
