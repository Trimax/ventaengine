package io.github.trimax.venta.engine.shaders;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
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

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.github.trimax.venta.engine.enums.TextureUnit;

/**
 * The shadows of the sun in the shaders (common/shadow.glsl) on the GPU. The cascades have identity matrices, so a point
 * of the world is the point of the light space: its depth in the map is z / 2 + 0.5. The camera at the origin looks along
 * the x axis, so the distance of a point from the camera is its x.
 */
final class ShadowShaderTest {
    private static final float EPSILON = 1e-4f;
    private static final int RESOLUTION = 4;
    private static final int CASCADES = 2;
    private static final float DISTANCE = 1.f;
    private static final float[] SPLITS = {0.5f, 1.f};
    private static final float[] UP = {0, 1, 0};
    private static final float[] IDENTITIES = {
            1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1,
            1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1};

    private static ShaderHarness harness;

    private int texture;

    @BeforeAll
    static void context() {
        GLTestContext.require();
        harness = new ShaderHarness();
    }

    @BeforeEach
    void createMap() {
        texture = glGenTextures();
        glBindTexture(GL_TEXTURE_2D_ARRAY, texture);
        glTexImage3D(GL_TEXTURE_2D_ARRAY, 0, GL_DEPTH_COMPONENT32F, RESOLUTION, RESOLUTION, CASCADES, 0, GL_DEPTH_COMPONENT, GL_FLOAT, (ByteBuffer) null);
        glTexParameteri(GL_TEXTURE_2D_ARRAY, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D_ARRAY, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D_ARRAY, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_2D_ARRAY, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_2D_ARRAY, GL_TEXTURE_COMPARE_MODE, GL_COMPARE_REF_TO_TEXTURE);
        glTexParameteri(GL_TEXTURE_2D_ARRAY, GL_TEXTURE_COMPARE_FUNC, GL_LEQUAL);
        glBindTexture(GL_TEXTURE_2D_ARRAY, 0);
    }

    @AfterEach
    void deleteMap() {
        glActiveTexture(TextureUnit.Shadow.getLocationID());
        glBindTexture(GL_TEXTURE_2D_ARRAY, 0);
        glActiveTexture(GL_TEXTURE0);
        glDeleteTextures(texture);
    }

    @Test
    void testWithoutShadowsAllTheLightArrives() {
        fill(0.f, 0.f);

        assertEquals(1.f, shadow(point(0.2f, 0.f, 0.f), UP, parameters().with("useShadows", 0)), EPSILON);
    }

    @Test
    void testPointBehindTheStoredDepthIsInTheShadow() {
        fill(0.5f, 0.5f);

        assertEquals(1.f, shadow(point(0.2f, 0.f, -0.5f), UP, parameters()), EPSILON, "Before the stored depth: 0.25 < 0.5");
        assertEquals(0.f, shadow(point(0.2f, 0.f, 0.5f), UP, parameters()), EPSILON, "Behind the stored depth: 0.75 > 0.5");
    }

    @Test
    void testSecondCascadeIsChosenAfterTheFirstSplit() {
        fill(1.f, 0.f);

        assertEquals(1.f, shadow(point(0.2f, 0.f, 0.f), UP, parameters()), EPSILON, "The first cascade is empty: lit");
        assertEquals(1.f, shadow(point(0.45f, 0.f, 0.f), UP, parameters()), EPSILON, "Just before the first split");
        assertEquals(0.f, shadow(point(0.55f, 0.f, 0.f), UP, parameters()), EPSILON, "The second cascade is full: shadow");
    }

    @Test
    void testFirstCascadeIsChosenBeforeTheSplit() {
        fill(0.f, 1.f);

        assertEquals(0.f, shadow(point(0.2f, 0.f, 0.f), UP, parameters()), EPSILON, "The first cascade is full: shadow");
        assertEquals(1.f, shadow(point(0.7f, 0.f, 0.f), UP, parameters()), EPSILON, "The second cascade is empty: lit");
    }

    @Test
    void testBeyondTheLastCascadeIsLit() {
        fill(0.f, 0.f);

        assertEquals(1.f, shadow(point(1.5f, 0.f, 0.f), UP, parameters()), EPSILON, "Behind the shadow distance");
    }

    @Test
    void testBehindTheCameraUsesTheFirstCascade() {
        fill(0.f, 1.f);

        assertEquals(0.f, shadow(point(-0.2f, 0.f, 0.f), UP, parameters()), EPSILON, "A negative distance is before the first split");
    }

    @Test
    void testOutsideTheDepthRangeIsLit() {
        fill(0.f, 0.f);

        assertEquals(1.f, shadow(point(0.2f, 0.f, 1.5f), UP, parameters()), EPSILON, "Farther than the far plane of the cascade");
    }

    @Test
    void testShadowsFadeOutAtTheEndOfTheLastCascade() {
        fill(0.f, 0.f);

        for (final var x : new float[]{0.6f, 0.8f, 0.85f, 0.9f, 0.95f, 0.99f}) {
            final var t = Math.max(0.f, Math.min(1.f, (x - 0.85f * DISTANCE) / (0.15f * DISTANCE)));
            final var expected = t * t * (3.f - 2.f * t);

            assertEquals(expected, shadow(point(x, 0.f, 0.f), UP, parameters()), EPSILON, "The distance " + x);
        }
    }

