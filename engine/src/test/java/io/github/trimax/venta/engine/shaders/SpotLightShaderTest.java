package io.github.trimax.venta.engine.shaders;

import static org.junit.jupiter.api.Assertions.*;
import static org.lwjgl.opengl.GL11C.*;
import static org.lwjgl.opengl.GL12C.GL_CLAMP_TO_EDGE;
import static org.lwjgl.opengl.GL13C.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13C.glActiveTexture;
import static org.lwjgl.opengl.GL30C.*;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.github.trimax.venta.engine.definitions.DefinitionsCommon;
import io.github.trimax.venta.engine.enums.TextureUnit;
import io.github.trimax.venta.engine.helpers.PointShadowHelper;
import io.github.trimax.venta.engine.model.common.shadow.PointShadowFrame;
import io.github.trimax.venta.engine.model.instance.implementation.TestObjects;

/**
 * The cone of the spot lights (common/lights.glsl) and their shadows (common/point-shadow.glsl) on the GPU.
 */
final class SpotLightShaderTest {
    private static final float EPSILON = 1e-3f;
    private static final int RESOLUTION = 64;
    private static final int LAYERS = DefinitionsCommon.MAX_POINT_SHADOWS * DefinitionsCommon.POINT_SHADOW_FACES;
    private static final float INNER = 20.f;
    private static final float OUTER = 40.f;
    private static final float RANGE = 60.f;
    private static final float STORED = 10.f;

    private static ShaderHarness harness;

    private final PointShadowHelper helper = new PointShadowHelper();
    private final PointShadowFrame frame = new PointShadowFrame(DefinitionsCommon.MAX_POINT_SHADOWS, DefinitionsCommon.POINT_SHADOW_FACES);

    private int texture;

    @BeforeAll
    static void context() {
        GLTestContext.require();
        harness = new ShaderHarness();
    }

