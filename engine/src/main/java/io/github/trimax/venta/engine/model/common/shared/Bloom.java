package io.github.trimax.venta.engine.model.common.shared;

import io.github.trimax.venta.engine.model.dto.scene.SceneBloomDTO;
import lombok.Getter;
import lombok.NonNull;

/**
 * The bloom of a scene: the bright parts of the HDR image are blurred over several scales and added back, so the sun, the
 * fire and the lit windows glow. A scene without bloom has none: the effect costs a few full screen passes.
 */
@Getter
public final class Bloom {
    public static final float DEFAULT_INTENSITY = 0.1f;
    public static final float DEFAULT_THRESHOLD = 1.f;
    public static final float DEFAULT_SOFT_KNEE = 0.5f;

    /** How much of the glow is added to the image, a share of the light of the scene */
    private float intensity;

    /** The brightness from which a pixel glows, in the linear light before the exposure */
    private float threshold;

    /** How softly the glow starts around the threshold, from 0 to 1 */
    private float softKnee;

    public Bloom(@NonNull final SceneBloomDTO dto) {
        this(dto.intensity() == null ? DEFAULT_INTENSITY : dto.intensity(),
                dto.threshold() == null ? DEFAULT_THRESHOLD : dto.threshold(),
                dto.softKnee() == null ? DEFAULT_SOFT_KNEE : dto.softKnee());
    }

    public Bloom(final float intensity, final float threshold, final float softKnee) {
        setIntensity(intensity);
        setThreshold(threshold);
        setSoftKnee(softKnee);
    }

    public void setIntensity(final float intensity) {
        if (!Float.isFinite(intensity) || intensity < 0.f)
            throw new IllegalArgumentException("The intensity of the bloom must not be negative, got " + intensity);

        this.intensity = intensity;
    }

    public void setThreshold(final float threshold) {
        if (!Float.isFinite(threshold) || threshold < 0.f)
            throw new IllegalArgumentException("The threshold of the bloom must not be negative, got " + threshold);

        this.threshold = threshold;
    }

    public void setSoftKnee(final float softKnee) {
        if (!(softKnee >= 0.f && softKnee <= 1.f))
            throw new IllegalArgumentException("The soft knee of the bloom must be from 0 to 1, got " + softKnee);

        this.softKnee = softKnee;
    }
}
