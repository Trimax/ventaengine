package io.github.trimax.venta.engine.renderers.common;

import static org.lwjgl.opengl.GL11C.*;
import static org.lwjgl.opengl.GL12C.GL_CLAMP_TO_EDGE;
import static org.lwjgl.opengl.GL12C.GL_TEXTURE_BASE_LEVEL;
import static org.lwjgl.opengl.GL12C.GL_TEXTURE_MAX_LEVEL;
import static org.lwjgl.opengl.GL12C.GL_TEXTURE_WRAP_R;
import static org.lwjgl.opengl.GL13C.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13C.GL_TEXTURE_CUBE_MAP;
import static org.lwjgl.opengl.GL13C.GL_TEXTURE_CUBE_MAP_POSITIVE_X;
import static org.lwjgl.opengl.GL13C.glActiveTexture;
import static org.lwjgl.opengl.GL20C.*;
import static org.lwjgl.opengl.GL30C.*;
import static org.lwjgl.opengl.GL32C.GL_TEXTURE_CUBE_MAP_SEAMLESS;

import java.nio.ByteBuffer;
import java.util.function.ToIntFunction;

import io.github.trimax.venta.engine.enums.ProgramType;
import io.github.trimax.venta.engine.exceptions.FramebufferCreationException;
import io.github.trimax.venta.engine.memory.Memory;
import io.github.trimax.venta.engine.model.common.environment.EnvironmentMap;
import lombok.NonNull;

/**
 * Convolves a skybox into the maps of the image based lighting on the GPU, with a full screen triangle drawn into the
 * faces and the levels of cube maps:
 * <ol>
 *   <li>the skybox is copied into a floating point cube map with mip levels, the sky blurred for the later passes;</li>
 *   <li>the irradiance map holds what a surface diffuses from every direction (a small cube map: it is smooth);</li>
 *   <li>the prefiltered map holds what a surface of every roughness reflects, the roughness grows with the mip level;</li>
 *   <li>the lookup table of the BRDF is independent of the sky and is made once.</li>
 * </ol>
 * The state of OpenGL that the passes change (the framebuffer, the viewport, the culling) is restored.
 */
final class EnvironmentGenerator {
    static final int ENVIRONMENT_RESOLUTION = 256;
    static final int IRRADIANCE_RESOLUTION = 32;
    static final int PREFILTER_RESOLUTION = 128;
    static final int PREFILTER_LEVELS = 5;
    static final int BRDF_RESOLUTION = 256;

    static final int IRRADIANCE_SAMPLES = 1024;
    static final int PREFILTER_SAMPLES = 256;
    static final int BRDF_SAMPLES = 256;

    /** The irradiance changes slowly: its samples read the sky from a coarse level (the cube map of 16 texels) */
    private static final float IRRADIANCE_SOURCE_LEVEL = 4.f;

    private static final int FACES = 6;

    private final ToIntFunction<String> programs;
    private final Memory memory;

    /**
     * Creates a generator that draws with the programs of the environment.
     *
     * @param programs gives the identifier of a program by its name, see {@link ProgramType}
     */
    EnvironmentGenerator(@NonNull final Memory memory, @NonNull final ToIntFunction<String> programs) {
        this.memory = memory;
        this.programs = programs;
    }

