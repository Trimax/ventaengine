package io.github.trimax.venta.engine.model.instance.implementation;

import io.github.trimax.venta.engine.enums.TextureType;
import io.github.trimax.venta.engine.model.dto.MaterialDTO;
import io.github.trimax.venta.engine.model.entity.TextureEntity;
import io.github.trimax.venta.engine.model.entity.implementation.TextureEntityImplementation;
import io.github.trimax.venta.engine.model.instance.MaterialInstance;
import lombok.Getter;
import lombok.NonNull;
import org.joml.Vector2f;

import java.util.HashMap;
import java.util.Map;

@Getter
public final class MaterialInstanceImplementation extends AbstractInstanceImplementation implements MaterialInstance {
    private final Map<TextureType, TextureEntityImplementation> textures = new HashMap<>();
    private final Float shininess;
    private final Float opacity;
    private final Vector2f tiling;
    private final Vector2f offset;


    MaterialInstanceImplementation(@NonNull final String name, final Float shininess, final Float opacity, final Vector2f tiling, final Vector2f offset) {
        super(name);

        this.shininess = shininess;
        this.opacity = opacity;
        this.tiling = tiling;
        this.offset = offset;
    }

    public MaterialInstanceImplementation(@NonNull final String name, @NonNull final MaterialDTO dto) {
        this(name, dto.shininess(), dto.opacity(), dto.tiling(), dto.offset());
    }

    @Override
    public void setTexture(final TextureType type, final TextureEntity texture) {
        if (texture instanceof TextureEntityImplementation t)
            this.textures.put(type, t);
    }

    @Override
    public TextureEntityImplementation getTexture(final TextureType texture) {
        return this.textures.get(texture);
    }
}
