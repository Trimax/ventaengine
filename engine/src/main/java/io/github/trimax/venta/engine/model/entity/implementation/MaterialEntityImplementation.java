package io.github.trimax.venta.engine.model.entity.implementation;

import io.github.trimax.venta.engine.enums.TextureType;
import io.github.trimax.venta.engine.model.dto.MaterialDTO;
import io.github.trimax.venta.engine.model.entity.MaterialEntity;
import io.github.trimax.venta.engine.model.entity.TextureEntity;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import org.joml.Vector2f;

import java.util.HashMap;
import java.util.Map;

@Getter
@AllArgsConstructor(access = AccessLevel.PACKAGE)
public final class MaterialEntityImplementation extends AbstractEntityImplementation implements MaterialEntity {
    private final Map<TextureType, TextureEntityImplementation> textures = new HashMap<>();
    private final Float shininess;
    private final Float opacity;
    private final Vector2f tiling;
    private final Vector2f offset;

    MaterialEntityImplementation(@NonNull final MaterialDTO dto) {
        this(dto.shininess(), dto.opacity(), dto.tiling(), dto.offset());
    }

    @Override
    public void setTexture(final TextureType type, final TextureEntity texture) {
        if (texture instanceof TextureEntityImplementation t)
            this.textures.put(type, t);
    }

    @Override
    public void setTiling(@NonNull final Vector2f tiling) {
        this.tiling.set(tiling);
    }

    @Override
    public TextureEntityImplementation getTexture(final TextureType texture) {
        return this.textures.get(texture);
    }
}
