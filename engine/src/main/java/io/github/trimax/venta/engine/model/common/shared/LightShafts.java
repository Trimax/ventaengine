package io.github.trimax.venta.engine.model.common.shared;

import io.github.trimax.venta.engine.model.dto.scene.SceneLightShaftsDTO;
import lombok.Getter;
import lombok.NonNull;

/**
 * The light shafts of a scene: the light of the sun scattered in the exponential fog, seen through the gaps between what
 * shadows it. The effect is a march along every view ray that tests its points against the shadow maps of the sun.
 */
@Getter
public final class LightShafts {
    public static final float DEFAULT_INTENSITY = 1.f;
    public static final int DEFAULT_STEPS = 32;
    public static final float DEFAULT_ANISOTROPY = 0.6f;

    public static final int MINIMAL_STEPS = 4;
    public static final int MAXIMAL_STEPS = 128;

    /** How bright the shafts are, 0 is none */
    private float intensity;

    /** The points of a view ray that are tested against the shadows */
    private int steps;

    /** How much the fog scatters the light forward, from -1 to 1 (not including them: the phase function would be infinite) */
    private float anisotropy;

    public LightShafts(@NonNull final SceneLightShaftsDTO dto) {
        this(dto.intensity() == null ? DEFAULT_INTENSITY : dto.intensity(),
                dto.steps() == null ? DEFAULT_STEPS : dto.steps(),
                dto.anisotropy() == null ? DEFAULT_ANISOTROPY : dto.anisotropy());
    }

    public LightShafts(final float intensity, final int steps, final float anisotropy) {
        setIntensity(intensity);
        setSteps(steps);
        setAnisotropy(anisotropy);
    }

    public void setIntensity(final float intensity) {
        if (!Float.isFinite(intensity) || intensity < 0.f)
            throw new IllegalArgumentException("The intensity of the light shafts must not be negative, got " + intensity);

        this.intensity = intensity;
    }

    public void setSteps(final int steps) {
        if (steps < MINIMAL_STEPS || steps > MAXIMAL_STEPS)
            throw new IllegalArgumentException(String.format("The steps of the light shafts must be from %d to %d, got %d", MINIMAL_STEPS, MAXIMAL_STEPS, steps));

        this.steps = steps;
    }

    public void setAnisotropy(final float anisotropy) {
        if (!(anisotropy > -1.f && anisotropy < 1.f))
            throw new IllegalArgumentException("The anisotropy of the light shafts must be between -1 and 1, got " + anisotropy);

        this.anisotropy = anisotropy;
    }
}
