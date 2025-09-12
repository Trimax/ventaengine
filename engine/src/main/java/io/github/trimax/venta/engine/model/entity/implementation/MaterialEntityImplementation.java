package io.github.trimax.venta.engine.model.entity.implementation;

import io.github.trimax.venta.engine.enums.TextureType;
import io.github.trimax.venta.engine.model.common.dto.Color;
import io.github.trimax.venta.engine.model.dto.MaterialDTO;
import io.github.trimax.venta.engine.model.entity.MaterialEntity;
import io.github.trimax.venta.engine.model.entity.TextureEntity;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import org.joml.Vector2f;
import org.joml.Vector2fc;
import org.joml.Vector4f;
import org.joml.Vector4fc;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Getter
@AllArgsConstructor(access = AccessLevel.PACKAGE)
public final class MaterialEntityImplementation extends AbstractEntityImplementation implements MaterialEntity {
    private final Map<TextureType, TextureEntityImplementation> textures = new HashMap<>();
    private final float metalness;
    private final float roughness;
    private final float opacity;
    private final Vector4f color;
    private final Vector2f tiling;
    private final Vector2f offset;

    MaterialEntityImplementation(@NonNull final MaterialDTO dto) {
        this(Optional.ofNullable(dto.metalness()).orElse(0.f),
                Optional.ofNullable(dto.roughness()).orElse(0.f),
                Optional.ofNullable(dto.opacity()).orElse(0.f),
                Optional.ofNullable(dto.color()).map(Color::toVector4f).orElse(new Vector4f(1.f)),
                Optional.ofNullable(dto.tiling()).orElse(new Vector2f(1.f)),
                Optional.ofNullable(dto.offset()).orElse(new Vector2f(0.f)));
    }

    @Override
    public void setTexture(final TextureType type, final TextureEntity texture) {
        if (texture instanceof TextureEntityImplementation t)
            this.textures.put(type, t);
    }

    @Override
    public void setTiling(@NonNull final Vector2fc tiling) {
        this.tiling.set(tiling);
    }

    @Override
    public void setOffset(@NonNull final Vector2fc offset) {
        this.offset.set(offset);
    }

    @Override
    public void setColor(@NonNull final Vector4fc color) {
        this.color.set(color);
    }

    @Override
    public TextureEntityImplementation getTexture(final TextureType texture) {
        return this.textures.get(texture);
    }
}
