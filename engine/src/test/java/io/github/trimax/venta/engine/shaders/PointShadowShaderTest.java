package io.github.trimax.venta.engine.shaders;

import static org.junit.jupiter.api.Assertions.*;
import static org.lwjgl.opengl.GL11C.*;
import static org.lwjgl.opengl.GL13C.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13C.glActiveTexture;
import static org.lwjgl.opengl.GL14C.GL_TEXTURE_COMPARE_FUNC;
import static org.lwjgl.opengl.GL14C.GL_TEXTURE_COMPARE_MODE;
import static org.lwjgl.opengl.GL30C.*;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.github.trimax.venta.engine.definitions.DefinitionsCommon;
import io.github.trimax.venta.engine.enums.TextureUnit;
import io.github.trimax.venta.engine.helpers.PointShadowHelper;
import io.github.trimax.venta.engine.model.common.shadow.PointShadowFrame;
import io.github.trimax.venta.engine.model.instance.implementation.LightInstanceImplementation;
import io.github.trimax.venta.engine.model.instance.implementation.TestObjects;

/**
 * The shadows of the point lights (common/point-shadow.glsl) on the GPU. The maps are filled with one depth, so a point is
 * in a shadow when it is farther from its light than that, and lit when it is nearer: the faces of a cube of a light are the
 * ones the helper makes the matrices for, so the test tells that the shader and the helper agree on the faces.
 */
final class PointShadowShaderTest {
    private static final float EPSILON = 1e-3f;
    private static final int RESOLUTION = 64;
    private static final int LAYERS = DefinitionsCommon.MAX_POINT_SHADOWS * DefinitionsCommon.POINT_SHADOW_FACES;
    private static final float RANGE = 40.f;
    private static final float STORED = 10.f;
    private static final float[] UP = {0, 1, 0};

    private static ShaderHarness harness;

