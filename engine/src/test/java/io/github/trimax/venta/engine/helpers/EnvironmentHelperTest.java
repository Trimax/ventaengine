package io.github.trimax.venta.engine.helpers;

import static org.junit.jupiter.api.Assertions.*;
import static org.lwjgl.opengl.GL11C.*;
import static org.lwjgl.opengl.GL12C.GL_CLAMP_TO_EDGE;
import static org.lwjgl.opengl.GL13C.GL_TEXTURE_CUBE_MAP;
import static org.lwjgl.opengl.GL13C.GL_TEXTURE_CUBE_MAP_POSITIVE_X;
import static org.lwjgl.opengl.GL20C.*;
import static org.lwjgl.opengl.GL30C.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import io.github.trimax.venta.engine.memory.TestMemory;
import io.github.trimax.venta.engine.model.common.environment.EnvironmentMap;
import io.github.trimax.venta.engine.shaders.GLTestContext;
import io.github.trimax.venta.engine.shaders.ShaderResources;

/**
 * The convolution of a skybox on the GPU: the maps of the image based lighting made from skies of known colors.
 */
final class EnvironmentHelperTest {
    private static final int SOURCE_SIZE = 4;
    private static final float TOLERANCE = 0.02f;

    private static final Map<String, Integer> PROGRAMS = new HashMap<>();

    private static EnvironmentHelper generator;
    private static int brdf;

    @BeforeAll
    static void context() throws Exception {
        GLTestContext.require();

        // The table of the BRDF doesn't depend on the sky and is the slowest part: it is made once
        generator = new EnvironmentHelper(TestMemory.create(), EnvironmentHelperTest::program);
        brdf = generator.generateBrdf();
    }

    @AfterAll
    static void deleteEverything() {
        generator.releaseBrdf(brdf);

        PROGRAMS.values().forEach(program -> glDeleteProgram(program));
        PROGRAMS.clear();
    }

    @Test
    void testSkyOfOneColorGivesTheSameColorEverywhere() {
        final var sky = cube((face, x, y) -> new float[]{0.25f, 0.5f, 0.75f});
        final var map = generator.generate(sky, brdf);

        for (int face = 0; face < 6; face++) {
            assertColor(new float[]{0.25f, 0.5f, 0.75f}, texel(map.irradianceID(), face, 0, 16, 16), TOLERANCE, "Irradiance, face " + face);
            assertColor(new float[]{0.25f, 0.5f, 0.75f}, texel(map.irradianceID(), face, 0, 2, 29), TOLERANCE, "Irradiance near the edge, face " + face);

            for (int level = 0; level < EnvironmentHelper.PREFILTER_LEVELS; level++) {
                final var size = EnvironmentHelper.PREFILTER_RESOLUTION >> level;
                assertColor(new float[]{0.25f, 0.5f, 0.75f}, texel(map.prefilterID(), face, level, size / 2, size / 2), TOLERANCE,
                        "Prefiltered, face " + face + ", level " + level);
            }
        }

        release(map, sky);
    }

    @Test
    void testFacesAreDrawnWhereTheDirectionsLookThem() {
        // Every texel of the sky has its own color: the red is the face, the green is the column, the blue is the row
        final var sky = cube((face, x, y) -> new float[]{(face + 1) / 7.f, 0.1f + 0.25f * x, 0.1f + 0.25f * y});
        final var map = generator.generate(sky, brdf);

        // The mirror level of the prefiltered map is the sky itself; the middle texels of the sky (columns 1 and 2 of 4) are
        // at the texels 48 and 79 of the 128 of the map
        final var columns = new int[]{1, 2};
        final var positions = new int[]{48, 79};
        for (int face = 0; face < 6; face++)
            for (int i = 0; i < 2; i++)
                for (int j = 0; j < 2; j++) {
                    final var expected = new float[]{(face + 1) / 7.f, 0.1f + 0.25f * columns[i], 0.1f + 0.25f * columns[j]};

                    assertColor(expected, texel(map.prefilterID(), face, 0, positions[i], positions[j]), 0.05f,
                            "Face " + face + ", column " + columns[i] + ", row " + columns[j]);
                }

        release(map, sky);
    }