    /** Makes the irradiance and the prefiltered maps of a cube map; the table of the BRDF is given */
    EnvironmentMap generate(final int sourceID, final int brdfID) {
        try (final var state = new State()) {
            final var environmentID = createCube("Environment", ENVIRONMENT_RESOLUTION, fullLevels(ENVIRONMENT_RESOLUTION), 1, true);
            final var irradianceID = createCube("Irradiance", IRRADIANCE_RESOLUTION, 1, 1, false);
            final var prefilterID = createCube("Prefilter", PREFILTER_RESOLUTION, PREFILTER_LEVELS, PREFILTER_LEVELS, true);

            // The sky is read across the faces: without it the seams of the blurred levels show
            glEnable(GL_TEXTURE_CUBE_MAP_SEAMLESS);

            final var copy = programs.applyAsInt(ProgramType.EnvironmentCopy.getProgramName());
            glUseProgram(copy);
            bindSource(copy, sourceID);
            drawFaces(copy, environmentID, 0, ENVIRONMENT_RESOLUTION);

            glBindTexture(GL_TEXTURE_CUBE_MAP, environmentID);
            glGenerateMipmap(GL_TEXTURE_CUBE_MAP);

            final var irradiance = programs.applyAsInt(ProgramType.EnvironmentIrradiance.getProgramName());
            glUseProgram(irradiance);
            bindSource(irradiance, environmentID);
            glUniform1i(glGetUniformLocation(irradiance, "sampleCount"), IRRADIANCE_SAMPLES);
            glUniform1f(glGetUniformLocation(irradiance, "sourceLevel"), IRRADIANCE_SOURCE_LEVEL);
            drawFaces(irradiance, irradianceID, 0, IRRADIANCE_RESOLUTION);

            final var prefilter = programs.applyAsInt(ProgramType.EnvironmentPrefilter.getProgramName());
            glUseProgram(prefilter);
            bindSource(prefilter, environmentID);
            glUniform1i(glGetUniformLocation(prefilter, "sampleCount"), PREFILTER_SAMPLES);
            glUniform1f(glGetUniformLocation(prefilter, "sourceResolution"), ENVIRONMENT_RESOLUTION);
            for (int level = 0; level < PREFILTER_LEVELS; level++) {
                glUniform1f(glGetUniformLocation(prefilter, "roughness"), level / (float) (PREFILTER_LEVELS - 1));
                drawFaces(prefilter, prefilterID, level, PREFILTER_RESOLUTION >> level);
            }

            memory.getTextures().delete(environmentID);

            return new EnvironmentMap(irradianceID, prefilterID, PREFILTER_LEVELS, brdfID);
        }
    }

