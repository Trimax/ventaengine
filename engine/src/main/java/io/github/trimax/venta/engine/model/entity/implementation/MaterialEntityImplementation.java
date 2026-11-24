package io.github.trimax.venta.engine.model.entity.implementation;

import io.github.trimax.venta.engine.enums.AlphaMode;
import io.github.trimax.venta.engine.enums.TextureType;
import io.github.trimax.venta.engine.model.dto.MaterialDTO;
import io.github.trimax.venta.engine.model.dto.common.ColorDTO;
import io.github.trimax.venta.engine.model.entity.MaterialEntity;
import io.github.trimax.venta.engine.model.entity.TextureEntity;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.Value;
import org.joml.Vector2f;
import org.joml.Vector2fc;
import org.joml.Vector4f;
import org.joml.Vector4fc;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Value
@AllArgsConstructor(access = AccessLevel.PACKAGE)
public class MaterialEntityImplementation extends AbstractEntityImplementation implements MaterialEntity {
    Map<TextureType, TextureEntityImplementation> textures = new HashMap<>();
    float metalness;
    float roughness;
    float opacity;
    Vector4f color;
    Vector2f tiling;
    Vector2f offset;
    AlphaMode alphaMode;
    float alphaCutoff;
    boolean doubleSided;
    float windInfluence;

    MaterialEntityImplementation(@NonNull final MaterialDTO dto) {
        this(Optional.ofNullable(dto.metalness()).orElse(0.f),
                Optional.ofNullable(dto.roughness()).orElse(0.f),
                Optional.ofNullable(dto.opacity()).orElse(0.f),
                Optional.ofNullable(dto.color()).map(ColorDTO::toVector4f).orElse(new Vector4f(1.f)),
                Optional.ofNullable(dto.tiling()).orElse(new Vector2f(1.f)),
                Optional.ofNullable(dto.offset()).orElse(new Vector2f(0.f)),
                Optional.ofNullable(dto.alphaMode()).orElse(AlphaMode.Blend),
                checkAlphaCutoff(Optional.ofNullable(dto.alphaCutoff()).orElse(DEFAULT_ALPHA_CUTOFF)),
                Optional.ofNullable(dto.doubleSided()).orElse(true),
                checkWindInfluence(Optional.ofNullable(dto.windInfluence()).orElse(0.f)));
    }

    private static float checkWindInfluence(final float windInfluence) {
        if (!(windInfluence >= 0.f && windInfluence <= 1.f))
            throw new IllegalArgumentException("The wind influence must be from 0 to 1, got " + windInfluence);

        return windInfluence;
    }

    /** The cutoff of a material that doesn't set one: the alpha of a pixel is a hole below it */
    public static final float DEFAULT_ALPHA_CUTOFF = 0.5f;

    private static float checkAlphaCutoff(final float alphaCutoff) {
        if (!(alphaCutoff >= 0.f && alphaCutoff <= 1.f))
            throw new IllegalArgumentException("The alpha cutoff must be from 0 to 1, got " + alphaCutoff);

        return alphaCutoff;
    }

    /** Whether the wind sways the surface */
    public boolean followsWind() {
        return windInfluence > 0.f;
    }

    /** Whether the fragments with a low alpha are discarded and the others are opaque */
    public boolean isCutout() {
        return alphaMode == AlphaMode.Cutout;
    }

    @Override
    public void setTexture(final @NonNull TextureType type, final @NonNull TextureEntity texture) {
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
    public TextureEntityImplementation getTexture(@NonNull final TextureType texture) {
        return this.textures.get(texture);
    }
}
