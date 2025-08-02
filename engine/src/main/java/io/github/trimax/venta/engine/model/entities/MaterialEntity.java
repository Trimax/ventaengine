package io.github.trimax.venta.engine.model.entities;

import io.github.trimax.venta.engine.enums.TextureType;
import io.github.trimax.venta.engine.model.dto.MaterialDTO;
import io.github.trimax.venta.engine.model.view.MaterialView;
import io.github.trimax.venta.engine.model.view.TextureView;
import lombok.Getter;
import lombok.NonNull;
import org.joml.Vector2f;

import java.util.HashMap;
import java.util.Map;

@Getter
public final class MaterialEntity extends AbstractEntity implements MaterialView {
    private final Map<TextureType, TextureView> textures = new HashMap<>();
    private final Float shininess;
    private final Float opacity;
    private final Vector2f tiling;
    private final Vector2f offset;


    MaterialEntity(@NonNull final String name, final Float shininess, final Float opacity, final Vector2f tiling, final Vector2f offset) {
        super(name);

        this.shininess = shininess;
        this.opacity = opacity;
        this.tiling = tiling;
        this.offset = offset;
    }

    public MaterialEntity(@NonNull final String name, @NonNull final MaterialDTO dto) {
        this(name, dto.shininess(), dto.opacity(), dto.tiling(), dto.offset());
    }

    @Override
    public void setTexture(final TextureType type, final TextureView texture) {
        this.textures.put(type, texture);
    }

    @Override
    public TextureView getTexture(final TextureType texture) {
        return this.textures.get(texture);
    }
}
