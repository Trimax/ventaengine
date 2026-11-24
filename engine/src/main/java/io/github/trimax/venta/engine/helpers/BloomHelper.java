package io.github.trimax.venta.engine.helpers;

import static org.lwjgl.opengl.GL11C.*;
import static org.lwjgl.opengl.GL13C.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13C.glActiveTexture;
import static org.lwjgl.opengl.GL14C.glBlendFuncSeparate;
import static org.lwjgl.opengl.GL20C.*;
import static org.lwjgl.opengl.GL30C.*;

import java.util.function.ToIntFunction;

import io.github.trimax.venta.engine.enums.ProgramType;
import io.github.trimax.venta.engine.memory.Memory;
import io.github.trimax.venta.engine.model.common.post.BloomTargets;
import io.github.trimax.venta.engine.model.common.shared.Bloom;
import lombok.NonNull;

/**
 * Makes the glow of the bright parts of an image on the GPU: the image is halved level after level (the first reduction
 * keeps only what is brighter than the threshold) and then the levels are doubled back, each added to the one above it, so
 * the glow of every scale ends up in the first level, which is half the size of the image.
 *
 * <p>The state of OpenGL that the passes change (the framebuffer, the viewport, the blending, the depth test) is restored.
 */
public final class BloomHelper {
    private final ToIntFunction<String> programs;
    private final Memory memory;

    private BloomTargets targets;
    private int vertexArrayID;

    /**
     * Creates a helper that draws with the programs of the bloom.
     *
     * @param programs gives the identifier of a program by its name, see {@link ProgramType}
     */
    public BloomHelper(@NonNull final Memory memory, @NonNull final ToIntFunction<String> programs) {
        this.memory = memory;
        this.programs = programs;
    }

    /**
     * Makes the glow of an image.
     *
     * @param sourceID the texture of the image in linear HDR colors
     * @return the texture of the glow, half the size of the image: it is valid until the next call or until the helper is released
     */
    public int render(final int sourceID, final int width, final int height, @NonNull final Bloom bloom) {
        if (targets == null || targets.getWidth() != width || targets.getHeight() != height) {
            releaseTargets();
            targets = new BloomTargets(memory, width, height);
        }

        final var viewport = new int[4];
        glGetIntegerv(GL_VIEWPORT, viewport);
        final var previousFramebuffer = glGetInteger(GL_FRAMEBUFFER_BINDING);
        final var blend = glIsEnabled(GL_BLEND);
        final var depth = glIsEnabled(GL_DEPTH_TEST);

        try {
            glDisable(GL_DEPTH_TEST);
            glDisable(GL_BLEND);
            glBindVertexArray(getVertexArray());
            glActiveTexture(GL_TEXTURE0);

            reduce(sourceID, width, height, bloom);
            enlarge();
        } finally {
            glBindVertexArray(0);
            glBindTexture(GL_TEXTURE_2D, 0);
            glUseProgram(0);

            glBindFramebuffer(GL_FRAMEBUFFER, previousFramebuffer);
            glViewport(viewport[0], viewport[1], viewport[2], viewport[3]);

            if (blend)
                glEnable(GL_BLEND);
            else
                glDisable(GL_BLEND);

            if (depth)
                glEnable(GL_DEPTH_TEST);
        }

        return targets.getTextureID(0);
    }

    /** Deletes the images of the chain and the vertex array, the next call makes them again */
    public void release() {
        releaseTargets();

        if (vertexArrayID != 0) {
            memory.getVertexArrays().delete(vertexArrayID);
            vertexArrayID = 0;
        }
    }

    /** The image is reduced to the first level, and every level to the next one */
    private void reduce(final int sourceID, final int width, final int height, final Bloom bloom) {
        final var program = programs.applyAsInt(ProgramType.BloomDownsample.getProgramName());
        glUseProgram(program);
        glUniform1i(glGetUniformLocation(program, "textureScene"), 0);
        glUniform1f(glGetUniformLocation(program, "bloomThreshold"), bloom.getThreshold());
        glUniform1f(glGetUniformLocation(program, "bloomKnee"), bloom.getSoftKnee());

        var source = sourceID;
        var sourceWidth = width;
        var sourceHeight = height;
        for (int level = 0; level < targets.getLevels(); level++) {
            glUniform1i(glGetUniformLocation(program, "bloomFirst"), level == 0 ? 1 : 0);
            glUniform2f(glGetUniformLocation(program, "texelSize"), 1.f / sourceWidth, 1.f / sourceHeight);

            draw(source, level);

            source = targets.getTextureID(level);
            sourceWidth = targets.getWidth(level);
            sourceHeight = targets.getHeight(level);
        }
    }

    /** Every level is doubled and added to the one above it, from the smallest up */
    private void enlarge() {
        final var program = programs.applyAsInt(ProgramType.BloomUpsample.getProgramName());
        glUseProgram(program);
        glUniform1i(glGetUniformLocation(program, "textureScene"), 0);

        glEnable(GL_BLEND);
        glBlendFuncSeparate(GL_ONE, GL_ONE, GL_ZERO, GL_ONE);
        for (int level = targets.getLevels() - 1; level > 0; level--) {
            glUniform2f(glGetUniformLocation(program, "texelSize"), 1.f / targets.getWidth(level), 1.f / targets.getHeight(level));

            draw(targets.getTextureID(level), level - 1);
        }

        glDisable(GL_BLEND);
    }

    /** Draws the full screen triangle with a texture into a level */
    private void draw(final int textureID, final int level) {
        glBindFramebuffer(GL_FRAMEBUFFER, targets.getFramebufferID(level));
        glViewport(0, 0, targets.getWidth(level), targets.getHeight(level));
        glBindTexture(GL_TEXTURE_2D, textureID);
        glDrawArrays(GL_TRIANGLES, 0, 3);
    }

    private int getVertexArray() {
        if (vertexArrayID == 0)
            vertexArrayID = memory.getVertexArrays().create("Bloom triangle");

        return vertexArrayID;
    }

    private void releaseTargets() {
        if (targets != null) {
            targets.release();
            targets = null;
        }
    }
}
