package io.github.trimax.venta.engine.model.entity.implementation;

import io.github.trimax.venta.engine.enums.AlphaMode;
import io.github.trimax.venta.engine.enums.TextureType;
import io.github.trimax.venta.engine.model.common.reflection.ReflectionSettings;
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
import org.joml.Vector3f;
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
    Vector3f emissive;

    /** How the objects of the material reflect their surroundings, null for the objects that reflect the skybox only */
    ReflectionSettings reflections;

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
                checkWindInfluence(Optional.ofNullable(dto.windInfluence()).orElse(0.f)),
                readEmissive(dto),
                dto.dynamicReflections() == null ? null : new ReflectionSettings(dto.dynamicReflections()));
    }

    /** The light that the surface gives out by itself: its color by its intensity (black for a surface that gives out none) */
    private static Vector3f readEmissive(final MaterialDTO dto) {
        final var intensity = Optional.ofNullable(dto.emissiveIntensity()).orElse(1.f);
        if (!(intensity >= 0.f && Float.isFinite(intensity)))
            throw new IllegalArgumentException("The emissive intensity must not be negative, got " + intensity);

        final var color = Optional.ofNullable(dto.emissive()).map(ColorDTO::toVector4f).orElse(new Vector4f(0.f));
        if (!(color.x >= 0.f && color.y >= 0.f && color.z >= 0.f && Float.isFinite(color.x + color.y + color.z)))
            throw new IllegalArgumentException("The emissive color must not be negative, got " + color);

        return new Vector3f(color.x, color.y, color.z).mul(intensity);
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

    /** Whether the objects of the material have a cube map of their surroundings to reflect */
    public boolean hasReflections() {
        return reflections != null;
    }

    /** Whether the wind sways the surface */
    public boolean followsWind() {
        return windInfluence > 0.f;
    }

    /** Whether the alpha is ignored and every fragment is opaque */
    public boolean isOpaque() {
        return alphaMode == AlphaMode.Opaque;
    }

    /** Whether the surface has no partly transparent fragments: it is opaque or has holes, so it needs no sorting */
    public boolean isSolid() {
        return isOpaque() || isCutout();
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