    /** Makes the lookup table of the BRDF: the cosine of the viewer angle goes along x, the roughness along y */
    int generateBrdf() {
        try (final var state = new State()) {
            final var textureID = memory.getTextures().create("BRDF lookup table");
            glBindTexture(GL_TEXTURE_2D, textureID);
            glTexImage2D(GL_TEXTURE_2D, 0, GL_RG16F, BRDF_RESOLUTION, BRDF_RESOLUTION, 0, GL_RG, GL_FLOAT, (ByteBuffer) null);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
            glBindTexture(GL_TEXTURE_2D, 0);

            final var program = programs.applyAsInt(ProgramType.EnvironmentBrdf.getProgramName());
            glUseProgram(program);
            glUniform1i(glGetUniformLocation(program, "sampleCount"), BRDF_SAMPLES);

            glBindFramebuffer(GL_FRAMEBUFFER, state.framebufferID);
            glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, textureID, 0);
            check();
            glViewport(0, 0, BRDF_RESOLUTION, BRDF_RESOLUTION);
            glDrawArrays(GL_TRIANGLES, 0, 3);

            return textureID;
        }
    }

    /** Deletes the maps of a sky, the table of the BRDF stays */
    void release(@NonNull final EnvironmentMap map) {
        memory.getTextures().delete(map.irradianceID());
        memory.getTextures().delete(map.prefilterID());
    }

    void releaseBrdf(final int brdfID) {
        memory.getTextures().delete(brdfID);
    }

    private void bindSource(final int program, final int cubeID) {
        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_CUBE_MAP, cubeID);
        glUniform1i(glGetUniformLocation(program, "textureSource"), 0);
    }

    /** Draws the six faces of a level of a cube map, the program takes the face number */
    private void drawFaces(final int program, final int cubeID, final int level, final int size) {
        glViewport(0, 0, size, size);

        final var face = glGetUniformLocation(program, "face");
        for (int i = 0; i < FACES; i++) {
            glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_CUBE_MAP_POSITIVE_X + i, cubeID, level);
            check();
            glUniform1i(face, i);
            glDrawArrays(GL_TRIANGLES, 0, 3);
        }
    }

    /**
     * Makes a cube map of floating point colors.
     *
     * @param levels  the number of the mip levels
     * @param defined how many of them are allocated here, the others are made by {@code glGenerateMipmap} later
     */
    private int createCube(final String name, final int resolution, final int levels, final int defined, final boolean mipmaps) {
        final var id = memory.getTextures().create(name);
        glBindTexture(GL_TEXTURE_CUBE_MAP, id);

        for (int level = 0; level < defined; level++)
            for (int face = 0; face < FACES; face++)
                glTexImage2D(GL_TEXTURE_CUBE_MAP_POSITIVE_X + face, level, GL_RGBA16F, resolution >> level, resolution >> level, 0, GL_RGBA, GL_FLOAT, (ByteBuffer) null);

        glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_MIN_FILTER, mipmaps ? GL_LINEAR_MIPMAP_LINEAR : GL_LINEAR);
        glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_WRAP_R, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_BASE_LEVEL, 0);
        glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_MAX_LEVEL, levels - 1);
        glBindTexture(GL_TEXTURE_CUBE_MAP, 0);

        return id;
    }

    private static int fullLevels(final int resolution) {
        return 1 + (int) (Math.log(resolution) / Math.log(2));
    }

    private static void check() {
        final var status = glCheckFramebufferStatus(GL_FRAMEBUFFER);
        if (status != GL_FRAMEBUFFER_COMPLETE)
            throw new FramebufferCreationException(String.format("The environment framebuffer is incomplete: 0x%X", status));
    }

    /** The objects of a pass and the state of OpenGL it changes; closing restores the state and deletes the objects */
    private final class State implements AutoCloseable {
        private final int[] viewport = new int[4];
        private final int previousFramebuffer;
        private final boolean culling;
        private final boolean depth;
        private final boolean blend;
        private final boolean seamless;

        private final int framebufferID;
        private final int vertexArrayID;

        State() {
            glGetIntegerv(GL_VIEWPORT, viewport);
            previousFramebuffer = glGetInteger(GL_FRAMEBUFFER_BINDING);
            culling = glIsEnabled(GL_CULL_FACE);
            depth = glIsEnabled(GL_DEPTH_TEST);
            blend = glIsEnabled(GL_BLEND);
            seamless = glIsEnabled(GL_TEXTURE_CUBE_MAP_SEAMLESS);

            glDisable(GL_CULL_FACE);
            glDisable(GL_DEPTH_TEST);
            glDisable(GL_BLEND);

            framebufferID = memory.getFramebuffers().create("Environment");
            vertexArrayID = memory.getVertexArrays().create("Environment triangle");
            glBindFramebuffer(GL_FRAMEBUFFER, framebufferID);
            glBindVertexArray(vertexArrayID);
        }

        @Override
        public void close() {
            glBindVertexArray(0);
            glUseProgram(0);
            glActiveTexture(GL_TEXTURE0);
            glBindTexture(GL_TEXTURE_CUBE_MAP, 0);
            glBindFramebuffer(GL_FRAMEBUFFER, previousFramebuffer);
            glViewport(viewport[0], viewport[1], viewport[2], viewport[3]);

            enable(GL_CULL_FACE, culling);
            enable(GL_DEPTH_TEST, depth);
            enable(GL_BLEND, blend);
            // The maps are sampled across the faces for the rest of the run, so seamless stays on
            enable(GL_TEXTURE_CUBE_MAP_SEAMLESS, seamless || glIsEnabled(GL_TEXTURE_CUBE_MAP_SEAMLESS));

            memory.getFramebuffers().delete(framebufferID);
            memory.getVertexArrays().delete(vertexArrayID);
        }

        private static void enable(final int capability, final boolean enabled) {
            if (enabled)
                glEnable(capability);
            else
                glDisable(capability);
        }
    }
}
