package io.github.trimax.venta.engine.shaders;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * The physically based shading (common/pbr.glsl) on the GPU: the terms of the BRDF are compared with the formulas
 * in Java, and the whole BRDF is checked not to reflect more light than it gets.
 */
final class PbrShaderTest {
    private static final float EPSILON = 1e-4f;
    private static final float ROUGHNESS_MINIMAL = 0.045f;
    private static final List<String> INCLUDES = List.of("common/pbr.glsl");

    private static ShaderHarness harness;

    @BeforeAll
    static void context() {
        GLTestContext.require();
        harness = new ShaderHarness();
    }

    @Test
    void testRoughnessIsClampedAwayFromAMirror() {
        assertEquals(ROUGHNESS_MINIMAL, scalar("getPbrRoughness(value)", 0.f), EPSILON, "A mirror");
        assertEquals(ROUGHNESS_MINIMAL, scalar("getPbrRoughness(value)", -1.f), EPSILON, "Less than a mirror");
        assertEquals(0.5f, scalar("getPbrRoughness(value)", 0.5f), EPSILON, "A normal roughness stays");
        assertEquals(1.f, scalar("getPbrRoughness(value)", 3.f), EPSILON, "Rougher than possible");
    }

    @Test
    void testDistributionMatchesGgx() {
        for (final var roughness : new float[]{0.1f, 0.3f, 0.6f, 1.f})
            for (final var cosine : new float[]{0.2f, 0.7f, 0.95f, 1.f}) {
                final var expected = distribution(cosine, roughness);

                // The denominator subtracts nearly equal numbers at the peak: a float keeps about 3 digits of it there
                assertEquals(expected, run("outputColor = vec4(distributionGGX(" + cosine + ", " + roughness + "));")[0],
                        Math.max(EPSILON, expected * 2e-3f), "D(" + cosine + ", " + roughness + ")");
            }
    }

    @Test
    void testDistributionIsNormalized() {
        // The projected area of the microfacets of a surface is its area: the integral of D * cos over the hemisphere is 1
        for (final var roughness : new float[]{0.3f, 0.6f, 1.f}) {
            final var steps = 20000;
            var integral = 0.0;
            for (int i = 0; i < steps; i++) {
                final var angle = (i + 0.5) / steps * Math.PI / 2.0;
                integral += distribution((float) Math.cos(angle), roughness) * Math.cos(angle) * Math.sin(angle) * 2.0 * Math.PI * (Math.PI / 2.0 / steps);
            }

            assertEquals(1.0, integral, 2e-3, "The integral of the distribution of the roughness " + roughness);
        }
    }

    @Test
    void testGeometryMatchesSmith() {
        for (final var roughness : new float[]{0.2f, 0.5f, 1.f})
            for (final var view : new float[]{0.1f, 0.5f, 1.f})
                for (final var light : new float[]{0.1f, 0.6f, 1.f}) {
                    final var k = (roughness + 1.f) * (roughness + 1.f) / 8.f;
                    final var expected = view / (view * (1.f - k) + k) * (light / (light * (1.f - k) + k));

                    assertEquals(expected, run("outputColor = vec4(geometrySmith(" + view + ", " + light + ", " + roughness + "));")[0], EPSILON,
                            "G(" + view + ", " + light + ", " + roughness + ")");
                }
    }

    @Test
    void testGeometryDoesNotShadowAFacingSurface() {
        for (final var roughness : new float[]{0.1f, 0.5f, 1.f})
            assertEquals(1.f, run("outputColor = vec4(geometrySmith(1.0, 1.0, " + roughness + "));")[0], EPSILON, "Seen and lit straight on");
    }

    @Test
    void testFresnelGrowsToOneAtGrazingAngles() {
        final var reflectance = "vec3(0.04, 0.5, 0.9)";

        assertArrayEquals(new float[]{0.04f, 0.5f, 0.9f}, rgb("fresnelSchlick(1.0, " + reflectance + ")"), EPSILON, "Straight on it is the reflectance");
        assertArrayEquals(new float[]{1.f, 1.f, 1.f}, rgb("fresnelSchlick(0.0, " + reflectance + ")"), EPSILON, "At the grazing angle everything is reflected");

        final var half = rgb("fresnelSchlick(0.5, " + reflectance + ")");
        final var factor = (float) Math.pow(0.5, 5);
        assertArrayEquals(new float[]{0.04f + 0.96f * factor, 0.5f + 0.5f * factor, 0.9f + 0.1f * factor}, half, EPSILON, "In between");
    }

    @Test
    void testFresnelOfRoughSurfaceIsNotBrighterThanAMirrorOne() {
        final var mirror = rgb("fresnelSchlickRoughness(0.1, vec3(0.04), 0.0)");
        final var rough = rgb("fresnelSchlickRoughness(0.1, vec3(0.04), 1.0)");

        assertTrue(rough[0] < mirror[0], "A rough surface reflects less at a grazing angle: " + rough[0] + " < " + mirror[0]);
        assertEquals(0.04f, rgb("fresnelSchlickRoughness(1.0, vec3(0.04), 1.0)")[0], EPSILON, "Straight on there is no difference");
    }

