package io.github.trimax.venta.engine.renderers.common;

import static org.lwjgl.opengl.GL11C.*;
import static org.lwjgl.opengl.GL12C.glTexImage3D;
import static org.lwjgl.opengl.GL13C.GL_CLAMP_TO_BORDER;
import static org.lwjgl.opengl.GL14C.GL_TEXTURE_COMPARE_FUNC;
import static org.lwjgl.opengl.GL14C.GL_TEXTURE_COMPARE_MODE;
import static org.lwjgl.opengl.GL30C.*;

import java.nio.ByteBuffer;

import io.github.trimax.venta.engine.exceptions.FramebufferCreationException;
import io.github.trimax.venta.engine.memory.Memory;
import lombok.Getter;
import lombok.NonNull;

/**
 * The depth of the scene seen by the sun: a layer of a texture array for every cascade and a framebuffer to draw them.
 *
 * <p>The texture compares the depth of a point with the stored one when it is sampled (as a shadow sampler) and the
 * hardware filters the comparisons: a texel at the edge of a shadow is partly lit. Outside of the layer everything is lit.
 */
final class ShadowMap {
    private static final float[] LIT = {1.f, 1.f, 1.f, 1.f};

    private final Memory memory;

    @Getter
    private final int resolution;

    @Getter
    private final int layers;

    @Getter
    private final int textureID;

    private final int framebufferID;

    ShadowMap(@NonNull final Memory memory, final int resolution, final int layers) {
        if (resolution < 1 || layers < 1)
            throw new IllegalArgumentException(String.format("A shadow map needs texels and layers, got %d texels and %d layers", resolution, layers));

        this.memory = memory;
        this.resolution = resolution;
        this.layers = layers;

        textureID = memory.getTextureArrays().create("Shadow map");
        glBindTexture(GL_TEXTURE_2D_ARRAY, textureID);
        glTexImage3D(GL_TEXTURE_2D_ARRAY, 0, GL_DEPTH_COMPONENT32F, resolution, resolution, layers, 0, GL_DEPTH_COMPONENT, GL_FLOAT, (ByteBuffer) null);
        glTexParameteri(GL_TEXTURE_2D_ARRAY, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D_ARRAY, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D_ARRAY, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_BORDER);
        glTexParameteri(GL_TEXTURE_2D_ARRAY, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_BORDER);
        glTexParameterfv(GL_TEXTURE_2D_ARRAY, GL_TEXTURE_BORDER_COLOR, LIT);
        glTexParameteri(GL_TEXTURE_2D_ARRAY, GL_TEXTURE_COMPARE_MODE, GL_COMPARE_REF_TO_TEXTURE);
        glTexParameteri(GL_TEXTURE_2D_ARRAY, GL_TEXTURE_COMPARE_FUNC, GL_LEQUAL);
        glBindTexture(GL_TEXTURE_2D_ARRAY, 0);

        framebufferID = memory.getFramebuffers().create("Shadow map");
        glBindFramebuffer(GL_FRAMEBUFFER, framebufferID);
        glFramebufferTextureLayer(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, textureID, 0, 0);
        glDrawBuffer(GL_NONE);
        glReadBuffer(GL_NONE);
        final var status = glCheckFramebufferStatus(GL_FRAMEBUFFER);
        glBindFramebuffer(GL_FRAMEBUFFER, 0);

        if (status != GL_FRAMEBUFFER_COMPLETE) {
            release();
            throw new FramebufferCreationException(String.format("The shadow map framebuffer is incomplete: 0x%X", status));
        }
    }

    /** Draws into the layer: binds the framebuffer with it and sets the viewport of the map */
    void bindLayer(final int layer) {
        if (layer < 0 || layer >= layers)
            throw new IllegalArgumentException(String.format("The shadow map has %d layers, got %d", layers, layer));

        glBindFramebuffer(GL_FRAMEBUFFER, framebufferID);
        glFramebufferTextureLayer(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, textureID, 0, layer);
        glViewport(0, 0, resolution, resolution);
    }

    void release() {
        memory.getFramebuffers().delete(framebufferID);
        memory.getTextureArrays().delete(textureID);
    }
}