    @Test
    void testIrradianceFollowsTheBrightSideOfTheSky() {
        // Only the face +Y (the zenith) shines
        final var sky = cube((face, x, y) -> face == 2 ? new float[]{1.f, 1.f, 1.f} : new float[]{0.f, 0.f, 0.f});
        final var map = generator.generate(sky, brdf);

        final var zenith = texel(map.irradianceID(), 2, 0, 16, 16)[0];
        final var nadir = texel(map.irradianceID(), 3, 0, 16, 16)[0];
        final var sides = new float[]{
                texel(map.irradianceID(), 0, 0, 16, 16)[0], texel(map.irradianceID(), 1, 0, 16, 16)[0],
                texel(map.irradianceID(), 4, 0, 16, 16)[0], texel(map.irradianceID(), 5, 0, 16, 16)[0]};

        assertTrue(zenith > 0.4f, "A surface facing the bright face is lit: " + zenith);
        assertTrue(nadir < 0.02f, "A surface facing away from it is dark: " + nadir);
        for (final var side : sides) {
            assertTrue(side > nadir + 0.02f && side < zenith, "A side surface gets part of the light: " + side + " between " + nadir + " and " + zenith);
            assertEquals(sides[0], side, 0.03f, "The four sides are the same by symmetry");
        }

        release(map, sky);
    }

    @Test
    void testLightStaysWhereTheSkyShinesOnEveryFace() {
        final var irradianceSize = EnvironmentHelper.IRRADIANCE_RESOLUTION;
        final var prefilterSize = EnvironmentHelper.PREFILTER_RESOLUTION >> 2;
        final var near = 3;

        // A bright spot in the corner of one face of the sky. What a surface diffuses and reflects is read by the
        // directions of the world, so on that face the convolved light must be nearer to the spot than to the mirrored corners
        for (int face = 0; face < 6; face++) {
            final var shining = face;
            final var sky = cube((f, x, y) -> f == shining && x == 0 && y == 0 ? new float[]{200.f, 200.f, 200.f} : new float[]{0.f, 0.f, 0.f});
            final var map = generator.generate(sky, brdf);

            final var far = irradianceSize - 1 - near;
            final var spot = texel(map.irradianceID(), face, 0, near, near)[0];
            final var mirroredColumn = texel(map.irradianceID(), face, 0, far, near)[0];
            final var mirroredRow = texel(map.irradianceID(), face, 0, near, far)[0];

            assertTrue(spot > mirroredColumn * 1.5f, "Irradiance, face " + face + ": the corner of the spot " + spot + " is brighter than the mirrored column " + mirroredColumn);
            assertTrue(spot > mirroredRow * 1.5f, "Irradiance, face " + face + ": the corner of the spot " + spot + " is brighter than the mirrored row " + mirroredRow);

            final var prefilterFar = prefilterSize - 1 - near;
            final var reflected = texel(map.prefilterID(), face, 2, near, near)[0];
            final var reflectedColumn = texel(map.prefilterID(), face, 2, prefilterFar, near)[0];
            final var reflectedRow = texel(map.prefilterID(), face, 2, near, prefilterFar)[0];

            assertTrue(reflected > reflectedColumn * 1.5f, "Prefiltered, face " + face + ": " + reflected + " is brighter than the mirrored column " + reflectedColumn);
            assertTrue(reflected > reflectedRow * 1.5f, "Prefiltered, face " + face + ": " + reflected + " is brighter than the mirrored row " + reflectedRow);

            release(map, sky);
        }
    }

    @Test
    void testPrefilteredLevelsBlurTheSkyMoreAndMore() {
        final var sky = cube((face, x, y) -> face == 2 && x >= 1 && x <= 2 && y >= 1 && y <= 2 ? new float[]{1.f, 1.f, 1.f} : new float[]{0.f, 0.f, 0.f});
        final var map = generator.generate(sky, brdf);

        var previous = Float.MAX_VALUE;
        for (int level = 0; level < EnvironmentHelper.PREFILTER_LEVELS; level++) {
            final var size = EnvironmentHelper.PREFILTER_RESOLUTION >> level;
            final var center = texel(map.prefilterID(), 2, level, size / 2, size / 2)[0];

            assertTrue(center <= previous + TOLERANCE, "The sun gets dimmer with the roughness: level " + level + " has " + center + " after " + previous);
            previous = center;
        }

        final var sharp = texel(map.prefilterID(), 2, 0, 64, 64)[0];
        final var blurred = texel(map.prefilterID(), 2, EnvironmentHelper.PREFILTER_LEVELS - 1, 4, 4)[0];
        assertTrue(sharp > 0.9f, "A mirror shows the bright spot as it is: " + sharp);
        assertTrue(blurred < sharp - 0.2f, "A rough surface spreads it: " + blurred + " < " + sharp);

        // A mirror looking at the side of the sky sees black; the roughest level gets some light from the spot
        final var sideMirror = texel(map.prefilterID(), 0, 0, 64, 64)[0];
        final var sideRough = texel(map.prefilterID(), 0, EnvironmentHelper.PREFILTER_LEVELS - 1, 4, 4)[0];
        assertTrue(sideMirror < 0.01f, "A mirror sees black on the side: " + sideMirror);
        assertTrue(sideRough > sideMirror + 0.005f, "A rough surface gets the light of the spot from the side: " + sideRough);

        release(map, sky);
    }