    @Test
    void testReflectanceOfNonMetalsIsFourPercentAndOfMetalsIsTheirColor() {
        assertArrayEquals(new float[]{0.04f, 0.04f, 0.04f}, rgb("getReflectance(vec3(0.9, 0.2, 0.1), 0.0)"), EPSILON, "A non-metal");
        assertArrayEquals(new float[]{0.9f, 0.2f, 0.1f}, rgb("getReflectance(vec3(0.9, 0.2, 0.1), 1.0)"), EPSILON, "A metal");
    }

    @Test
    void testLightFromBehindTheSurfaceGivesNothing() {
        assertArrayEquals(new float[]{0.f, 0.f, 0.f}, rgb(light("vec3(0, 0, 1)", "vec3(0, 0, 1)", "vec3(0, 0, -1)", "vec3(1.0)", 0.f, 0.5f)), EPSILON);
        assertArrayEquals(new float[]{0.f, 0.f, 0.f}, rgb(light("vec3(0, 0, 1)", "vec3(0, 0, 1)", "vec3(1, 0, 0)", "vec3(1.0)", 0.f, 0.5f)), EPSILON, "A grazing light");
    }

    @Test
    void testLightMatchesTheCookTorranceFormula() {
        final var normal = new float[]{0.f, 0.f, 1.f};
        final var view = normalize(0.3f, 0.2f, 0.9f);
        final var toLight = normalize(-0.4f, 0.1f, 0.8f);
        final var albedo = new float[]{0.8f, 0.5f, 0.2f};

        for (final var metalness : new float[]{0.f, 0.5f, 1.f})
            for (final var roughness : new float[]{0.2f, 0.6f, 1.f}) {
                final var expected = reference(normal, view, toLight, albedo, metalness, roughness);
                final var actual = rgb(light(vec(normal), vec(view), vec(toLight), vec(albedo), metalness, roughness));

                assertArrayEquals(expected, actual, 2e-4f, "metalness " + metalness + ", roughness " + roughness);
            }
    }

    @Test
    void testRadianceScalesTheResult() {
        final var one = rgb(light("vec3(0, 0, 1)", "vec3(0, 0, 1)", "normalize(vec3(0.3, 0.1, 1.0))", "vec3(0.7)", 0.f, 0.5f, "vec3(1.0)"));
        final var two = rgb(light("vec3(0, 0, 1)", "vec3(0, 0, 1)", "normalize(vec3(0.3, 0.1, 1.0))", "vec3(0.7)", 0.f, 0.5f, "vec3(2.0, 0.5, 1.0)"));

        assertEquals(one[0] * 2.f, two[0], EPSILON, "Twice the radiance");
        assertEquals(one[1] * 0.5f, two[1], EPSILON, "Half the green radiance");
        assertEquals(one[2], two[2], EPSILON, "The same blue radiance");
    }

    @Test
    void testMetalsDoNotDiffuse() {
        // The fresnel of a pure red metal seen and lit straight on has no green and blue, so neither has the light it reflects
        final var result = rgb(light("vec3(0, 0, 1)", "vec3(0, 0, 1)", "vec3(0, 0, 1)", "vec3(1.0, 0.0, 0.0)", 1.f, 1.f));

        assertTrue(result[0] > 0.f, "The metal is lit");
        assertEquals(0.f, result[1], EPSILON, "No green");
        assertEquals(0.f, result[2], EPSILON, "No blue");
    }

    @Test
    void testRoughSurfaceHasWiderAndWeakerHighlight() {
        // Looking at the mirror direction of a light: a smooth surface shines much brighter than a rough one
        final var toLight = "normalize(vec3(0.5, 0.0, 0.8))";
        final var view = "normalize(vec3(-0.5, 0.0, 0.8))";

        final var smooth = rgb(light("vec3(0, 0, 1)", view, toLight, "vec3(1.0)", 0.f, 0.2f))[0];
        final var rough = rgb(light("vec3(0, 0, 1)", view, toLight, "vec3(1.0)", 0.f, 0.9f))[0];

        assertTrue(smooth > rough * 2.f, "The highlight of the smooth surface " + smooth + " must be much brighter than " + rough);
    }

