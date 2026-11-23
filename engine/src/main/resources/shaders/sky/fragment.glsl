#version 330 core

#include "common/camera.glsl"
#include "common/flags.glsl"
#include "common/fog.glsl"

/* Vertex shader output */
in vec3 vertexPosition;

/* Skybox texture */
uniform samplerCube textureSkybox;

/* Output color */
out vec4 outputColor;

/*
 * The sky is infinitely far. Inside the fog layer a ray looking down or horizontally never leaves it, a ray looking up
 * leaves it at fog.height: dense fog hides the whole sky, light fog covers only the horizon.
 * Above the layer the sky is clear and the top of the layer is seen at the horizon, a narrow band smooths its edge.
 */
float getSkyFogFactor(vec3 direction) {
    if (cameraPosition.y >= fog.height)
        return 1.0 - smoothstep(-0.03, 0.03, direction.y);

    if (direction.y <= 0.0)
        return 1.0;

    return getFogFactor((fog.height - cameraPosition.y) / max(direction.y, 0.0001));
}

void main() {
    vec4 color = texture(textureSkybox, vertexPosition);
    if (!isSet(useFog)) {
        outputColor = color;
        return;
    }

    outputColor = vec4(mix(color.rgb, fog.color, getSkyFogFactor(normalize(vertexPosition))), color.a);
}