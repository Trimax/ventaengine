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
 * The chain of images of the bloom: every level is half the size of the one before and of the image of the scene (the
 * first level is half of it), and has a framebuffer to be drawn into. The smaller the level, the wider the blur it holds.
 */
public final class BloomTargets {
    /** The most levels of a chain: a level is twice as wide in the world as the one before, so six are a very wide glow */
    public static final int MAXIMAL_LEVELS = 6;

    /** The smallest size of a level in texels: a smaller image holds nothing */
    private static final int MINIMAL_SIZE = 4;

    private final Memory memory;

    /** The size of the image of the scene the chain is made for */
    @Getter
    private final int width;

    @Getter
    private final int height;

    @Getter
    private final int levels;

    private final int[] textureIDs;
    private final int[] framebufferIDs;

    /**
     * How many levels the chain of an image has: as many halvings as keep the smallest side of a level above a few texels,
     * but not more than {@link #MAXIMAL_LEVELS} and at least one.
     */
    public static int getLevelCount(final int width, final int height) {
        var smallest = Math.min(width, height);
        var count = 0;
        while (count < MAXIMAL_LEVELS && (smallest >>= 1) >= MINIMAL_SIZE)
            count++;

        return Math.max(count, 1);
    }

    /** The size of a level in texels: half of the image for the level 0, a half of that for every next one, not less than one */
    public static int getLevelSize(final int size, final int level) {
        return Math.max(size >> (level + 1), 1);
    }

    public BloomTargets(@NonNull final Memory memory, final int width, final int height) {
        if (width < 1 || height < 1)
            throw new IllegalArgumentException(String.format("The bloom needs an image, got %dx%d", width, height));

        this.memory = memory;
        this.width = width;
        this.height = height;
        this.levels = getLevelCount(width, height);
        this.textureIDs = new int[levels];
        this.framebufferIDs = new int[levels];

        for (int level = 0; level < levels; level++) {
            textureIDs[level] = memory.getTextures().create("Bloom level %d", level);
            glBindTexture(GL_TEXTURE_2D, textureIDs[level]);
            glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA16F, getWidth(level), getHeight(level), 0, GL_RGBA, GL_FLOAT, (ByteBuffer) null);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
            glBindTexture(GL_TEXTURE_2D, 0);

            framebufferIDs[level] = memory.getFramebuffers().create("Bloom level %d", level);
            glBindFramebuffer(GL_FRAMEBUFFER, framebufferIDs[level]);
            glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, textureIDs[level], 0);
            final var status = glCheckFramebufferStatus(GL_FRAMEBUFFER);
            glBindFramebuffer(GL_FRAMEBUFFER, 0);

            if (status != GL_FRAMEBUFFER_COMPLETE) {
                release();
                throw new FramebufferCreationException(String.format("The framebuffer of the bloom level %d is incomplete: 0x%X", level, status));
            }
        }
    }

    /** The width of a level in texels */
    public int getWidth(final int level) {
        return getLevelSize(width, level);
    }

    /** The height of a level in texels */
    public int getHeight(final int level) {
        return getLevelSize(height, level);
    }

    public int getTextureID(final int level) {
        return textureIDs[level];
    }

    public int getFramebufferID(final int level) {
        return framebufferIDs[level];
    }

    /** Deletes the textures and the framebuffers */
    public void release() {
        for (int level = 0; level < levels; level++) {
            if (framebufferIDs[level] != 0) {
                memory.getFramebuffers().delete(framebufferIDs[level]);
                framebufferIDs[level] = 0;
            }

            if (textureIDs[level] != 0) {
                memory.getTextures().delete(textureIDs[level]);
                textureIDs[level] = 0;
            }
        }
    }
}
