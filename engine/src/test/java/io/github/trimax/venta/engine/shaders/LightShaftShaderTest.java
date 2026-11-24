package io.github.trimax.venta.engine.shaders;

import static org.junit.jupiter.api.Assertions.*;
import static org.lwjgl.opengl.GL11C.*;
import static org.lwjgl.opengl.GL13C.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13C.glActiveTexture;
import static org.lwjgl.opengl.GL14C.GL_TEXTURE_COMPARE_FUNC;
import static org.lwjgl.opengl.GL14C.GL_TEXTURE_COMPARE_MODE;
import static org.lwjgl.opengl.GL30C.*;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import io.github.trimax.venta.engine.enums.TextureUnit;

/**
 * The light shafts (common/light-shafts.glsl) on the GPU. The fog is thin and level (its falloff is very small), so the
 * light that a ray gathers can be computed by a formula: the fog scatters a part of the light that is equal to the part
 * of the light of the sun that it does not let through.
 */
final class LightShaftShaderTest {
    private static final float EPSILON = 1e-3f;
    private static final float[] SUN_COLOR = {1.f, 0.5f, 0.25f};

    private static ShaderHarness harness;

    @BeforeAll
    static void context() {
        GLTestContext.require();
        harness = new ShaderHarness();
    }

    /** The uniforms of a camera at the origin, a level fog of a density and the sun that shines down along y */
    private static Map<String, Object> scene(final float density, final int steps, final float anisotropy) {
        final Map<String, Object> uniforms = new HashMap<>();
        uniforms.put("fog.density", density);
        uniforms.put("fog.falloff", 1e-6f);
        uniforms.put("fog.baseHeight", 0.f);
        uniforms.put("cameraPosition", new float[]{0, 0, 0});
        uniforms.put("directionalLight.color", SUN_COLOR);
        uniforms.put("directionalLight.direction", new float[]{0, -1, 0});
        uniforms.put("directionalLight.intensity", 2.f);
        uniforms.put("lightShaftsSteps", steps);
        uniforms.put("lightShaftsAnisotropy", anisotropy);
        uniforms.put("useShadows", 0);
        return uniforms;
    }

    private static float[] shafts(final Map<String, Object> uniforms, final float[] direction, final float length, final float jitter) {
        final Map<String, Object> all = new HashMap<>(uniforms);
        all.put("direction", direction);
        all.put("rayLength", length);
        all.put("jitter", jitter);

        return harness.run(List.of("common/light-shafts.glsl"), "uniform vec3 direction; uniform float rayLength; uniform float jitter;",
                "outputColor = vec4(getLightShafts(direction, rayLength, jitter), 1.0);", all);
    }

    private static float phase(final float cosine, final float anisotropy) {
        return harness.run(List.of("common/light-shafts.glsl"), "uniform float cosine; uniform float anisotropy;",
                "outputColor = vec4(getShaftsPhase(cosine, anisotropy));", Map.of("cosine", cosine, "anisotropy", anisotropy))[0];
    }

    @Test
    void testFogThatScattersEquallyHasThePhaseOfOne() {
        for (final var cosine : new float[]{-1.f, -0.3f, 0.f, 0.6f, 1.f})
            assertEquals(1.f, phase(cosine, 0.f), EPSILON, "At " + cosine);
    }

    @Test
    void testForwardScatteringIsStrongestTowardsTheSun() {
        final var forward = phase(1.f, 0.6f);
        final var side = phase(0.f, 0.6f);
        final var back = phase(-1.f, 0.6f);

        assertTrue(forward > side && side > back, forward + " " + side + " " + back);
        assertEquals((1.f - 0.36f) / (float) Math.pow(1.f + 0.36f - 1.2f, 1.5), forward, forward * EPSILON, "The function of Henyey and Greenstein");
    }

    @Test
    void testBackwardScatteringIsStrongestAwayFromTheSun() {
        assertTrue(phase(-1.f, -0.6f) > phase(1.f, -0.6f));
    }

    @Test
    void testPhaseHasTheSameAverageWhateverTheAnisotropy() {
        // The integral of the phase function over the sphere is the same: the cosine of a ray is uniform for a uniform sphere
        for (final var anisotropy : new float[]{-0.7f, 0.f, 0.3f, 0.8f}) {
            var sum = 0.0;
            final var samples = 4000;
            for (int i = 0; i < samples; i++)
                sum += phase(-1.f + 2.f * (i + 0.5f) / samples, anisotropy);

            assertEquals(1.0, sum / samples, 0.02, "The anisotropy " + anisotropy);
        }
    }

