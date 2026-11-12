package io.github.trimax.venta.engine.enums;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import one.util.streamex.StreamEx;

import static org.lwjgl.opengl.GL11C.*;
import static org.lwjgl.opengl.GL30C.GL_R16;
import static org.lwjgl.opengl.GL30C.GL_R8;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum TextureFormat {
    Grayscale(1, GL_R8, GL_R16, GL_RED),
    RGB(3, GL_RGB8, GL_RGB16, GL_RGB),
    RGBA(4, GL_RGBA8, GL_RGBA16, GL_RGBA);

    private final int channelsCount;
    private final int internal;

    /** Internal format for images with 16 bits per channel (heightmaps need the precision) */
    private final int internal16;

    private final int external;

    public static TextureFormat of(final int channelsCount) {
        return StreamEx.of(values())
                .filterBy(TextureFormat::getChannelsCount, channelsCount)
                .findFirst()
                .orElse(null);
    }
}