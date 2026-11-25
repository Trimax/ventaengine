#version 330 core

#include "common/camera.glsl"
#include "common/flags.glsl"
#include "common/fog.glsl"
#include "common/lights.glsl"
#include "common/noise.glsl"
#include "common/point-shadow.glsl"
#include "common/shadow.glsl"
#include "common/water-depth.glsl"
#include "common/water-reflection.glsl"

/***
 * Definitions
 ***/

/* Water */
const int MAX_NOISES = 16;

/***
 * Structures
 ***/

struct Material {
    vec3 colorSurface;
    vec3 colorDepth;
    vec3 colorPeak;
    float metalness;
    float opacity;
    float depthDistance;
    float refraction;
};

struct Noise {
    float scale;
    float speed;
    float strength;
    float offset;
};

struct Foam {
    float threshold;
    float intensity;
    float shoreWidth;
    float shoreIntensity;
    float bands;
};

/***
 * Input variables and uniforms
 ***/

/* Vertex shader output */
in vec3 vertexNormal;
in vec3 vertexPosition;
in float vertexHeight;
in vec2 vertexTextureCoordinates;
in float vertexTimeElapsed;

/* Textures */
uniform samplerCube textureSkybox;

/* The opaque scene behind the water (its depth and color), when it is resolved for the water to read */
uniform sampler2D textureSceneColor;
uniform sampler2D textureSceneDepth;
uniform int useSceneTextures;
uniform mat4 matrixViewProjectionInverse;

/* The reflection of the scene (drawn as the mirror image of the camera sees it), when this water has it */
uniform sampler2D textureReflection;
uniform int useReflection;
uniform mat4 matrixViewProjectionReflection;

/* Feature flags */
uniform int useTextureSkybox;
uniform int useMaterial;

/* Noise */
uniform Noise noises[MAX_NOISES];
uniform int noiseCount;

/* Material */
uniform Material material;
uniform Foam foam;

/* Waves */
uniform float waveAmplitude;

/* Time */
uniform float timeElapsed;

/* Output color */
out vec4 outputColor;

/***
 * Common functions
 ***/

/* Height of the wave at this point relative to the calm surface, from 0 (the lowest) to 1 (the crest); a calm surface is in the middle */
float getHeightFactor() {
    float amplitude = max(waveAmplitude, 0.0001);
    return clamp((vertexHeight + amplitude) / (2.0 * amplitude), 0.0, 1.0);
}

vec3 calculateColor() {
    float heightFactor = getHeightFactor();
    vec3 waterColor = mix(material.colorDepth, material.colorSurface, heightFactor);

    return mix(waterColor, material.colorPeak, smoothstep(foam.threshold, 1.0, heightFactor) * foam.intensity);
}

/*
 * Ripples: every noise layer is a height field over the surface, its world space gradient tilts the normal.
 * The gradient doesn't depend on the screen resolution or the distance, unlike screen space derivatives.
 */
vec3 calculateNormal() {
    const float STEP = 0.05;
    vec3 normal = normalize(vertexNormal);

    for (int i = 0; i < noiseCount; i++) {
        vec2 uv = vertexPosition.xz * noises[i].scale + timeElapsed * noises[i].speed + noises[i].offset;

        float level = noise(uv);
        vec2 gradient = vec2(noise(uv + vec2(STEP, 0.0)) - level, noise(uv + vec2(0.0, STEP)) - level) / STEP * noises[i].scale;

        // Far away a pixel covers several ripples: they fade out instead of flickering (the surface gets smooth like a mirror)
        float detail = 1.0 - smoothstep(0.25, 1.0, length(fwidth(uv)));

        normal.xz -= gradient * noises[i].strength * detail;
    }

    // Far away the waves are smaller than a pixel too: the surface is seen as a smooth mirror reflecting the horizon
    float footprint = length(fwidth(vertexPosition.xz));
    return normalize(mix(normalize(normal), vec3(0.0, 1.0, 0.0), smoothstep(0.05, 0.5, footprint)));
}

/***
 * Lighting functions
 ***/