    @AfterAll
    static void release() {
        harness = null;
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

    private static Map<String, Object> light(final Vector3f position, final Vector3f direction, final boolean spot) {
        final var cosInner = (float) Math.cos(Math.toRadians(INNER));
        final var cosOuter = (float) Math.cos(Math.toRadians(OUTER));

        final Map<String, Object> uniforms = new HashMap<>();
        uniforms.put("light.position", new float[]{position.x, position.y, position.z});
        uniforms.put("light.direction", new float[]{direction.x, direction.y, direction.z});
        uniforms.put("light.spot", spot ? 1 : 0);
        uniforms.put("light.cosInner", cosInner);
        uniforms.put("light.cosOuter", cosOuter);
        uniforms.put("light.range", 0.f);
        uniforms.put("light.attenuation.constant", 1.f);
        uniforms.put("light.attenuation.linear", 0.f);
        uniforms.put("light.attenuation.quadratic", 0.f);
        uniforms.put("light.shadowSlot", 0);

        return uniforms;
    }

    /** The light of a spot light at a point at the angle from its direction (down the axis y) and at the distance 1 */
    private float factor(final float degrees, final boolean spot) {
        final var angle = Math.toRadians(degrees);
        final var uniforms = light(new Vector3f(), new Vector3f(0, -1, 0), spot);
        uniforms.put("position", new float[]{(float) Math.sin(angle), (float) -Math.cos(angle), 0.f});

        return harness.run(List.of("common/lights.glsl"), "uniform Light light; uniform vec3 position;",
                "outputColor = vec4(getLightFactor(light, position));", uniforms)[0];
    }

    @Test
    void testInsideTheInnerAngleTheLightIsFull() {
        assertEquals(1.f, factor(0.f, true), EPSILON, "On the axis");
        assertEquals(1.f, factor(INNER - 1.f, true), EPSILON, "Just inside the inner angle");
    }

    @Test
    void testBeyondTheOuterAngleThereIsNoLight() {
        assertEquals(0.f, factor(OUTER + 1.f, true), EPSILON);
        assertEquals(0.f, factor(120.f, true), EPSILON, "Behind the light");
        assertEquals(0.f, factor(180.f, true), EPSILON, "Opposite to the direction");
    }

    @Test
    void testBetweenTheAnglesTheLightFallsSmoothly() {
        var previous = 1.f;
        for (var degrees = INNER; degrees <= OUTER; degrees += 2.f) {
            final var value = factor(degrees, true);
            assertTrue(value <= previous + EPSILON, "It never grows farther from the axis: " + value + " after " + previous);
            previous = value;
        }

        final var middle = factor((INNER + OUTER) / 2.f, true);
        assertTrue(middle > 0.1f && middle < 0.9f, "Half way it is neither full nor gone: " + middle);
    }

    @Test
    void testPointLightIgnoresTheConeAndTheDirection() {
        for (final var degrees : new float[]{0.f, 50.f, 120.f, 180.f})
            assertEquals(1.f, factor(degrees, false), EPSILON, "At " + degrees + " degrees");
    }

    @Test
    void testConeIsAppliedOnTopOfTheAttenuation() {
        final var uniforms = light(new Vector3f(), new Vector3f(0, -1, 0), true);
        uniforms.put("light.attenuation.constant", 1.f);
        uniforms.put("light.attenuation.linear", 1.f);
        uniforms.put("position", new float[]{0.f, -3.f, 0.f});

        assertEquals(0.25f, harness.run(List.of("common/lights.glsl"), "uniform Light light; uniform vec3 position;",
                "outputColor = vec4(getLightFactor(light, position));", uniforms)[0], EPSILON, "1 / (1 + 3) on the axis");
    }

    /** Fills the layer of the spot light with the depth of a wall at the distance across its axis */
    private void fill(final Vector3f direction, final float distance) {
        final var position = frame.getLight(0).getPosition();
        final var clip = new Vector4f(position.x() + direction.x * distance, position.y() + direction.y * distance,
                position.z() + direction.z * distance, 1.f).mul(frame.getMatrix(0, 0));

        final var framebuffer = glGenFramebuffers();
        glBindFramebuffer(GL_FRAMEBUFFER, framebuffer);
        glFramebufferTextureLayer(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, texture, 0, 0);
        glDrawBuffer(GL_NONE);
        glReadBuffer(GL_NONE);
        assertEquals(GL_FRAMEBUFFER_COMPLETE, glCheckFramebufferStatus(GL_FRAMEBUFFER));

        glClearDepth(clip.z / clip.w * 0.5f + 0.5f);
        glClear(GL_DEPTH_BUFFER_BIT);
        glClearDepth(1.0);
        glBindFramebuffer(GL_FRAMEBUFFER, 0);
        glDeleteFramebuffers(framebuffer);
    }

    private float shadow(final Vector3f position, final Vector3f direction, final float[] point) {
        glActiveTexture(TextureUnit.PointShadow.getLocationID());
        glBindTexture(GL_TEXTURE_2D_ARRAY, texture);
        glActiveTexture(GL_TEXTURE0);

        final var numbers = new float[LAYERS * 16];
        for (int i = 0; i < LAYERS; i++)
            (i == 0 ? frame.getMatrix(0, 0) : new Matrix4f()).get(numbers, i * 16);

        final var uniforms = light(position, direction, true);
        uniforms.put("usePointShadows", 1);
        uniforms.put("texturePointShadow", TextureUnit.PointShadow.getId());
        uniforms.put("pointShadowMatrices", new ShaderHarness.Matrices(numbers));
        uniforms.put("position", point);
        uniforms.put("normal", new float[]{0, 1, 0});

        return harness.run(List.of("common/point-shadow.glsl"), "uniform Light light; uniform vec3 position; uniform vec3 normal;",
                "outputColor = vec4(getPointShadow(light, position, normal));", uniforms)[0];
    }

    private void assign(final Vector3f position, final Vector3f direction) {
        helper.assign(frame, List.of(TestObjects.spot("spot", position, direction, INNER, OUTER, RANGE, true)), new Vector3f());
    }

    @Test
    void testPointNearerThanTheWallIsLitAndFartherIsInShadow() {
        final var position = new Vector3f(1.f, 2.f, 3.f);
        final var direction = new Vector3f(0, 0, 1);
        assign(position, direction);
        fill(direction, STORED);

        assertEquals(1.f, shadow(position, direction, new float[]{1.f, 2.f, 3.f + STORED * 0.5f}), EPSILON, "In front of the wall");
        assertEquals(0.f, shadow(position, direction, new float[]{1.f, 2.f, 3.f + STORED * 2.f}), EPSILON, "Behind the wall");
    }

    @Test
    void testShadowIsReadFromTheFirstFaceOfTheSlotForAnyDirection() {
        // A point light would take the face by the axis, a spot light reads its one face whatever the direction is
        for (final var direction : new Vector3f[]{new Vector3f(1, 0, 0), new Vector3f(0, -1, 0), new Vector3f(1, 1, 1).normalize()}) {
            assign(new Vector3f(), direction);
            fill(direction, STORED);

            final var near = new Vector3f(direction).mul(STORED * 0.5f);
            final var behind = new Vector3f(direction).mul(STORED * 2.f);
            assertEquals(1.f, shadow(new Vector3f(), direction, new float[]{near.x, near.y, near.z}), EPSILON, "Lit, direction " + direction);
            assertEquals(0.f, shadow(new Vector3f(), direction, new float[]{behind.x, behind.y, behind.z}), EPSILON, "Shadowed, direction " + direction);
        }
    }

    @Test
    void testOutOfTheFaceThereIsNoShadow() {
        final var direction = new Vector3f(0, 0, 1);
        assign(new Vector3f(), direction);
        fill(direction, STORED);

        // Far to the side, well outside of the cone and of the view of the map
        assertEquals(1.f, shadow(new Vector3f(), direction, new float[]{100.f, 0.f, 20.f}), EPSILON);
    }
}
