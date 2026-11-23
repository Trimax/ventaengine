package io.github.trimax.venta.engine.shaders;

import static org.junit.jupiter.api.Assertions.*;
import static org.lwjgl.opengl.GL11C.*;
import static org.lwjgl.opengl.GL12C.GL_TEXTURE_MAX_LEVEL;
import static org.lwjgl.opengl.GL13C.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13C.GL_TEXTURE_CUBE_MAP;
import static org.lwjgl.opengl.GL13C.GL_TEXTURE_CUBE_MAP_POSITIVE_X;
import static org.lwjgl.opengl.GL13C.glActiveTexture;
import static org.lwjgl.opengl.GL30C.GL_RG;
import static org.lwjgl.opengl.GL30C.GL_RG32F;
import static org.lwjgl.opengl.GL30C.GL_RGBA32F;

import java.nio.FloatBuffer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.lwjgl.system.MemoryStack;

import io.github.trimax.venta.engine.enums.TextureUnit;

/**
 * The ambient light of a surface (common/environment.glsl): the light of the environment or the flat ambient light of
 * the scene, diffused and reflected, on the GPU. The maps of the environment are made of one color each.
 */
final class EnvironmentLightShaderTest {
    private static final float EPSILON = 1e-4f;

    private static final String DECLARATIONS = """
            uniform vec3 albedo;
            uniform float metalness;
            uniform float roughness;
            uniform float occlusion;
            """;

    private static final String CODE = """
            vec3 N = vec3(0.0, 0.0, 1.0);
            vec3 V = vec3(0.0, 0.0, 1.0);
            outputColor = vec4(getAmbientLight(N, V, albedo, metalness, roughness, occlusion), 1.0);
            """;

    private static ShaderHarness harness;

    private int skybox;
    private int irradiance;
    private int prefilter;
    private int brdf;

    @BeforeAll
    static void context() {
        GLTestContext.require();
        harness = new ShaderHarness();
    }

    @BeforeEach
    void createMaps() {
        skybox = cube(0.9f, 0.1f, 0.5f);
        irradiance = cube(0.2f, 0.4f, 0.6f);
        prefilter = cube(1.f, 0.5f, 0.25f);
        brdf = table(1.f, 0.f);
    }