    @Test
    void testRayThroughLitFogGathersTheLightThatTheFogDoesNotLetThrough() {
        // The ray looks sideways, a right angle to the sun: the phase is that of cosine 0
        final var density = 0.01f;
        final var length = 100.f;
        final var result = shafts(scene(density, 64, 0.f), new float[]{1, 0, 0}, length, 0.5f);

        final var scattered = 1.f - (float) Math.exp(-density * length);
        assertEquals(SUN_COLOR[0] * 2.f * scattered, result[0], scattered * 0.02f);
        assertEquals(SUN_COLOR[1] * 2.f * scattered, result[1], scattered * 0.02f);
        assertEquals(SUN_COLOR[2] * 2.f * scattered, result[2], scattered * 0.02f);
    }

    @Test
    void testMoreStepsGiveTheSameLight() {
        final var coarse = shafts(scene(0.01f, 8, 0.f), new float[]{1, 0, 0}, 100.f, 0.5f)[0];
        final var fine = shafts(scene(0.01f, 128, 0.f), new float[]{1, 0, 0}, 100.f, 0.5f)[0];

        assertEquals(fine, coarse, fine * 1e-3f, "A fog of a constant density gives the same light with any number of steps");
    }

    @Test
    void testStepsOfAThickFogDoNotScatterMoreThanThereIs() {
        final var uniforms = scene(5.f, 4, 0.f);

        assertEquals(SUN_COLOR[0] * 2.f, shafts(uniforms, new float[]{1, 0, 0}, 100.f, 0.5f)[0], 1e-3f, "A step much thicker than 1 scatters its whole share");
    }

    @Test
    void testDenserFogGathersMoreLightButNotWithoutLimit() {
        final var thin = shafts(scene(0.001f, 64, 0.f), new float[]{1, 0, 0}, 100.f, 0.5f)[0];
        final var thick = shafts(scene(0.01f, 64, 0.f), new float[]{1, 0, 0}, 100.f, 0.5f)[0];
        final var opaque = shafts(scene(10.f, 64, 0.f), new float[]{1, 0, 0}, 100.f, 0.5f)[0];

        assertTrue(thin < thick && thick < opaque, thin + " " + thick + " " + opaque);
        assertEquals(SUN_COLOR[0] * 2.f, opaque, 0.2f, "A fog that is never seen through scatters all the light of the sun");
    }

    @Test
    void testLookingAtTheSunGathersMoreThanLookingAway() {
        // The sun is up: the light travels along -y, so the direction to the sun is +y
        final var towards = shafts(scene(0.01f, 64, 0.6f), new float[]{0, 1, 0}, 100.f, 0.5f)[0];
        final var aside = shafts(scene(0.01f, 64, 0.6f), new float[]{1, 0, 0}, 100.f, 0.5f)[0];
        final var away = shafts(scene(0.01f, 64, 0.6f), new float[]{0, -1, 0}, 100.f, 0.5f)[0];

        assertTrue(towards > aside && aside > away, towards + " " + aside + " " + away);
    }

    @Test
    void testNoFogOrNoRayGathersNoLight() {
        assertEquals(0.f, shafts(scene(0.f, 32, 0.6f), new float[]{1, 0, 0}, 100.f, 0.5f)[0], EPSILON, "No fog");
        assertEquals(0.f, shafts(scene(0.01f, 32, 0.6f), new float[]{1, 0, 0}, 0.f, 0.5f)[0], EPSILON, "A ray of no length");
    }

    @Test
    void testFogThinsOutWithTheHeightSoTheRayGathersLessUp() {
        final var uniforms = scene(0.01f, 64, 0.f);
        uniforms.put("fog.falloff", 0.05f);

        final var low = shafts(uniforms, new float[]{1, 0, 0}, 100.f, 0.5f)[0];
        final var uniformsHigh = new HashMap<>(uniforms);
        uniformsHigh.put("cameraPosition", new float[]{0, 80, 0});
        final var high = shafts(uniformsHigh, new float[]{1, 0, 0}, 100.f, 0.5f)[0];

        assertTrue(low > high * 5.f, low + " against " + high);
    }

    @Test
    void testJitterMovesThePointsButNotTheLightMuch() {
        final var uniforms = scene(0.01f, 32, 0.f);

        final var early = shafts(uniforms, new float[]{1, 0, 0}, 100.f, 0.05f)[0];
        final var late = shafts(uniforms, new float[]{1, 0, 0}, 100.f, 0.95f)[0];

        assertEquals(early, late, early * 0.05f, "The jitter turns the bands of the steps into noise, it doesn't change the amount");
    }

