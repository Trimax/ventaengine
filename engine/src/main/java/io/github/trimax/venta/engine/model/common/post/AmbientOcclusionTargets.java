package io.github.trimax.venta.engine.model.common.post;

import static org.lwjgl.opengl.GL11C.*;
import static org.lwjgl.opengl.GL12C.GL_CLAMP_TO_EDGE;
import static org.lwjgl.opengl.GL30C.*;

import java.nio.ByteBuffer;

import io.github.trimax.venta.engine.exceptions.FramebufferCreationException;
import io.github.trimax.venta.engine.memory.Memory;
import lombok.Getter;
import lombok.NonNull;

/**
 * The images of the ambient occlusion: two of half the size of the scene, because the occlusion is soft. The estimate is
 * drawn into the first, the blur goes from one to the other and back along the two axes. The red is the occlusion, the
 * green is the distance of the pixel from the eye.
 */
public final class AmbientOcclusionTargets {
    private static final int IMAGES = 2;

    private final Memory memory;
    private final int[] textureIDs = new int[IMAGES];
    private final int[] framebufferIDs = new int[IMAGES];

    /** The size of the scene the images are made for */
    @Getter
    private final int sceneWidth;

    @Getter
    private final int sceneHeight;

    @Getter
    private final int width;

    @Getter
    private final int height;

    public AmbientOcclusionTargets(@NonNull final Memory memory, final int sceneWidth, final int sceneHeight) {
        if (sceneWidth < 1 || sceneHeight < 1)
            throw new IllegalArgumentException(String.format("The ambient occlusion needs a scene, got %dx%d", sceneWidth, sceneHeight));

        this.memory = memory;
        this.sceneWidth = sceneWidth;
        this.sceneHeight = sceneHeight;
        this.width = LightShaftTargets.getSize(sceneWidth);
        this.height = LightShaftTargets.getSize(sceneHeight);

        for (int image = 0; image < IMAGES; image++) {
            textureIDs[image] = memory.getTextures().create("Ambient occlusion %d", image);
            glBindTexture(GL_TEXTURE_2D, textureIDs[image]);
            glTexImage2D(GL_TEXTURE_2D, 0, GL_RG16F, width, height, 0, GL_RG, GL_FLOAT, (ByteBuffer) null);

            // The blur takes the samples between the texels, the estimate is read texel by texel
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
            glBindTexture(GL_TEXTURE_2D, 0);

            framebufferIDs[image] = memory.getFramebuffers().create("Ambient occlusion %d", image);
            glBindFramebuffer(GL_FRAMEBUFFER, framebufferIDs[image]);
            glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, textureIDs[image], 0);
            final var status = glCheckFramebufferStatus(GL_FRAMEBUFFER);
            glBindFramebuffer(GL_FRAMEBUFFER, 0);

            if (status != GL_FRAMEBUFFER_COMPLETE) {
                release();
                throw new FramebufferCreationException(String.format("The framebuffer of the ambient occlusion is incomplete: 0x%X", status));
            }
        }
    }

    public int getTextureID(final int image) {
        return textureIDs[image];
    }

    public int getFramebufferID(final int image) {
        return framebufferIDs[image];
    }

    /** Deletes the textures and the framebuffers */
    public void release() {
        for (int image = 0; image < IMAGES; image++) {
            if (framebufferIDs[image] != 0) {
                memory.getFramebuffers().delete(framebufferIDs[image]);
                framebufferIDs[image] = 0;
            }

            if (textureIDs[image] != 0) {
                memory.getTextures().delete(textureIDs[image]);
                textureIDs[image] = 0;
            }
        }
    }
}
