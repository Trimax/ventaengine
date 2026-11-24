#version 330 core

#include "common/camera.glsl"
#include "common/flags.glsl"
#include "common/fog.glsl"

/* Vertex shader output */
in vec3 vertexPosition;

/* Skybox texture */
uniform samplerCube textureSkybox;

/* The disc of the sun: the direction to the sun, its color (the light times how much brighter the disc is) and the cosines of its radius and of the radius where the edge starts */
uniform int useSunDisc;
uniform vec3 sunDirection;
uniform vec3 sunColor;
uniform vec2 sunSize;

/* Output color */
out vec4 outputColor;

/*
 * The sky is infinitely far. Inside the fog layer a ray looking down or horizontally never leaves it, a ray looking up
 * leaves it at fog.height: dense fog hides the whole sky, light fog covers only the horizon.
 * Above the layer the sky is clear and the top of the layer is seen at the horizon, a narrow band smooths its edge.
 */
float getSkyFogFactor(vec3 direction) {
    // The exponential fog on a ray that goes up and never ends: its thickness is the density at the camera over the fall
    // of the density along the ray. A ray that doesn't go up stays in the fog, near the horizon the fog is thick
    if (isFogExponential()) {
        if (direction.y <= 0.0)
            return 1.0;

        float start = fog.density * exp(clamp(-fog.falloff * (cameraPosition.y - fog.baseHeight), -FOG_MAXIMAL_EXPONENT, FOG_MAXIMAL_EXPONENT));
        return 1.0 - exp(-start / (fog.falloff * direction.y));
    }

    if (cameraPosition.y >= fog.height)
        return 1.0 - smoothstep(-0.03, 0.03, direction.y);

    if (direction.y <= 0.0)
        return 1.0;

    return getFogFactor((fog.height - cameraPosition.y) / max(direction.y, 0.0001));
}

/* The disc of the sun is far brighter than the sky: in HDR it glows with the bloom */
vec3 getSunDisc(vec3 direction) {
    if (!isSet(useSunDisc))
        return vec3(0.0);

    return sunColor * smoothstep(sunSize.x, sunSize.y, dot(direction, sunDirection));
}

void main() {
    vec4 color = texture(textureSkybox, vertexPosition);
    color.rgb += getSunDisc(normalize(vertexPosition));

    if (!isSet(useFog)) {
        outputColor = color;
        return;
    }

    vec3 direction = normalize(vertexPosition);
    outputColor = vec4(mix(color.rgb, getFogColor(direction), getSkyFogFactor(direction)), color.a);
}