/* Computes light: diffuse light takes the water color, specular highlights keep the light color */
vec3 calculateLight(Light light, vec3 baseColor, vec3 normal, vec3 cameraDirection) {
    if (light.enabled == 0)
        return vec3(0.0);

    vec3 lightDirection = normalize(light.position - vertexPosition);
    float attenuation = getLightFactor(light, vertexPosition) * getPointShadow(light, vertexPosition, normalize(vertexNormal));

    float diff = max(dot(normal, lightDirection), 0.0);
    vec3 R = reflect(-lightDirection, normal);
    float spec = pow(max(dot(R, cameraDirection), 0.0), 32.0);

    return (diff * baseColor + spec * 0.3) * light.color * light.intensity * attenuation;
}

/* Computes directional light */
vec3 computeDirectionalLight(vec3 baseColor, vec3 normal, vec3 cameraDirection) {
    if (!isSet(useDirectionalLight))
        return vec3(0.0);

    vec3 lightDirection = normalize(-directionalLight.direction);

    float diff = max(dot(normal, lightDirection), 0.0);
    vec3 R = reflect(-lightDirection, normal);

    // The sun is small and bright: a sharp glint on the ripples
    float spec = pow(max(dot(R, cameraDirection), 0.0), 256.0) * 2.0 + pow(max(dot(R, cameraDirection), 0.0), 24.0) * 0.1;

    return (diff * baseColor + spec) * directionalLight.color * directionalLight.intensity * getSunShadow(vertexPosition, normalize(vertexNormal));
}

vec3 computeLighting(vec3 baseColor, vec3 normal, vec3 cameraDirection) {
    vec3 result = ambientLight * baseColor + computeDirectionalLight(baseColor, normal, cameraDirection);

    for (int i = 0; i < lightCount; i++)
        result += calculateLight(lights[i], baseColor, normal, cameraDirection);

    return result;
}

/*
 * The world position of the opaque scene at a point of the screen: the position of what lies under the water there. The sky
 * (the far plane) is infinitely far.
 */
vec3 getScenePosition(vec2 coordinates, float depth) {
    vec4 position = matrixViewProjectionInverse * vec4(vec3(coordinates, depth) * 2.0 - 1.0, 1.0);
    return position.xyz / position.w;
}

/* How much water the view goes through at a point of the screen, up to the opaque scene behind it */
float getThickness(vec2 coordinates) {
    float depth = texture(textureSceneDepth, coordinates).r;

    // Nothing behind the water: the sky, or the depth of the water is not known
    if (depth >= 1.0)
        return material.depthDistance * 10.0;

    // What stands in front of this point of the water can not be seen under it
    if (depth <= gl_FragCoord.z)
        return 0.0;

    return distance(getScenePosition(coordinates, depth), vertexPosition);
}

/* The foam scatters the light of the sky and of the sun that passes through it, so it is never dark: this much of its color is added to the light it gets */
const float FOAM_GLOW = 0.35;

/* The foam of the crests of the waves is the whitest: it scatters the light too, and more of it than the foam of the shore */
const float CREST_GLOW = 0.8;

/* How deep (the height of the water over the ground) the water becomes opaque, at the edge of a shore */
const float SHORE_EDGE = 0.15;

/* The height of the water over the opaque scene under it at a point of the screen: large where there is nothing under it */
float getVerticalDepth(vec2 coordinates) {
    float depth = texture(textureSceneDepth, coordinates).r;
    if (depth >= 1.0)
        return 1000.0;

    return vertexPosition.y - getScenePosition(coordinates, depth).y;
}

const float WATER_REFLECTANCE = 0.02;