    private final PointShadowHelper helper = new PointShadowHelper();
    private final PointShadowFrame frame = new PointShadowFrame(DefinitionsCommon.MAX_POINT_SHADOWS, DefinitionsCommon.POINT_SHADOW_FACES);

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
        glTexImage3D(GL_TEXTURE_2D_ARRAY, 0, GL_DEPTH_COMPONENT32F, RESOLUTION, RESOLUTION, LAYERS, 0, GL_DEPTH_COMPONENT, GL_FLOAT, (ByteBuffer) null);
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
        glActiveTexture(TextureUnit.PointShadow.getLocationID());
        glBindTexture(GL_TEXTURE_2D_ARRAY, 0);
        glActiveTexture(GL_TEXTURE0);
        glDeleteTextures(texture);
    }

    /** The slots of lights at a position that the helper makes the matrices of: the nearest light to the camera is the slot 0 */
    private void assign(final Vector3f... positions) {
        final var lights = new ArrayList<LightInstanceImplementation>();
        for (int i = 0; i < positions.length; i++)
            lights.add(TestObjects.light("light " + i, positions[i], RANGE, true));

        helper.assign(frame, lights, new Vector3f());
    }

    /** Fills the layers of a slot with the depth that a point at the distance has on its face: it is as if a wall stood there */
    private void fill(final int slot, final float distance) {
        final var light = frame.getLight(slot).getPosition();
        for (int face = 0; face < frame.getFaces(); face++) {
            final var direction = PointShadowHelper.FACE_DIRECTIONS[face];
            final var clip = new Vector4f(light.x() + direction.x() * distance, light.y() + direction.y() * distance, light.z() + direction.z() * distance, 1.f)
                    .mul(frame.getMatrix(slot, face));
            fillLayer(slot * frame.getFaces() + face, clip.z / clip.w * 0.5f + 0.5f);
        }
    }

    private void fillLayer(final int layer, final float depth) {
        final var framebuffer = glGenFramebuffers();
        glBindFramebuffer(GL_FRAMEBUFFER, framebuffer);
        glFramebufferTextureLayer(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, texture, 0, layer);
        glDrawBuffer(GL_NONE);
        glReadBuffer(GL_NONE);
        assertEquals(GL_FRAMEBUFFER_COMPLETE, glCheckFramebufferStatus(GL_FRAMEBUFFER));

        glClearDepth(depth);
        glClear(GL_DEPTH_BUFFER_BIT);
        glClearDepth(1.0);
        glBindFramebuffer(GL_FRAMEBUFFER, 0);
        glDeleteFramebuffers(framebuffer);
    }

    /** The matrices of all the slots as the shader takes them: 16 numbers each, column by column */
    private float[] matrices() {
        final var numbers = new float[LAYERS * 16];
        for (int slot = 0; slot < DefinitionsCommon.MAX_POINT_SHADOWS; slot++)
            for (int face = 0; face < frame.getFaces(); face++) {
                final var matrix = slot < frame.getCount() ? frame.getMatrix(slot, face) : new Matrix4f();
                matrix.get(numbers, (slot * frame.getFaces() + face) * 16);
            }

        return numbers;
    }

    private float shadow(final Vector3f lightPosition, final int slot, final float[] position, final float[] normal) {
        glActiveTexture(TextureUnit.PointShadow.getLocationID());
        glBindTexture(GL_TEXTURE_2D_ARRAY, texture);
        glActiveTexture(GL_TEXTURE0);

        final Map<String, Object> uniforms = new HashMap<>();
        uniforms.put("light.position", new float[]{lightPosition.x, lightPosition.y, lightPosition.z});
        uniforms.put("light.shadowSlot", slot);
        uniforms.put("usePointShadows", 1);
        uniforms.put("texturePointShadow", TextureUnit.PointShadow.getId());
        uniforms.put("pointShadowMatrices", new ShaderHarness.Matrices(matrices()));
        uniforms.put("position", position);
        uniforms.put("normal", normal);

        return harness.run(List.of("common/point-shadow.glsl"), "uniform Light light; uniform vec3 position; uniform vec3 normal;",
                "outputColor = vec4(getPointShadow(light, position, normal));", uniforms)[0];
    }

    private float shadow(final Vector3f lightPosition, final float[] direction, final float distance) {
        final var length = (float) Math.sqrt(direction[0] * direction[0] + direction[1] * direction[1] + direction[2] * direction[2]);
        final var position = new float[]{lightPosition.x + direction[0] / length * distance, lightPosition.y + direction[1] / length * distance,
                lightPosition.z + direction[2] / length * distance};

        return shadow(lightPosition, 0, position, UP);
    }

    private static final float[][] DIRECTIONS = {
            {1, 0, 0}, {-1, 0, 0}, {0, 1, 0}, {0, -1, 0}, {0, 0, 1}, {0, 0, -1},
            {1, 1, 0.5f}, {-1, 0.4f, 0.8f}, {0.3f, -1, -0.9f}, {-0.7f, -0.8f, 1}, {1, -0.9f, -0.2f}, {0.2f, 0.9f, -1}};

    @Test
    void testPointNearerThanTheStoredDepthIsLitInEveryDirection() {
        final var light = new Vector3f(1.f, 2.f, 3.f);
        assign(light);
        fill(0, STORED);

        for (final var direction : DIRECTIONS)
            assertEquals(1.f, shadow(light, direction, STORED * 0.5f), EPSILON, "Nearer than the wall, direction " + Arrays.toString(direction));
    }

    @Test
    void testPointBehindTheStoredDepthIsInTheShadowInEveryDirection() {
        final var light = new Vector3f(1.f, 2.f, 3.f);
        assign(light);
        fill(0, STORED);

        for (final var direction : DIRECTIONS)
            assertEquals(0.f, shadow(light, direction, STORED * 2.f), EPSILON, "Behind the wall, direction " + Arrays.toString(direction));
    }

    @Test
    void testPointBeyondTheRangeIsLit() {
        final var light = new Vector3f();
        assign(light);
        fill(0, STORED);

        assertEquals(1.f, shadow(light, new float[]{1, 0, 0}, RANGE * 1.5f), EPSILON, "The far plane of the faces is the range");
    }

    @Test
    void testLightWithoutASlotLightsEverything() {
        final var light = new Vector3f();
        assign(light);
        fill(0, STORED);

        assertEquals(1.f, shadow(light, -1, new float[]{20, 0, 0}, UP), EPSILON, "No map");
    }

    @Test
    void testWithoutPointShadowsEverythingIsLit() {
        final var light = new Vector3f();
        assign(light);
        fill(0, STORED);

        glActiveTexture(TextureUnit.PointShadow.getLocationID());
        glBindTexture(GL_TEXTURE_2D_ARRAY, texture);
        glActiveTexture(GL_TEXTURE0);

        final Map<String, Object> uniforms = new HashMap<>();
        uniforms.put("light.position", new float[]{0, 0, 0});
        uniforms.put("light.shadowSlot", 0);
        uniforms.put("usePointShadows", 0);
        uniforms.put("texturePointShadow", TextureUnit.PointShadow.getId());
        uniforms.put("pointShadowMatrices", new ShaderHarness.Matrices(matrices()));
        uniforms.put("position", new float[]{20, 0, 0});
        uniforms.put("normal", UP);

        assertEquals(1.f, harness.run(List.of("common/point-shadow.glsl"), "uniform Light light; uniform vec3 position; uniform vec3 normal;",
                "outputColor = vec4(getPointShadow(light, position, normal));", uniforms)[0], EPSILON);
    }

    @Test
    void testEverySlotHasItsOwnFaces() {
        // Two lights at the same place (the matrices are the same): the walls of the first are near, the ones of the second are far
        final var light = new Vector3f(0.f, 0.f, 0.f);
        assign(light, new Vector3f(0.f, 0.001f, 0.f));
        fill(0, STORED);
        fill(1, STORED * 4.f);

        final var position = new float[]{STORED * 2.f, 0, 0};
        assertEquals(0.f, shadow(light, 0, position, UP), EPSILON, "Behind the wall of the first slot");
        assertEquals(1.f, shadow(light, 1, position, UP), EPSILON, "In front of the wall of the second slot");
    }

    @Test
    void testFaceIsChosenByTheAxisAPointLiesTheMostAlong() {
        for (final var expected : new Object[][]{
                {new float[]{5, 1, 2}, 0}, {new float[]{-5, 1, 2}, 1}, {new float[]{1, 5, 2}, 2}, {new float[]{1, -5, 2}, 3},
                {new float[]{1, 2, 5}, 4}, {new float[]{1, 2, -5}, 5}, {new float[]{3, 3, 1}, 0}, {new float[]{1, 3, 3}, 2}, {new float[]{-3, 1, 3}, 1}}) {
            final var face = harness.run(List.of("common/point-shadow.glsl"), "uniform vec3 direction;",
                    "outputColor = vec4(float(getPointShadowFace(direction)));", Map.of("direction", expected[0]))[0];

            assertEquals((int) (Integer) expected[1], Math.round(face), "The face of " + Arrays.toString((float[]) expected[0]));
        }
    }

    @Test
    void testRangeFadesTheLightSmoothlyToNothing() {
        assertEquals(1.f, fade(50.f, 0.f), EPSILON, "At the light");
        assertEquals(0.f, fade(50.f, 50.f), EPSILON, "At the range");
        assertEquals(0.f, fade(50.f, 80.f), EPSILON, "Beyond the range");

        var previous = 1.f;
        for (var distance = 5.f; distance <= 50.f; distance += 5.f) {
            final var value = fade(50.f, distance);

            assertTrue(value <= previous + EPSILON, "The light never gets stronger with the distance: " + value + " after " + previous);
            previous = value;
        }

        final var share = 0.5f;
        assertEquals((1.f - share * share * share * share) * (1.f - share * share * share * share), fade(50.f, 25.f), EPSILON, "Half of the way");
    }

    @Test
    void testLightWithoutARangeIsNotFaded() {
        assertEquals(1.f, fade(0.f, 0.f), EPSILON);
        assertEquals(1.f, fade(0.f, 1000.f), EPSILON, "Only its attenuation weakens it");
    }

    @Test
    void testFadeHasNoEdgeAtTheRange() {
        final var before = fade(50.f, 49.9f);

        assertTrue(before < 0.02f, "The light is nearly gone before the range, there is no step: " + before);
    }

    private static float fade(final float range, final float distance) {
        return harness.run(List.of("common/lights.glsl"), "uniform float range; uniform float distance;",
                "outputColor = vec4(getLightFade(range, distance));", Map.of("range", range, "distance", distance))[0];
    }
}
