package io.github.trimax.venta.engine.model.entity;

import io.github.trimax.venta.engine.enums.AlphaMode;
import io.github.trimax.venta.engine.enums.TextureType;
import lombok.NonNull;
import org.joml.Vector2fc;
import org.joml.Vector4fc;

public interface MaterialEntity extends AbstractEntity {
    Vector2fc getTiling();

    Vector2fc getOffset();

    Vector4fc getColor();

    /** Whether the alpha of the material mixes colors or cuts holes */
    AlphaMode getAlphaMode();

    /** The alpha below which a cutout material has a hole, from 0 to 1 */
    float getAlphaCutoff();

    /** How much the wind of the scene sways the surface: 0 is not at all, 1 is fully (leaves); the surface bends the more the higher it is on its mesh */
    float getWindInfluence();

    /** Whether both sides of a face are drawn; one sided materials are drawn with the faces turned counter-clockwise to the viewer only */
    boolean isDoubleSided();

    TextureEntity getTexture(@NonNull final TextureType texture);

    void setTiling(@NonNull final Vector2fc tiling);

    void setOffset(@NonNull final Vector2fc tiling);

    void setColor(@NonNull final Vector4fc color);

    void setTexture(@NonNull final TextureType type, @NonNull final TextureEntity texture);
}