vec3 applyReflections(vec3 color, vec3 cameraDirection, vec3 normal) {
    // The sun is reflected by the specular light, the environment is the sky and, where the water has it, the scene
    if (!isSet(useTextureSkybox) && !isSet(useReflection))
        return color;

    vec3 direction = reflect(-cameraDirection, normal);
    direction.y = abs(direction.y);
    vec3 environmentColor = isSet(useTextureSkybox) ? texture(textureSkybox, direction).rgb : color;

    if (isSet(useReflection)) {
        vec3 reflection = getReflectionCoordinates(matrixViewProjectionReflection, vertexPosition, normal, material.refraction * 0.5);
        environmentColor = mix(environmentColor, texture(textureReflection, reflection.xy).rgb, reflection.z);
    }

    // Schlick fresnel with the reflectance of water (2% looking straight down, all of it at grazing angles)
    float cosine = clamp(dot(normal, cameraDirection), 0.0, 1.0);
    float fresnel = WATER_REFLECTANCE + (1.0 - WATER_REFLECTANCE) * pow(1.0 - cosine, 5.0);

    return mix(color, environmentColor, fresnel * material.metalness);
}

/***
 * Fog
 ***/

vec3 applyFog(vec3 color) {
    if (!isSet(useFog))
        return color;

    return mix(color, getFogColor(cameraPosition, vertexPosition), getFogAmount(cameraPosition, vertexPosition));
}

void main() {
    vec3 cameraDirection = normalize(cameraPosition - vertexPosition);
    vec3 normal = calculateNormal();

    vec3 colorWithLighting = computeLighting(calculateColor(), normal, cameraDirection);
    float opacity = material.opacity;

    // How much water the view goes through: the foam of the crests is out at sea, the shore has the foam of its own
    float seaThickness = 1000.0;

    if (isSet(useSceneTextures)) {
        // At the very edge of a shore the water is thin: the ground shows through it
        opacity *= getShoreFade(getVerticalDepth(gl_FragCoord.xy / vec2(textureSize(textureSceneDepth, 0))), SHORE_EDGE);

        // The bottom is seen through the ripples, and the deeper the water, the less of it
        vec2 coordinates = gl_FragCoord.xy / vec2(textureSize(textureSceneDepth, 0));
        float thickness = getThickness(coordinates);

        vec2 refracted = getRefractedCoordinates(coordinates, normal, material.refraction, thickness, material.depthDistance);

        // What stands out of the water in front of the shifted point must not be dragged into the water
        if (texture(textureSceneDepth, refracted).r < gl_FragCoord.z)
            refracted = coordinates;
        else
            thickness = getThickness(refracted);

        seaThickness = thickness;

        vec3 bottom = texture(textureSceneColor, refracted).rgb;
        colorWithLighting = mix(colorWithLighting, bottom, getWaterTransmittance(thickness, material.depthDistance));

        // The foam of the shore, lit as a surface of its color
        float pattern = noise(vertexPosition.xz * 0.9 + timeElapsed * 0.25) * 0.6 + noise(vertexPosition.xz * 2.3 - timeElapsed * 0.4) * 0.4;
        float shore = max(getShoreFoam(thickness, foam.shoreWidth, foam.shoreIntensity, pattern),
                getFoamBands(thickness, foam.shoreWidth, foam.bands, timeElapsed) * mix(0.55, 1.0, pattern));
        if (shore > 0.0)
            colorWithLighting = mix(colorWithLighting, computeLighting(material.colorPeak, normal, cameraDirection) + material.colorPeak * FOAM_GLOW, shore);
    }

    // The crests of the waves are white with the foam that scatters the light, so they glow against the sun as well. The foam lies in
    // soft streaks along the waves (the noise is stretched along them), not on the whole crest and not where the water is shallow
    float patches = noise(vertexPosition.xz * vec2(0.12, 0.9) + timeElapsed * vec2(0.02, 0.15)) * 0.6 + noise(vertexPosition.xz * vec2(0.4, 2.6) - timeElapsed * vec2(0.03, 0.25)) * 0.4;
    float deep = smoothstep(foam.shoreWidth * 2.0, foam.shoreWidth * 6.0, seaThickness);
    float crest = smoothstep(foam.threshold, 1.0, getHeightFactor()) * smoothstep(0.3, 0.72, patches) * deep * foam.intensity;
    colorWithLighting += material.colorPeak * crest * CREST_GLOW;

    vec3 colorWithReflections = applyReflections(colorWithLighting, cameraDirection, normal);

    outputColor = vec4(applyFog(max(colorWithReflections, vec3(0.0))), opacity);
}