    /**
     * Energy conservation: the part of the light that a surface reflects over the hemisphere (the directional albedo)
     * must not exceed 1, or it would shine brighter than its lights. The integral runs on the GPU over the hemisphere
     * with the cosine weighted samples.
     */
    @Test
    void testSurfaceDoesNotReflectMoreLightThanItGets() {
        final var declarations = """
                uniform float viewCosine;
                uniform float roughness;
                uniform float metalness;
                uniform vec3 albedo;
                """;
        final var code = """
                vec3 N = vec3(0.0, 0.0, 1.0);
                vec3 V = vec3(sqrt(1.0 - viewCosine * viewCosine), 0.0, viewCosine);
                vec3 sum = vec3(0.0);
                const int COUNT = 4096;
                for (int i = 0; i < COUNT; i++) {
                    vec3 L = sampleHemisphereCosine(hammersley(uint(i), uint(COUNT)), N);
                    // pbrLight(...) = pi * f * cos and the weight of a cosine sample is pi / cos
                    sum += pbrLight(N, V, L, albedo, metalness, roughness, vec3(1.0)) / L.z;
                }
                outputColor = vec4(sum / float(COUNT), 1.0);
                """;

        for (final var metalness : new float[]{0.f, 1.f})
            for (final var roughness : new float[]{0.3f, 0.6f, 1.f})
                for (final var viewCosine : new float[]{0.2f, 0.6f, 1.f}) {
                    final var albedo = new float[]{1.f, 1.f, 1.f};
                    final var result = harness.run(List.of("common/ibl.glsl"), declarations, code,
                            Map.of("viewCosine", viewCosine, "roughness", roughness, "metalness", metalness, "albedo", albedo));
                    final var message = "metalness " + metalness + ", roughness " + roughness + ", view cosine " + viewCosine + ": " + result[0];

                    // The Smith term with the Schlick approximation and 4096 samples of a sharp lobe are not exact: a few percent are allowed
                    assertTrue(result[0] <= 1.05f, "A white surface reflects at most all the light, got " + message);
                    assertTrue(result[0] > 0.3f, "A white surface reflects most of the light, got " + message);
                }
    }

    private static float distribution(final float cosine, final float roughness) {
        final var a2 = Math.pow(roughness, 4);
        final var denominator = cosine * cosine * (a2 - 1.0) + 1.0;

        return (float) (a2 / (Math.PI * denominator * denominator));
    }

    private static float[] reference(final float[] normal, final float[] view, final float[] light, final float[] albedo,
                                     final float metalness, final float roughness) {
        final var half = normalize(view[0] + light[0], view[1] + light[1], view[2] + light[2]);
        final var normalDotLight = dot(normal, light);
        final var normalDotView = Math.max(dot(normal, view), 0.0001f);
        final var normalDotHalf = Math.max(dot(normal, half), 0.f);
        final var viewDotHalf = Math.max(dot(view, half), 0.f);

        final var k = (roughness + 1.f) * (roughness + 1.f) / 8.f;
        final var geometry = normalDotView / (normalDotView * (1.f - k) + k) * (normalDotLight / (normalDotLight * (1.f - k) + k));
        final var d = distribution(normalDotHalf, roughness);
        final var fresnelFactor = (float) Math.pow(1.f - viewDotHalf, 5);

        final var result = new float[3];
        for (int i = 0; i < 3; i++) {
            final var reflectance = 0.04f * (1.f - metalness) + albedo[i] * metalness;
            final var fresnel = reflectance + (1.f - reflectance) * fresnelFactor;
            final var specular = d * geometry * fresnel / Math.max(4.f * normalDotView * normalDotLight, 0.001f);
            final var diffuse = (1.f - fresnel) * (1.f - metalness) * albedo[i];

            result[i] = (diffuse + (float) Math.PI * specular) * normalDotLight;
        }

        return result;
    }

    private static float dot(final float[] a, final float[] b) {
        return a[0] * b[0] + a[1] * b[1] + a[2] * b[2];
    }

    private static float[] normalize(final float x, final float y, final float z) {
        final var length = (float) Math.sqrt(x * x + y * y + z * z);

        return new float[]{x / length, y / length, z / length};
    }

    private static String vec(final float[] values) {
        return String.format(Locale.ROOT, "vec3(%.7f, %.7f, %.7f)", values[0], values[1], values[2]);
    }

    private static String light(final String normal, final String view, final String toLight, final String albedo, final float metalness, final float roughness) {
        return light(normal, view, toLight, albedo, metalness, roughness, "vec3(1.0)");
    }

    private static String light(final String normal, final String view, final String toLight, final String albedo,
                                final float metalness, final float roughness, final String radiance) {
        return String.format(Locale.ROOT, "pbrLight(%s, %s, %s, %s, %.7f, %.7f, %s)", normal, view, toLight, albedo, metalness, roughness, radiance);
    }

    private static float[] rgb(final String expression) {
        final var color = run("outputColor = vec4(" + expression + ", 1.0);");

        return new float[]{color[0], color[1], color[2]};
    }

    private static float scalar(final String expression, final float value) {
        return harness.run(INCLUDES, "uniform float value;", "outputColor = vec4(" + expression + ");", Map.of("value", value))[0];
    }

    private static float[] run(final String code) {
        return harness.run(INCLUDES, "", code, Map.of());
    }
}
