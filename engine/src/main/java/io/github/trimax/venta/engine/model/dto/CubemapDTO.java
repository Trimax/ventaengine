package io.github.trimax.venta.engine.model.dto;

import java.util.Map;

import io.github.trimax.venta.engine.enums.CubemapFace;
import lombok.NonNull;

/**
 * A skybox: six images (8 bit, the faces of a cube) or one panorama of the sky in HDR colors.
 *
 * @param program         the program that draws the sky
 * @param faces           the images of the six faces, of the sky with the colors of a screen (8 bit)
 * @param equirectangular a Radiance (.hdr) panorama of the whole sky, twice as wide as it is high, with the real brightness of the
 *                        sun and the sky (the center of it looks along -z, the right of it goes to +x); it is made a cube map of floats
 * @param resolution      the size of a face of that cube map in pixels, 1024 when it is not set
 * @param clamp           the brightest value of the panorama that is kept, 256 when it is not set: the sun is thousands of times
 *                        brighter than the sky around it, and the light of the sky that is blurred for the reflections would
 *                        sparkle with it
 */
public record CubemapDTO(@NonNull String program,
                         Map<CubemapFace, String> faces,
                         String equirectangular,
                         Integer resolution,
                         Float clamp) {
    public static final int DEFAULT_RESOLUTION = 1024;
    public static final int MINIMAL_RESOLUTION = 16;
    public static final int MAXIMAL_RESOLUTION = 4096;
    public static final float DEFAULT_CLAMP = 256.f;

    /** Whether the sky is a panorama in HDR: it is drawn from one image and not from six */
    public boolean isEquirectangular() {
        return equirectangular != null;
    }

    public String getTexturePath(@NonNull final CubemapFace face) {
        return faces == null ? null : faces.getOrDefault(face, null);
    }

    /** The size of a face of a cube map made of a panorama */
    public int getResolution() {
        final var value = resolution == null ? DEFAULT_RESOLUTION : resolution;
        if (value < MINIMAL_RESOLUTION || value > MAXIMAL_RESOLUTION)
            throw new IllegalArgumentException(String.format("The resolution of a skybox must be from %d to %d, got %d", MINIMAL_RESOLUTION, MAXIMAL_RESOLUTION, value));

        return value;
    }

    /** The brightest value of a panorama that is kept */
    public float getClampValue() {
        final var value = clamp == null ? DEFAULT_CLAMP : clamp;
        if (!(value > 0.f && Float.isFinite(value)))
            throw new IllegalArgumentException("The clamp of a skybox must be positive, got " + value);

        return value;
    }

    /** A skybox has the six faces or a panorama, not both and not none */
    public void check() {
        final var hasFaces = faces != null && !faces.isEmpty();

        if (hasFaces == isEquirectangular())
            throw new IllegalArgumentException(hasFaces
                    ? "A skybox has either faces or an equirectangular panorama, not both"
                    : "A skybox needs faces or an equirectangular panorama");
    }
}
