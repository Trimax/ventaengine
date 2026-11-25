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
 * The image of the reflection of the water: the scene seen by the mirror image of the camera, in HDR colors. It has half the
 * size of the scene (the reflection is blurred by the ripples anyway), and no multisampling.
 */
public final class ReflectionTarget {
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
    private final int textureID;

    @Getter
    private final int framebufferID;

    private final int depthID;

    /** The size of the image of the reflection for a size of the scene: half of it, rounded up */
    public static int getSize(final int sceneSize) {
        return Math.max((sceneSize + 1) / 2, 1);
    }

    public ReflectionTarget(@NonNull final Memory memory, final int sceneWidth, final int sceneHeight) {
        if (sceneWidth < 1 || sceneHeight < 1)
            throw new IllegalArgumentException(String.format("The reflection needs a scene, got %dx%d", sceneWidth, sceneHeight));

        this.memory = memory;
        this.sceneWidth = sceneWidth;
        this.sceneHeight = sceneHeight;
        this.width = getSize(sceneWidth);
        this.height = getSize(sceneHeight);

        textureID = memory.getTextures().create("Reflection");
        glBindTexture(GL_TEXTURE_2D, textureID);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA16F, width, height, 0, GL_RGBA, GL_FLOAT, (ByteBuffer) null);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
        glBindTexture(GL_TEXTURE_2D, 0);

        depthID = memory.getRenderbuffers().create("Reflection depth");
        glBindRenderbuffer(GL_RENDERBUFFER, depthID);
        glRenderbufferStorage(GL_RENDERBUFFER, GL_DEPTH24_STENCIL8, width, height);
        glBindRenderbuffer(GL_RENDERBUFFER, 0);

        framebufferID = memory.getFramebuffers().create("Reflection");
        glBindFramebuffer(GL_FRAMEBUFFER, framebufferID);
        glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, textureID, 0);
        glFramebufferRenderbuffer(GL_FRAMEBUFFER, GL_DEPTH_STENCIL_ATTACHMENT, GL_RENDERBUFFER, depthID);
        final var status = glCheckFramebufferStatus(GL_FRAMEBUFFER);
        glBindFramebuffer(GL_FRAMEBUFFER, 0);

        if (status != GL_FRAMEBUFFER_COMPLETE) {
            release();
            throw new FramebufferCreationException(String.format("Reflection framebuffer is incomplete: 0x%X", status));
        }
    }

    /** Deletes the texture, the depth and the framebuffer */
    public void release() {
        memory.getFramebuffers().delete(framebufferID);
        memory.getRenderbuffers().delete(depthID);
        memory.getTextures().delete(textureID);
    }
}