    @Test
    void testShadowedRayGathersNothing() {
        // A map of the shadows that is full: all the points are in the shadow of something
        final var texture = glGenTextures();
        glBindTexture(GL_TEXTURE_2D_ARRAY, texture);
        glTexImage3D(GL_TEXTURE_2D_ARRAY, 0, GL_DEPTH_COMPONENT32F, 4, 4, 1, 0, GL_DEPTH_COMPONENT, GL_FLOAT, (ByteBuffer) null);
        glTexParameteri(GL_TEXTURE_2D_ARRAY, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D_ARRAY, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D_ARRAY, GL_TEXTURE_COMPARE_MODE, GL_COMPARE_REF_TO_TEXTURE);
        glTexParameteri(GL_TEXTURE_2D_ARRAY, GL_TEXTURE_COMPARE_FUNC, GL_LEQUAL);

        final var framebuffer = glGenFramebuffers();
        glBindFramebuffer(GL_FRAMEBUFFER, framebuffer);
        glFramebufferTextureLayer(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, texture, 0, 0);
        glDrawBuffer(GL_NONE);
        glReadBuffer(GL_NONE);
        assertEquals(GL_FRAMEBUFFER_COMPLETE, glCheckFramebufferStatus(GL_FRAMEBUFFER));

        try {
            // The camera looks along x from the origin; the matrices are identities, so a point of the world is a point of the map
            final var uniforms = scene(0.01f, 32, 0.f);
            uniforms.put("textureShadow", TextureUnit.Shadow.getId());
            uniforms.put("useShadows", 1);
            uniforms.put("shadowCascadeCount", 1);
            uniforms.put("shadowMatrices", new ShaderHarness.Matrices(1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1));
            uniforms.put("shadowSplits", new ShaderHarness.Floats(1000.f));
            uniforms.put("shadowTexels", new ShaderHarness.Floats(0.f));
            uniforms.put("shadowDistance", 1000.f);
            uniforms.put("cameraForward", new float[]{1, 0, 0});

            for (final var depth : new float[]{0.f, 1.f}) {
                // The harness draws into its own framebuffer: the layer of the map is the one that is cleared
                glBindFramebuffer(GL_FRAMEBUFFER, framebuffer);
                glClearDepth(depth);
                glClear(GL_DEPTH_BUFFER_BIT);
                glClearDepth(1.0);

                glActiveTexture(TextureUnit.Shadow.getLocationID());
                glBindTexture(GL_TEXTURE_2D_ARRAY, texture);
                glActiveTexture(GL_TEXTURE0);

                // The ray goes through the map (its points are in the cube of the map: 0 to 0.9 in x, and the depth is z at 0)
                final var result = shafts(uniforms, new float[]{1, 0, 0}, 0.9f, 0.5f)[0];
                if (depth == 0.f)
                    assertEquals(0.f, result, 1e-6f, "Every point is behind a stored depth of 0: all in the shadow");
                else
                    assertTrue(result > 0.f, "A map without anything in it lets the light through");
            }
        } finally {
            glBindFramebuffer(GL_FRAMEBUFFER, 0);
            glDeleteFramebuffers(framebuffer);
            glActiveTexture(TextureUnit.Shadow.getLocationID());
            glBindTexture(GL_TEXTURE_2D_ARRAY, 0);
            glActiveTexture(GL_TEXTURE0);
            glDeleteTextures(texture);
        }
    }

    @Test
    void testSampleOfTheSameLengthCountsMoreThanOneOfAnother() {
        final var same = weight(100.f, 100.f);
        final var near = weight(100.f, 95.f);
        final var far = weight(100.f, 10.f);

        assertTrue(same > near && near > far && far > 0.f, same + " " + near + " " + far);
        assertEquals(weight(100.f, 80.f), weight(80.f, 100.f), 0.f, "The weight does not depend on the order");
    }

    @Test
    void testSkyAndSurfaceDoNotMixInTheWeights() {
        assertTrue(weight(300.f, 300.f) > 20.f * weight(300.f, 40.f), "A sample of a close surface hardly counts for a pixel of the sky");
    }

    private static float weight(final float rayLength, final float sampled) {
        return harness.run(List.of("common/light-shafts.glsl"), "uniform float rayLength; uniform float sampled;",
                "outputColor = vec4(getShaftsWeight(rayLength, sampled));", Map.of("rayLength", rayLength, "sampled", sampled))[0];
    }
}