    @AfterEach
    void deleteMaps() {
        for (final var unit : new TextureUnit[]{TextureUnit.Irradiance, TextureUnit.Prefilter, TextureUnit.Brdf}) {
            glActiveTexture(unit.getLocationID());
            glBindTexture(GL_TEXTURE_CUBE_MAP, 0);
            glBindTexture(GL_TEXTURE_2D, 0);
        }

        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_CUBE_MAP, 0);
        glDeleteTextures(skybox);
        glDeleteTextures(irradiance);
        glDeleteTextures(prefilter);
        glDeleteTextures(brdf);
    }

    @Test
    void testWithoutEnvironmentTheAmbientLightIsAFlatColor() {
        // A non-metal facing the viewer: the diffuse part is (1 - F0) of the albedo, the reflection is F0 of the ambient light
        final var result = ambient(false, new float[]{0.5f, 0.5f, 0.5f}, new float[]{1.f, 1.f, 1.f}, 0.f, 1.f, 1.f);

        final var approximation = approximation(1.f, 1.f);
        final var expected = 0.5f * ((1.f - 0.04f) + 0.04f * approximation[0] + approximation[1]);
        assertEquals(expected, result[0], 2e-3f, "A white non-metal under the ambient light 0.5");
        assertEquals(result[0], result[1], EPSILON, "The ambient light is gray");
    }

    @Test
    void testAmbientLightMultipliesTheEnvironment() {
        final var one = ambient(true, new float[]{1.f, 1.f, 1.f}, new float[]{1.f, 1.f, 1.f}, 0.f, 1.f, 1.f);
        final var half = ambient(true, new float[]{0.5f, 0.5f, 0.5f}, new float[]{1.f, 1.f, 1.f}, 0.f, 1.f, 1.f);

        for (int i = 0; i < 3; i++)
            assertEquals(one[i] * 0.5f, half[i], EPSILON, "Half the ambient light gives half the light of the sky, channel " + i);
    }

    @Test
    void testEnvironmentIsDiffusedAndReflectedByTheLookupTable() {
        // The table here is (1, 0): the reflection is F0 of the prefiltered map. Straight on the fresnel is F0 = 0.04
        final var result = ambient(true, new float[]{1.f, 1.f, 1.f}, new float[]{1.f, 1.f, 1.f}, 0.f, 1.f, 1.f);

        final var irradianceColor = new float[]{0.2f, 0.4f, 0.6f};
        final var prefilterColor = new float[]{1.f, 0.5f, 0.25f};
        for (int i = 0; i < 3; i++)
            assertEquals((1.f - 0.04f) * irradianceColor[i] + 0.04f * prefilterColor[i], result[i], EPSILON, "channel " + i);
    }

    @Test
    void testMetalReflectsTheSkyInItsOwnColorAndDoesNotDiffuse() {
        final var result = ambient(true, new float[]{1.f, 1.f, 1.f}, new float[]{1.f, 0.f, 0.5f}, 1.f, 1.f, 1.f);

        assertEquals(1.f, result[0], EPSILON, "The red metal reflects all the red of the sky");
        assertEquals(0.f, result[1], EPSILON, "and none of the green");
        assertEquals(0.25f * 0.5f, result[2], EPSILON, "and half of the blue");
    }

    @Test
    void testOcclusionDarkensTheAmbientLight() {
        final var lit = ambient(true, new float[]{1.f, 1.f, 1.f}, new float[]{0.8f, 0.8f, 0.8f}, 0.f, 0.5f, 1.f);
        final var occluded = ambient(true, new float[]{1.f, 1.f, 1.f}, new float[]{0.8f, 0.8f, 0.8f}, 0.f, 0.5f, 0.25f);
        final var dark = ambient(true, new float[]{1.f, 1.f, 1.f}, new float[]{0.8f, 0.8f, 0.8f}, 0.f, 0.5f, 0.f);

        for (int i = 0; i < 3; i++) {
            assertEquals(lit[i] * 0.25f, occluded[i], EPSILON, "A quarter of the light, channel " + i);
            assertEquals(0.f, dark[i], EPSILON, "No light, channel " + i);
        }
    }

    @Test
    void testSmoothSurfaceReflectsTheSkyboxItselfAndARoughOneTheBlurredMap() {
        final var white = new float[]{1.f, 1.f, 1.f};

        // A white metal seen straight on reflects what it sees: with the table (1, 0) it is the color itself
        final var mirror = ambientWithSkybox(white, white, 1.f, 0.f);
        assertArrayEquals(new float[]{0.9f, 0.1f, 0.5f}, new float[]{mirror[0], mirror[1], mirror[2]}, EPSILON, "A mirror shows the skybox");

        final var rough = ambientWithSkybox(white, white, 1.f, 1.f);
        assertArrayEquals(new float[]{1.f, 0.5f, 0.25f}, new float[]{rough[0], rough[1], rough[2]}, EPSILON, "A rough surface shows the prefiltered map");

        // Halfway between the roughness where the skybox ends (0.05) and where the map begins (0.25) both are mixed equally
        final var between = ambientWithSkybox(white, white, 1.f, 0.15f);
        assertEquals((0.9f + 1.f) / 2.f, between[0], EPSILON, "In between, red");
        assertEquals((0.1f + 0.5f) / 2.f, between[1], EPSILON, "In between, green");
    }

    @Test
    void testSkyboxIsNotUsedWhenTheSceneHasNone() {
        final var white = new float[]{1.f, 1.f, 1.f};
        final var mirror = ambient(true, white, white, 1.f, 0.f, 1.f);

        assertArrayEquals(new float[]{1.f, 0.5f, 0.25f}, new float[]{mirror[0], mirror[1], mirror[2]}, EPSILON, "Only the prefiltered map is left");
    }

    @Test
    void testBlackSurfaceDiffusesNothing() {
        final var result = ambient(true, new float[]{1.f, 1.f, 1.f}, new float[]{0.f, 0.f, 0.f}, 0.f, 1.f, 1.f);

        // Only the reflection of a non-metal is left
        assertEquals(0.04f * 1.f, result[0], EPSILON);
        assertEquals(0.04f * 0.5f, result[1], EPSILON);
        assertEquals(0.04f * 0.25f, result[2], EPSILON);
    }

    private float[] ambientWithSkybox(final float[] ambientLight, final float[] albedo, final float metalness, final float roughness) {
        return ambient(true, true, ambientLight, albedo, metalness, roughness, 1.f);
    }

    private float[] ambient(final boolean environment, final float[] ambientLight, final float[] albedo, final float metalness,
                            final float roughness, final float occlusion) {
        return ambient(environment, false, ambientLight, albedo, metalness, roughness, occlusion);
    }

    private float[] ambient(final boolean environment, final boolean withSkybox, final float[] ambientLight, final float[] albedo,
                            final float metalness, final float roughness, final float occlusion) {
        glActiveTexture(TextureUnit.Skybox.getLocationID());
        glBindTexture(GL_TEXTURE_CUBE_MAP, skybox);
        glActiveTexture(TextureUnit.Irradiance.getLocationID());
        glBindTexture(GL_TEXTURE_CUBE_MAP, irradiance);
        glActiveTexture(TextureUnit.Prefilter.getLocationID());
        glBindTexture(GL_TEXTURE_CUBE_MAP, prefilter);
        glActiveTexture(TextureUnit.Brdf.getLocationID());
        glBindTexture(GL_TEXTURE_2D, brdf);
        glActiveTexture(GL_TEXTURE0);

        final Map<String, Object> uniforms = new HashMap<>();
        uniforms.put("textureSkybox", TextureUnit.Skybox.getId());
        uniforms.put("useTextureSkybox", withSkybox ? 1 : 0);
        uniforms.put("textureIrradiance", TextureUnit.Irradiance.getId());
        uniforms.put("texturePrefilter", TextureUnit.Prefilter.getId());
        uniforms.put("textureBrdf", TextureUnit.Brdf.getId());
        uniforms.put("useEnvironment", environment ? 1 : 0);
        uniforms.put("prefilterLevels", 5.f);
        uniforms.put("ambientLight", ambientLight);
        uniforms.put("albedo", albedo);
        uniforms.put("metalness", metalness);
        uniforms.put("roughness", roughness);
        uniforms.put("occlusion", occlusion);

        return harness.run(List.of("common/environment.glsl"), DECLARATIONS, CODE, uniforms);
    }

    /** The approximation of the lookup table of the shader for the roughness and the cosine of the angle of the viewer */
    private static float[] approximation(final float cosine, final float roughness) {
        final var r = new float[]{
                roughness * -1.f + 1.f, roughness * -0.0275f + 0.0425f, roughness * -0.572f + 1.04f, roughness * 0.022f - 0.04f};
        final var a004 = Math.min(r[0] * r[0], (float) Math.pow(2.0, -9.28 * cosine)) * r[0] + r[1];

        return new float[]{-1.04f * a004 + r[2], 1.04f * a004 + r[3]};
    }

    /** A cube map of one texel per face of one color */
    private static int cube(final float red, final float green, final float blue) {
        final var id = glGenTextures();
        glBindTexture(GL_TEXTURE_CUBE_MAP, id);
        for (int face = 0; face < 6; face++)
            glTexImage2D(GL_TEXTURE_CUBE_MAP_POSITIVE_X + face, 0, GL_RGBA32F, 1, 1, 0, GL_RGBA, GL_FLOAT, new float[]{red, green, blue, 1.f});

        glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_MAX_LEVEL, 0);
        glBindTexture(GL_TEXTURE_CUBE_MAP, 0);

        return id;
    }

    /** A lookup table of one texel */
    private static int table(final float scale, final float bias) {
        final var id = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, id);
        try (final var stack = MemoryStack.stackPush()) {
            final FloatBuffer texel = stack.floats(scale, bias);
            glTexImage2D(GL_TEXTURE_2D, 0, GL_RG32F, 1, 1, 0, GL_RG, GL_FLOAT, texel);
        }

        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        glBindTexture(GL_TEXTURE_2D, 0);

        return id;
    }
}