    @Test
    void testBrdfTableOfAMirrorSeenStraightOnReflectsAllTheLight() {
        final var size = EnvironmentHelper.BRDF_RESOLUTION;
        final var table = table();

        // x is the cosine of the viewer angle, y is the roughness: the table starts at the smoothest roughness
        final var straight = table[(2 * size + size - 1) * 2];
        assertTrue(straight > 0.9f, "The scale of a smooth surface seen straight on: " + straight);
        assertTrue(table[(2 * size + size - 1) * 2 + 1] < 0.05f, "The bias of a smooth surface seen straight on");
    }

    @Test
    void testBrdfTableNeverReflectsMoreThanAllTheLight() {
        final var table = table();

        for (int i = 0; i < table.length; i += 2) {
            assertTrue(table[i] >= 0.f && table[i + 1] >= 0.f, "No negative light at " + i / 2);
            assertTrue(table[i] + table[i + 1] <= 1.03f, "No more than all the light at " + i / 2 + ": " + (table[i] + table[i + 1]));
        }
    }

    @Test
    void testBrdfTableOfARoughSurfaceReflectsLess() {
        final var size = EnvironmentHelper.BRDF_RESOLUTION;
        final var table = table();

        final var smooth = table[(40 * size + size / 2) * 2] + table[(40 * size + size / 2) * 2 + 1];
        final var rough = table[((size - 3) * size + size / 2) * 2] + table[((size - 3) * size + size / 2) * 2 + 1];

        assertTrue(rough < smooth, "A rough surface reflects less: " + rough + " < " + smooth);
    }

    @Test
    void testStateOfOpenGlIsRestored() {
        final var framebuffer = glGenFramebuffers();
        glBindFramebuffer(GL_FRAMEBUFFER, framebuffer);
        glViewport(3, 4, 50, 60);
        glEnable(GL_DEPTH_TEST);
        glEnable(GL_CULL_FACE);
        glDisable(GL_BLEND);

        final var sky = cube((face, x, y) -> new float[]{0.5f, 0.5f, 0.5f});
        final var map = generator.generate(sky, brdf);

        final var viewport = new int[4];
        glGetIntegerv(GL_VIEWPORT, viewport);
        assertArrayEquals(new int[]{3, 4, 50, 60}, viewport, "The viewport");
        assertEquals(framebuffer, glGetInteger(GL_FRAMEBUFFER_BINDING), "The framebuffer");
        assertTrue(glIsEnabled(GL_DEPTH_TEST), "The depth test");
        assertTrue(glIsEnabled(GL_CULL_FACE), "The culling");
        assertFalse(glIsEnabled(GL_BLEND), "The blending");

        glBindFramebuffer(GL_FRAMEBUFFER, 0);
        glDeleteFramebuffers(framebuffer);
        glDisable(GL_CULL_FACE);
        release(map, sky);
    }

    @Test
    void testReleaseDeletesTheMapsButNotTheTable() {
        final var sky = cube((face, x, y) -> new float[]{0.5f, 0.5f, 0.5f});
        final var map = generator.generate(sky, brdf);

        assertTrue(glIsTexture(map.irradianceID()) && glIsTexture(map.prefilterID()), "The maps are made");

        generator.release(map);

        assertFalse(glIsTexture(map.irradianceID()), "The irradiance map is deleted");
        assertFalse(glIsTexture(map.prefilterID()), "The prefiltered map is deleted");
        assertTrue(glIsTexture(brdf), "The table doesn't depend on the sky");

        final var own = generator.generateBrdf();
        assertTrue(glIsTexture(own), "A table of its own");
        generator.releaseBrdf(own);
        assertFalse(glIsTexture(own), "The table is deleted on its own");
        glDeleteTextures(sky);
    }

