package io.github.trimax.venta.editor.utils;

import java.awt.image.BufferedImage;

import io.github.trimax.venta.editor.model.common.RawColor;
import lombok.NonNull;
import lombok.experimental.UtilityClass;

@UtilityClass
public final class ColorUtil {
    public RawColor extract(@NonNull final BufferedImage image, final int x, final int y) {
        final var pixel = image.getRGB(x, y);
        final var a = (pixel >> 24) & 0xFF;
        final var r = (pixel >> 16) & 0xFF;
        final var g = (pixel >> 8) & 0xFF;
        final var b = pixel & 0xFF;

        return new RawColor(r, g, b, a);
    }
}
