#version 330 core

struct Fog {
    vec3 color;
    float minimalDistance;
    float maximalDistance;
    float height;
};

/* Vertex shader output */
in vec3 vertexPosition;

/* Skybox texture */
uniform samplerCube textureSkybox;

/* Fog */
uniform Fog fog;

/* Camera */
uniform vec3 cameraPosition;

/* Feature flags */
uniform int useFog;

/* Output color */
out vec4 outputColor;

/* Checks if flag is set (bools are not supported by macOS Radeon cards */
bool isSet(int value) {
    return value != 0;
}

/* Amount of fog color: 0 is no fog, 1 is the fog color only */
float getFogFactor(float distance) {
    float range = max(fog.maximalDistance - fog.minimalDistance, 0.0001);
    return clamp((distance - fog.minimalDistance) / range, 0.0, 1.0);
}

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