    private static void release(final EnvironmentMap map, final int sky) {
        generator.release(map);
        glDeleteTextures(sky);
    }

    /** The scale and the bias of the table, row by row */
    private static float[] table() {
        final var size = EnvironmentHelper.BRDF_RESOLUTION;
        final var table = new float[size * size * 2];

        glBindTexture(GL_TEXTURE_2D, brdf);
        glGetTexImage(GL_TEXTURE_2D, 0, GL_RG, GL_FLOAT, table);
        glBindTexture(GL_TEXTURE_2D, 0);

        return table;
    }

    /** The color of a texel of a level of a face of a cube map */
    private static float[] texel(final int cubeID, final int face, final int level, final int x, final int y) {
        final var size = sizeOf(cubeID, level);
        final var pixels = new float[size * size * 4];

        glBindTexture(GL_TEXTURE_CUBE_MAP, cubeID);
        glGetTexImage(GL_TEXTURE_CUBE_MAP_POSITIVE_X + face, level, GL_RGBA, GL_FLOAT, pixels);
        glBindTexture(GL_TEXTURE_CUBE_MAP, 0);

        final var index = (y * size + x) * 4;
        return new float[]{pixels[index], pixels[index + 1], pixels[index + 2]};
    }

    private static int sizeOf(final int cubeID, final int level) {
        glBindTexture(GL_TEXTURE_CUBE_MAP, cubeID);
        final var size = glGetTexLevelParameteri(GL_TEXTURE_CUBE_MAP_POSITIVE_X, level, GL_TEXTURE_WIDTH);
        glBindTexture(GL_TEXTURE_CUBE_MAP, 0);

        return size;
    }

    private static void assertColor(final float[] expected, final float[] actual, final float tolerance, final String message) {
        for (int i = 0; i < 3; i++)
            assertEquals(expected[i], actual[i], tolerance, message + ", channel " + i);
    }

    /** Gives the color of a texel of a face of the sky */
    @FunctionalInterface
    private interface Sky {
        float[] color(int face, int x, int y);
    }

    private static int cube(final Sky sky) {
        final var id = glGenTextures();
        glBindTexture(GL_TEXTURE_CUBE_MAP, id);
        for (int face = 0; face < 6; face++) {
            final var pixels = new float[SOURCE_SIZE * SOURCE_SIZE * 4];
            for (int y = 0; y < SOURCE_SIZE; y++)
                for (int x = 0; x < SOURCE_SIZE; x++) {
                    final var color = sky.color(face, x, y);
                    final var index = (y * SOURCE_SIZE + x) * 4;

                    pixels[index] = color[0];
                    pixels[index + 1] = color[1];
                    pixels[index + 2] = color[2];
                    pixels[index + 3] = 1.f;
                }

            glTexImage2D(GL_TEXTURE_CUBE_MAP_POSITIVE_X + face, 0, GL_RGBA32F, SOURCE_SIZE, SOURCE_SIZE, 0, GL_RGBA, GL_FLOAT, pixels);
        }

        glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
        glBindTexture(GL_TEXTURE_CUBE_MAP, 0);

        return id;
    }

    /** The program of the engine by the name of its definition file, compiled from the resources */
    private static synchronized int program(final String name) {
        return PROGRAMS.computeIfAbsent(name, key -> {
            try {
                final var definition = ShaderResources.programs().get(key);
                assertNotNull(definition, "The program " + key);

                final var program = glCreateProgram();
                for (final var shader : ShaderResources.shaders(definition)) {
                    final var source = ShaderResources.expand(shader);
                    final var id = glCreateShader(shader.contains("vertex") ? GL_VERTEX_SHADER : GL_FRAGMENT_SHADER);
                    glShaderSource(id, source.code());
                    glCompileShader(id);
                    assertEquals(GL_TRUE, glGetShaderi(id, GL_COMPILE_STATUS), shader + ": " + source.mapLog(glGetShaderInfoLog(id)));
                    glAttachShader(program, id);
                }

                glLinkProgram(program);
                assertEquals(GL_TRUE, glGetProgrami(program, GL_LINK_STATUS), glGetProgramInfoLog(program));

                return program;
            } catch (final Exception exception) {
                throw new IllegalStateException(exception);
            }
        });
    }
}
