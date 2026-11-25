package io.github.trimax.venta.engine.model.common.shared;

import io.github.trimax.venta.engine.enums.AntialiasingType;
import io.github.trimax.venta.engine.enums.FxaaQuality;
import io.github.trimax.venta.engine.model.dto.scene.SceneAntialiasingDTO;
import lombok.Getter;
import lombok.NonNull;

/**
 * The antialiasing of a scene: the multisampling smooths the edges of the geometry only, the alpha cutout, the bloom, the
 * ambient occlusion and the shafts of light have edges that it doesn't touch, so a pass over the finished image smooths them.
 * A scene without it has none: the pass costs a full screen draw.
 */
@Getter
public final class Antialiasing {
    public static final AntialiasingType DEFAULT_TYPE = AntialiasingType.FXAA;
    public static final FxaaQuality DEFAULT_QUALITY = FxaaQuality.Medium;
    public static final float DEFAULT_SUBPIXEL = 0.75f;
    public static final float DEFAULT_EDGE_THRESHOLD = 0.125f;

    /** The least threshold: a pixel needs some contrast to be on an edge, or the noise of the image would be blurred */
    public static final float MINIMAL_EDGE_THRESHOLD = 0.0312f;

    public static final float MAXIMAL_EDGE_THRESHOLD = 0.5f;

    private AntialiasingType type;
    private FxaaQuality quality;

    /** How much the lines thinner than a pixel are smoothed, from 0 to 1 */
    private float subpixel;

    /** The contrast of the brightness (a share of the brightest neighbour) from which a pixel is on an edge */
    private float edgeThreshold;

    public Antialiasing(@NonNull final SceneAntialiasingDTO dto) {
        this(dto.type() == null ? DEFAULT_TYPE : dto.type(),
                dto.quality() == null ? DEFAULT_QUALITY : dto.quality(),
                dto.subpixel() == null ? DEFAULT_SUBPIXEL : dto.subpixel(),
                dto.edgeThreshold() == null ? DEFAULT_EDGE_THRESHOLD : dto.edgeThreshold());
    }

    public Antialiasing(@NonNull final AntialiasingType type, @NonNull final FxaaQuality quality, final float subpixel, final float edgeThreshold) {
        this.type = type;
        this.quality = quality;
        setSubpixel(subpixel);
        setEdgeThreshold(edgeThreshold);
    }

    /** Whether the pass is made: a scene can keep the settings of an antialiasing that is switched off */
    public boolean isActive() {
        return type == AntialiasingType.FXAA;
    }

    public void setType(@NonNull final AntialiasingType type) {
        this.type = type;
    }

    public void setQuality(@NonNull final FxaaQuality quality) {
        this.quality = quality;
    }

    public void setSubpixel(final float subpixel) {
        if (!(subpixel >= 0.f && subpixel <= 1.f))
            throw new IllegalArgumentException("The subpixel smoothing must be from 0 to 1, got " + subpixel);

        this.subpixel = subpixel;
    }

    public void setEdgeThreshold(final float edgeThreshold) {
        if (!(edgeThreshold >= MINIMAL_EDGE_THRESHOLD && edgeThreshold <= MAXIMAL_EDGE_THRESHOLD))
            throw new IllegalArgumentException(String.format("The edge threshold must be from %s to %s, got %s", MINIMAL_EDGE_THRESHOLD, MAXIMAL_EDGE_THRESHOLD, edgeThreshold));

        this.edgeThreshold = edgeThreshold;
    }
}
