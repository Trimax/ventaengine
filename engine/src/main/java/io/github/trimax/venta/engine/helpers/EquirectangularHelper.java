package io.github.trimax.venta.engine.helpers;

import static org.lwjgl.opengl.GL11C.*;
import static org.lwjgl.opengl.GL12C.GL_CLAMP_TO_EDGE;
import static org.lwjgl.opengl.GL12C.GL_TEXTURE_WRAP_R;
import static org.lwjgl.opengl.GL13C.*;
import static org.lwjgl.opengl.GL20C.*;
import static org.lwjgl.opengl.GL30C.*;
import static org.lwjgl.system.MemoryStack.stackPush;

import java.nio.ByteBuffer;
import java.util.function.ToIntFunction;

import org.lwjgl.stb.STBImage;
import org.lwjgl.system.MemoryUtil;

import io.github.trimax.venta.engine.enums.ProgramType;
import io.github.trimax.venta.engine.exceptions.CubemapBakeException;
import io.github.trimax.venta.engine.exceptions.FramebufferCreationException;
import io.github.trimax.venta.engine.memory.Memory;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * Makes a cube map of floating point colors from a panorama of the sky in HDR (a Radiance file): the panorama is decoded with the
 * real brightness of its pixels, put in a texture, and every face of the cube is drawn from it. The state of OpenGL that it changes
 * is restored.
 */
@RequiredArgsConstructor
public final class EquirectangularHelper {
    private static final int FACES = 6;

    @NonNull
    private final Memory memory;

    @NonNull
    private final ToIntFunction<String> programs;

    /**
     * Makes a cube map of a panorama.
     *
     * @param panorama   the content of a Radiance (.hdr) file
     * @param name       what the panorama is called, for the messages
     * @param resolution the size of a face of the cube map in pixels
     * @param clamp      the brightest value that is kept
     * @return the cube map (RGBA16F, linear filtering, no mip levels)
     */
    public int convert(final byte @NonNull [] panorama, @NonNull final String name, final int resolution, final float clamp) {
        final var panoramaID = createPanorama(panorama, name);

        final var viewport = new int[4];
        glGetIntegerv(GL_VIEWPORT, viewport);
        final var previousFramebuffer = glGetInteger(GL_FRAMEBUFFER_BINDING);
        final var culling = glIsEnabled(GL_CULL_FACE);
        final var depth = glIsEnabled(GL_DEPTH_TEST);
        final var blend = glIsEnabled(GL_BLEND);

        final var cubeID = memory.getTextures().create("Sky %s", name);
        final var framebufferID = memory.getFramebuffers().create("Sky %s", name);
        final var vertexArrayID = memory.getVertexArrays().create("Sky %s triangle", name);

        try {
            glBindTexture(GL_TEXTURE_CUBE_MAP, cubeID);
            for (int face = 0; face < FACES; face++)
                glTexImage2D(GL_TEXTURE_CUBE_MAP_POSITIVE_X + face, 0, GL_RGBA16F, resolution, resolution, 0, GL_RGBA, GL_FLOAT, (ByteBuffer) null);

            glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
            glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
            glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
            glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
            glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_WRAP_R, GL_CLAMP_TO_EDGE);
            glBindTexture(GL_TEXTURE_CUBE_MAP, 0);

            glDisable(GL_CULL_FACE);
            glDisable(GL_DEPTH_TEST);
            glDisable(GL_BLEND);

            final var program = programs.applyAsInt(ProgramType.EquirectangularToCube.getProgramName());
            glUseProgram(program);
            glActiveTexture(GL_TEXTURE0);
            glBindTexture(GL_TEXTURE_2D, panoramaID);
            glUniform1i(glGetUniformLocation(program, "texturePanorama"), 0);
            glUniform1f(glGetUniformLocation(program, "clampValue"), clamp);

            glBindFramebuffer(GL_FRAMEBUFFER, framebufferID);
            glBindVertexArray(vertexArrayID);
            glViewport(0, 0, resolution, resolution);

            final var faceUniform = glGetUniformLocation(program, "face");
            for (int face = 0; face < FACES; face++) {
                glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_CUBE_MAP_POSITIVE_X + face, cubeID, 0);

                final var status = glCheckFramebufferStatus(GL_FRAMEBUFFER);
                if (status != GL_FRAMEBUFFER_COMPLETE)
                    throw new FramebufferCreationException(String.format("The framebuffer of the sky %s is incomplete: 0x%X", name, status));

                glUniform1i(faceUniform, face);
                glDrawArrays(GL_TRIANGLES, 0, 3);
            }

            return cubeID;
        } catch (final RuntimeException exception) {
            memory.getTextures().delete(cubeID);
            throw exception;
        } finally {
            glBindVertexArray(0);
            glUseProgram(0);
            glBindTexture(GL_TEXTURE_2D, 0);
            glBindFramebuffer(GL_FRAMEBUFFER, previousFramebuffer);
            glViewport(viewport[0], viewport[1], viewport[2], viewport[3]);

            if (culling)
                glEnable(GL_CULL_FACE);

            if (depth)
                glEnable(GL_DEPTH_TEST);

            if (blend)
                glEnable(GL_BLEND);

            memory.getFramebuffers().delete(framebufferID);
            memory.getVertexArrays().delete(vertexArrayID);
            memory.getTextures().delete(panoramaID);
        }
    }

    /** The texture of the panorama, in the colors it was written in (16-bit floats are enough for the sky, and the clamp keeps it in range) */
    private int createPanorama(final byte[] data, final String name) {
        final var encoded = MemoryUtil.memAlloc(data.length);
        try (final var stack = stackPush()) {
            encoded.put(data).flip();

            final var width = stack.mallocInt(1);
            final var height = stack.mallocInt(1);
            final var channels = stack.mallocInt(1);

            // Always three channels: the color of the sky has no alpha
            final var pixels = STBImage.stbi_loadf_from_memory(encoded, width, height, channels, 3);
            if (pixels == null)
                throw new CubemapBakeException(String.format("The panorama %s can't be decoded as a Radiance (.hdr) file: %s", name, STBImage.stbi_failure_reason()));

            try {
                final var textureID = memory.getTextures().create("Panorama %s", name);
                glBindTexture(GL_TEXTURE_2D, textureID);
                glPixelStorei(GL_UNPACK_ALIGNMENT, 1);
                glTexImage2D(GL_TEXTURE_2D, 0, GL_RGB16F, width.get(0), height.get(0), 0, GL_RGB, GL_FLOAT, pixels);
                glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
                glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);

                // The sky goes round: the left edge of the picture is next to the right one, and the poles are not
                glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
                glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
                glBindTexture(GL_TEXTURE_2D, 0);

                return textureID;
            } finally {
                STBImage.stbi_image_free(pixels);
            }
        } finally {
            MemoryUtil.memFree(encoded);
        }
    }
}
