package io.github.trimax.venta.engine.enums;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import one.util.streamex.StreamEx;

import static org.lwjgl.opengl.GL11C.*;
import static org.lwjgl.opengl.GL30C.GL_R8;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum TextureFormat {
    Grayscale(1, GL_R8, GL_RED),
    RGB(3, GL_RGB8, GL_RGB),
    RGBA(4, GL_RGBA8, GL_RGBA);

    private final int channelsCount;
    private final int internal;
    private final int external;

    public static TextureFormat of(final int channelsCount) {
        return StreamEx.of(values())
                .filterBy(TextureFormat::getChannelsCount, channelsCount)
                .findFirst()
                .orElse(null);
    }
}