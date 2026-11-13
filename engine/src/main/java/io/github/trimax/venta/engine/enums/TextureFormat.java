package io.github.trimax.venta.engine.enums;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import one.util.streamex.StreamEx;

import static org.lwjgl.opengl.GL11C.*;
import static org.lwjgl.opengl.GL21C.GL_SRGB8;
import static org.lwjgl.opengl.GL21C.GL_SRGB8_ALPHA8;
import static org.lwjgl.opengl.GL30C.GL_R16;
import static org.lwjgl.opengl.GL30C.GL_R8;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum TextureFormat {
    // There is no single channel sRGB format in OpenGL 3.3, grayscale colors stay linear
    Grayscale(1, GL_R8, GL_R16, GL_R8, GL_RED),
    RGB(3, GL_RGB8, GL_RGB16, GL_SRGB8, GL_RGB),
    RGBA(4, GL_RGBA8, GL_RGBA16, GL_SRGB8_ALPHA8, GL_RGBA);

    private final int channelsCount;
    private final int internal;

    /** Internal format for images with 16 bits per channel (heightmaps need the precision) */
    private final int internal16;

    /** Internal format for colors stored in sRGB */
    private final int internalSRGB;

    private final int external;

    /**
     * The internal format of an image: 16-bit images keep their precision (there are no 16-bit sRGB formats, they are
     * heightmaps and other data), 8-bit colors are stored in sRGB, 8-bit data is linear.
     */
    public int getInternal(final boolean is16Bit, final TextureColorSpace colorSpace) {
        if (is16Bit)
            return internal16;

        return colorSpace == TextureColorSpace.Color ? internalSRGB : internal;
    }

    public static TextureFormat of(final int channelsCount) {
        return StreamEx.of(values())
                .filterBy(TextureFormat::getChannelsCount, channelsCount)
                .findFirst()
                .orElse(null);
    }
}