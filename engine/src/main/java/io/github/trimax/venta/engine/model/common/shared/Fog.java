package io.github.trimax.venta.engine.model.common.shared;

import org.joml.Vector3f;
import org.joml.Vector3fc;

import io.github.trimax.venta.engine.model.dto.scene.SceneFogDTO;
import lombok.Data;
import lombok.NonNull;

@Data
public final class Fog {
    /**
     * The shader divides by (maximal - minimal), so the distances are kept at least this far apart
     */
    private static final float MINIMAL_RANGE = 0.001f;

    public static final float DEFAULT_FALLOFF = 0.02f;
    public static final float DEFAULT_SUN_SHARPNESS = 8.f;

    private float minimalDistance;
    private float maximalDistance;

    /**
     * Top of the fog layer in world space: only the part of the way below it is fogged.
     * Infinite by default, so the whole way is fogged and the fog hides the whole sky.
     */
    private float height = Float.POSITIVE_INFINITY;

    /**
     * The density of the exponential fog at its base height, 0 is the fog of a layer (the distances and the height of the top).
     * The part of the light that is lost on a unit of the way is about this much.
     */
    private float density;

    /**
     * How fast the exponential fog thins with the height: the density falls e times on 1 / falloff units
     */
    private float falloff = DEFAULT_FALLOFF;

    /**
     * The height where the density of the exponential fog is {@link #density}
     */
    private float baseHeight;

    /**
     * How much the fog takes the color of the sun when it is seen towards the sun, from 0 (never) to 1
     */
    private float sunScattering;

    /**
     * How narrow the glow of the sun in the fog is: the greater, the narrower
     */
    private float sunSharpness = DEFAULT_SUN_SHARPNESS;

    @NonNull
    private Vector3f color;

    public Fog(@NonNull final SceneFogDTO dto) {
        this(dto.distance() == null ? 0.f : dto.distance().min(), dto.distance() == null ? MINIMAL_RANGE : dto.distance().max(),
                dto.color().toVector3f());

        if (dto.distance() == null && dto.density() == null)
            throw new IllegalArgumentException("The fog needs the distances of a layer or the density of an exponential fog");

        if (dto.height() != null)
            this.height = dto.height();

        if (dto.density() != null)
            setDensity(dto.density());

        if (dto.falloff() != null)
            setFalloff(dto.falloff());

        if (dto.baseHeight() != null)
            setBaseHeight(dto.baseHeight());

        if (dto.sunScattering() != null)
            setSunScattering(dto.sunScattering());

        if (dto.sunSharpness() != null)
            setSunSharpness(dto.sunSharpness());
    }

    public Fog(final float minimalDistance, final float maximalDistance, final float height, @NonNull final Vector3fc color) {
        this(minimalDistance, maximalDistance, color);
        this.height = height;
    }

    public Fog(final float minimalDistance, final float maximalDistance, @NonNull final Vector3fc color) {
        if (minimalDistance < 0.0f)
            throw new IllegalArgumentException("Minimal distance must be greater than or equal to 0.0f");

        if (maximalDistance <= minimalDistance)
            throw new IllegalArgumentException("Maximal distance must be greater than minimal distance");

        this.minimalDistance = minimalDistance;
        this.maximalDistance = maximalDistance;
        this.color = new Vector3f(color);
    }

    /**
     * Whether the fog is an exponential one: its density falls with the height
     */
    public boolean isExponential() {
        return density > 0.f;
    }

    /**
     * Sets the minimal distance; the maximal distance is moved forward if it would not be greater anymore
     */
    public void setMinimalDistance(final float minimalDistance) {
        this.minimalDistance = Math.max(0, minimalDistance);
        this.maximalDistance = Math.max(this.maximalDistance, this.minimalDistance + MINIMAL_RANGE);
    }

    /**
     * Sets the maximal distance; it is kept greater than the minimal distance
     */
    public void setMaximalDistance(final float maximalDistance) {
        this.maximalDistance = Math.max(maximalDistance, minimalDistance + MINIMAL_RANGE);
    }

    /**
     * Sets the density of the exponential fog, 0 makes it a fog of a layer
     */
    public void setDensity(final float density) {
        if (!Float.isFinite(density) || density < 0.f)
            throw new IllegalArgumentException("The density of the fog must not be negative, got " + density);

        this.density = density;
    }

    public void setFalloff(final float falloff) {
        if (!Float.isFinite(falloff) || falloff <= 0.f)
            throw new IllegalArgumentException("The falloff of the fog must be positive, got " + falloff);

        this.falloff = falloff;
    }

    public void setBaseHeight(final float baseHeight) {
        if (!Float.isFinite(baseHeight))
            throw new IllegalArgumentException("The base height of the fog must be finite, got " + baseHeight);

        this.baseHeight = baseHeight;
    }

    public void setSunScattering(final float sunScattering) {
        if (!(sunScattering >= 0.f && sunScattering <= 1.f))
            throw new IllegalArgumentException("The sun scattering of the fog must be from 0 to 1, got " + sunScattering);

        this.sunScattering = sunScattering;
    }

    public void setSunSharpness(final float sunSharpness) {
        if (!Float.isFinite(sunSharpness) || sunSharpness < 1.f)
            throw new IllegalArgumentException("The sun sharpness of the fog must be at least 1, got " + sunSharpness);

        this.sunSharpness = sunSharpness;
    }
}
