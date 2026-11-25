package io.github.trimax.venta.engine.helpers;

import static org.lwjgl.opengl.GL11C.*;
import static org.lwjgl.opengl.GL13C.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13C.glActiveTexture;
import static org.lwjgl.opengl.GL20C.*;
import static org.lwjgl.opengl.GL30C.*;

import java.util.function.ToIntFunction;

import io.github.trimax.venta.engine.enums.ProgramType;
import io.github.trimax.venta.engine.memory.Memory;
import io.github.trimax.venta.engine.model.common.shared.Antialiasing;
import lombok.NonNull;

/**
 * Smooths the edges of a finished image on the GPU (FXAA): a full screen pass reads the tone mapped image and draws the
 * smoothed one into the framebuffer that is bound. The state of OpenGL that the pass changes is restored.
 */
public final class FxaaHelper {
    private final ToIntFunction<String> programs;
    private final Memory memory;

    private int vertexArrayID;

    /**
     * Creates a helper that draws with the program of FXAA.
     *
     * @param programs gives the identifier of a program by its name, see {@link ProgramType}
     */
    public FxaaHelper(@NonNull final Memory memory, @NonNull final ToIntFunction<String> programs) {
        this.memory = memory;
        this.programs = programs;
    }

    /**
     * Draws the smoothed image into the bound framebuffer, over all of its viewport.
     *
     * @param sourceID the texture of the image in sRGB colors, filtered linearly
     */
    public void render(final int sourceID, final int width, final int height, @NonNull final Antialiasing antialiasing) {
        final var blend = glIsEnabled(GL_BLEND);
        final var depth = glIsEnabled(GL_DEPTH_TEST);

        try {
            glDisable(GL_DEPTH_TEST);
            glDisable(GL_BLEND);

            final var program = programs.applyAsInt(ProgramType.Fxaa.getProgramName());
            glUseProgram(program);
            glUniform1i(glGetUniformLocation(program, "textureScene"), 0);
            glUniform2f(glGetUniformLocation(program, "texelSize"), 1.f / width, 1.f / height);
            glUniform1f(glGetUniformLocation(program, "fxaaSubpixel"), antialiasing.getSubpixel());
            glUniform1f(glGetUniformLocation(program, "fxaaEdgeThreshold"), antialiasing.getEdgeThreshold());
            glUniform1i(glGetUniformLocation(program, "fxaaSteps"), antialiasing.getQuality().getSteps());

            glActiveTexture(GL_TEXTURE0);
            glBindTexture(GL_TEXTURE_2D, sourceID);
            glBindVertexArray(getVertexArray());
            glDrawArrays(GL_TRIANGLES, 0, 3);
        } finally {
            glBindVertexArray(0);
            glBindTexture(GL_TEXTURE_2D, 0);
            glUseProgram(0);

            if (blend)
                glEnable(GL_BLEND);

            if (depth)
                glEnable(GL_DEPTH_TEST);
        }
    }

    /** Deletes the vertex array, the next call makes it again */
    public void release() {
        if (vertexArrayID != 0) {
            memory.getVertexArrays().delete(vertexArrayID);
            vertexArrayID = 0;
        }
    }

    private int getVertexArray() {
        if (vertexArrayID == 0)
            vertexArrayID = memory.getVertexArrays().create("FXAA triangle");

        return vertexArrayID;
    }
}
