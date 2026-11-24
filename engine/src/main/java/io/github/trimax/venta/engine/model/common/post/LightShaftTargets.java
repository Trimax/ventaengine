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
 * The image of the light shafts: it has half the size of the scene, because the march along the view rays is the costly
 * part and the shafts are soft. The colors are the light, the alpha is the length of the ray the light was gathered along.
 */
public final class LightShaftTargets {
    private final Memory memory;

    /** The size of the scene the image is made for */
    @Getter
    private final int sceneWidth;

    @Getter
    private final int sceneHeight;

    @Getter
    private final int width;

    @Getter
    private final int height;

    @Getter
    private int textureID;

    @Getter
    private int framebufferID;

    /** The size of the image of the shafts for a size of the scene: half of it, rounded up, so every pixel of the scene has a neighbor */
    public static int getSize(final int sceneSize) {
        return Math.max((sceneSize + 1) / 2, 1);
    }

    public LightShaftTargets(@NonNull final Memory memory, final int sceneWidth, final int sceneHeight) {
        if (sceneWidth < 1 || sceneHeight < 1)
            throw new IllegalArgumentException(String.format("The light shafts need a scene, got %dx%d", sceneWidth, sceneHeight));

        this.memory = memory;
        this.sceneWidth = sceneWidth;
        this.sceneHeight = sceneHeight;
        this.width = getSize(sceneWidth);
        this.height = getSize(sceneHeight);

        textureID = memory.getTextures().create("Light shafts");
        glBindTexture(GL_TEXTURE_2D, textureID);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA16F, width, height, 0, GL_RGBA, GL_FLOAT, (ByteBuffer) null);

        // The samples are read one by one (texelFetch) and weighted by the shader, so the filter is not used
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
        glBindTexture(GL_TEXTURE_2D, 0);

        framebufferID = memory.getFramebuffers().create("Light shafts");
        glBindFramebuffer(GL_FRAMEBUFFER, framebufferID);
        glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, textureID, 0);
        final var status = glCheckFramebufferStatus(GL_FRAMEBUFFER);
        glBindFramebuffer(GL_FRAMEBUFFER, 0);

        if (status != GL_FRAMEBUFFER_COMPLETE) {
            release();
            throw new FramebufferCreationException(String.format("The framebuffer of the light shafts is incomplete: 0x%X", status));
        }
    }

    /** Deletes the texture and the framebuffer */
    public void release() {
        if (framebufferID != 0) {
            memory.getFramebuffers().delete(framebufferID);
            framebufferID = 0;
        }

        if (textureID != 0) {
            memory.getTextures().delete(textureID);
            textureID = 0;
        }
    }
}