    @Test
    void testPointIsMovedToTheLightByTheTexelSize() {
        fill(0.5f, 0.5f);

        // The sun shines along -z, so a point is moved to the sun by 1.5 texels of 0.1 to smaller depths (acne is gone)
        final var light = new float[]{0, 0, 1};
        final var normal = new float[]{0, 0, -1};
        final var surface = point(0.2f, 0.f, 0.1f);

        assertEquals(0.f, shadow(surface, normal, parameters().with("directionalLight.direction", light)),
                EPSILON, "Without the offset the point is behind the stored depth: 0.55 > 0.5");
        assertEquals(1.f, shadow(surface, normal, parameters().with("directionalLight.direction", light)
                .with("shadowTexels", new ShaderHarness.Floats(0.1f, 0.1f))), EPSILON, "With the offset it is at 0.475 and is lit");
    }

    @Test
    void testEdgeOfShadowIsSoft() {
        // The lower half of the texels of the first layer is in the shadow, the upper half is lit
        glBindFramebuffer(GL_FRAMEBUFFER, layer(0));
        glClearDepth(1.0);
        glClear(GL_DEPTH_BUFFER_BIT);
        glEnable(GL_SCISSOR_TEST);
        glScissor(0, 0, RESOLUTION, RESOLUTION / 2);
        glClearDepth(0.0);
        glClear(GL_DEPTH_BUFFER_BIT);
        glDisable(GL_SCISSOR_TEST);
        glClearDepth(1.0);
        glBindFramebuffer(GL_FRAMEBUFFER, 0);

        final var edge = shadow(point(0.2f, 0.f, 0.f), UP, parameters().with("shadowTexels", new ShaderHarness.Floats(0.f, 0.f)));
        assertTrue(edge > 0.05f && edge < 0.95f, "Between the shadow and the light: " + edge);

        assertEquals(0.f, shadow(point(0.2f, -0.9f, 0.f), UP, parameters()), EPSILON, "Deep in the shadowed half");
        assertEquals(1.f, shadow(point(0.2f, 0.9f, 0.f), UP, parameters()), EPSILON, "Deep in the lit half");
    }

    private float shadow(final float[] position, final float[] normal, final Parameters parameters) {
        glActiveTexture(TextureUnit.Shadow.getLocationID());
        glBindTexture(GL_TEXTURE_2D_ARRAY, texture);
        glActiveTexture(GL_TEXTURE0);

        final var uniforms = new HashMap<>(parameters.uniforms);
        uniforms.put("position", position);
        uniforms.put("normal", normal);

        return harness.run(List.of("common/shadow.glsl"), "uniform vec3 position; uniform vec3 normal;",
                "outputColor = vec4(getSunShadow(position, normal));", uniforms)[0];
    }

    /** Fills the layers of the map with a depth: 1 is empty (everything is lit), 0 is full (everything is in the shadow) */
    private void fill(final float first, final float second) {
        final var depths = new float[]{first, second};
        for (int i = 0; i < CASCADES; i++) {
            glBindFramebuffer(GL_FRAMEBUFFER, layer(i));
            glClearDepth(depths[i]);
            glClear(GL_DEPTH_BUFFER_BIT);
        }

        glClearDepth(1.0);
        glBindFramebuffer(GL_FRAMEBUFFER, 0);
    }

    private int layer(final int layer) {
        final var framebuffer = glGenFramebuffers();
        glBindFramebuffer(GL_FRAMEBUFFER, framebuffer);
        glFramebufferTextureLayer(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, texture, 0, layer);
        glDrawBuffer(GL_NONE);
        glReadBuffer(GL_NONE);
        assertEquals(GL_FRAMEBUFFER_COMPLETE, glCheckFramebufferStatus(GL_FRAMEBUFFER));

        return framebuffer;
    }

    private static float[] point(final float x, final float y, final float z) {
        return new float[]{x, y, z};
    }

    private static Parameters parameters() {
        return new Parameters();
    }

    /** The uniforms of the shadows: no offsets, the sun shines down, the camera is at the origin and looks along x */
    private static final class Parameters {
        private final Map<String, Object> uniforms = new HashMap<>();

        Parameters() {
            uniforms.put("useShadows", 1);
            uniforms.put("textureShadow", TextureUnit.Shadow.getId());
            uniforms.put("shadowCascadeCount", CASCADES);
            uniforms.put("shadowMatrices", new ShaderHarness.Matrices(IDENTITIES));
            uniforms.put("shadowSplits", new ShaderHarness.Floats(SPLITS));
            uniforms.put("shadowTexels", new ShaderHarness.Floats(0.f, 0.f));
            uniforms.put("shadowDistance", DISTANCE);
            uniforms.put("cameraPosition", new float[]{0, 0, 0});
            uniforms.put("cameraForward", new float[]{1, 0, 0});
            uniforms.put("directionalLight.direction", new float[]{0, -1, 0});
        }

        Parameters with(final String name, final Object value) {
            uniforms.put(name, value);
            return this;
        }
    }
}
