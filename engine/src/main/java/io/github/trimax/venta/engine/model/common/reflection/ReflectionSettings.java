package io.github.trimax.venta.engine.model.common.reflection;

import io.github.trimax.venta.engine.model.dto.MaterialReflectionsDTO;
import lombok.Getter;
import lombok.NonNull;

/**
 * What a material says about the dynamic reflections of its objects (see {@link MaterialReflectionsDTO}), checked.
 */
@Getter
public final class ReflectionSettings {
    public static final int DEFAULT_RESOLUTION = 128;
    public static final float DEFAULT_REFRESH = 0.25f;
    public static final float DEFAULT_RANGE = 40.f;

    public static final int MINIMAL_RESOLUTION = 16;
    public static final int MAXIMAL_RESOLUTION = 1024;

    private final int resolution;
    private final float refresh;
    private final float range;

    public ReflectionSettings(@NonNull final MaterialReflectionsDTO dto) {
        this(dto.resolution() == null ? DEFAULT_RESOLUTION : dto.resolution(),
                dto.refresh() == null ? DEFAULT_REFRESH : dto.refresh(),
                dto.range() == null ? DEFAULT_RANGE : dto.range());
    }

    public ReflectionSettings(final int resolution, final float refresh, final float range) {
        if (resolution < MINIMAL_RESOLUTION || resolution > MAXIMAL_RESOLUTION)
            throw new IllegalArgumentException(String.format("The resolution of the reflections must be from %d to %d, got %d", MINIMAL_RESOLUTION, MAXIMAL_RESOLUTION, resolution));

        if (!Float.isFinite(refresh) || refresh < 0.f)
            throw new IllegalArgumentException("The refresh of the reflections must not be negative, got " + refresh);

        if (!Float.isFinite(range) || range <= 0.f)
            throw new IllegalArgumentException("The range of the reflections must be positive, got " + range);

        this.resolution = resolution;
        this.refresh = refresh;
        this.range = range;
    }

    /** Whether the cube map is drawn once: it is never refreshed */
    public boolean isStatic() {
        return refresh == 0.f;
    }
}
