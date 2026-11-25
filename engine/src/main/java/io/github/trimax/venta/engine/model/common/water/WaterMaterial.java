package io.github.trimax.venta.engine.model.common.water;

import org.joml.Vector3f;

import io.github.trimax.venta.engine.model.dto.water.WaterMaterialDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
public final class WaterMaterial {
    public static final float DEFAULT_DEPTH_DISTANCE = 8.f;
    public static final float DEFAULT_REFRACTION = 0.04f;
    public static final float MAXIMAL_REFRACTION = 0.5f;

    @NonNull
    Vector3f colorSurface;

    @NonNull
    Vector3f colorDepth;

    @NonNull
    Vector3f colorPeak;

    float metalness;
    float opacity;

    /** How deep (along the view, in the units of the world) the water has to be to hide what lies under it almost entirely */
    float depthDistance;

    /** How far the bottom seen through the water is shifted by its ripples, a share of the screen (0 is none) */
    float refraction;

    public WaterMaterial(@NonNull final WaterMaterialDTO dto) {
        this(dto.colorSurface().toVector3f(), dto.colorDepth().toVector3f(), dto.colorPeak().toVector3f(),
                dto.metalness(), dto.opacity(),
                dto.depthDistance() == null ? DEFAULT_DEPTH_DISTANCE : checkDepthDistance(dto.depthDistance()),
                dto.refraction() == null ? DEFAULT_REFRACTION : checkRefraction(dto.refraction()));
    }

    public WaterMaterial(@NonNull final WaterMaterial material) {
        this(new Vector3f(material.colorSurface), new Vector3f(material.colorDepth), new Vector3f(material.colorPeak),
                material.getMetalness(), material.getOpacity(), material.getDepthDistance(), material.getRefraction());
    }

    private static float checkDepthDistance(final float depthDistance) {
        if (!(depthDistance > 0.f && Float.isFinite(depthDistance)))
            throw new IllegalArgumentException("The depth distance of water must be positive, got " + depthDistance);

        return depthDistance;
    }

    private static float checkRefraction(final float refraction) {
        if (!(refraction >= 0.f && refraction <= MAXIMAL_REFRACTION))
            throw new IllegalArgumentException(String.format("The refraction of water must be from 0 to %s, got %s", MAXIMAL_REFRACTION, refraction));

        return refraction;
    }
}
