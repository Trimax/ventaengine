package io.github.trimax.venta.engine.helpers;

import static org.lwjgl.opengl.GL11C.*;
import static org.lwjgl.opengl.GL20C.glUseProgram;
import static org.lwjgl.opengl.GL30C.GL_FRAMEBUFFER;
import static org.lwjgl.opengl.GL30C.GL_FRAMEBUFFER_BINDING;
import static org.lwjgl.opengl.GL30C.glBindFramebuffer;
import static org.lwjgl.opengl.GL30C.glBindVertexArray;

import io.github.trimax.venta.engine.memory.Memory;
import lombok.NonNull;

/**
 * What the passes over the whole screen have in common: a triangle that covers the screen and needs no vertex buffer, and
 * the state of OpenGL that the passes change. A pass is begun, draws as many triangles into as many framebuffers as it needs,
 * and is closed: the framebuffer, the viewport, the blending and the depth test are the ones they were.
 *
 * <p>One instance is used by one renderer: it keeps the state of the pass that is open.
 */
public final class ScreenPassHelper {
    private final Memory memory;
    private final int[] viewport = new int[4];
    private final Pass pass = new Pass();

    private int vertexArrayID;
    private int previousFramebuffer;
    private boolean blend;
    private boolean depth;

    public ScreenPassHelper(@NonNull final Memory memory) {
        this.memory = memory;
    }

    /** Begins a pass: the state is saved, the blending and the depth test are off and the triangle is bound; close the result to end it */
    public ScreenPass begin() {
        glGetIntegerv(GL_VIEWPORT, viewport);
        previousFramebuffer = glGetInteger(GL_FRAMEBUFFER_BINDING);
        blend = glIsEnabled(GL_BLEND);
        depth = glIsEnabled(GL_DEPTH_TEST);

        glDisable(GL_DEPTH_TEST);
        glDisable(GL_BLEND);
        glBindVertexArray(getVertexArray());

        return pass;
    }

    /** Draws the triangle into a framebuffer, the viewport is the size of its image */
    public void draw(final int framebufferID, final int width, final int height) {
        glBindFramebuffer(GL_FRAMEBUFFER, framebufferID);
        glViewport(0, 0, width, height);
        glDrawArrays(GL_TRIANGLES, 0, 3);
    }

    /** Deletes the vertex array, the next pass makes it again */
    public void release() {
        if (vertexArrayID != 0) {
            memory.getVertexArrays().delete(vertexArrayID);
            vertexArrayID = 0;
        }
    }

    private int getVertexArray() {
        if (vertexArrayID == 0)
            vertexArrayID = memory.getVertexArrays().create("Screen triangle");

        return vertexArrayID;
    }

    /** A pass that is open, closing it restores the state of OpenGL that was saved when it began */
    public interface ScreenPass extends AutoCloseable {
        @Override
        void close();
    }

    /** The end of the pass: the state that was saved is restored */
    private final class Pass implements ScreenPass {
        @Override
        public void close() {
            glBindVertexArray(0);
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
    }
}
