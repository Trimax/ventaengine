#version 330 core

/***
 * Definitions
 ***/

const int MAX_LIGHTS = 64;

/***
 * Structures
 ***/

struct Attenuation {
    float constant;
    float linear;
    float quadratic;
};

struct Light {
    vec3 position;
    vec3 color;
    float intensity;
    Attenuation attenuation;
    int castShadows;
    int enabled;
};

struct DirectionalLight {
    vec3 color;
    vec3 direction;
    float intensity;
};

struct Fog {
    vec3 color;
    float minimalDistance;
    float maximalDistance;
    float height;
};

/***
 * Input variables and uniforms
 ***/

/* Vertex attributes */
layout(location = 0) in vec2 position;
layout(location = 1) in vec4 color;
layout(location = 2) in mat4 matrixModel;

/* Matrices */
uniform mat4 matrixViewProjection;  // Multiplied View & Projections matrices (built based on the window & camera parameters)

/* Feature flags */
uniform int useLighting;
uniform int useDirectionalLight;
uniform int useFog;

/* Lighting */
uniform DirectionalLight directionalLight;
uniform Light lights[MAX_LIGHTS];
uniform vec3 ambientLight;
uniform int lightCount;

/* Fog */
uniform Fog fog;

/* Camera */
uniform vec3 cameraPosition;

/* Parameters going to fragment shader */
out vec2 vertexTextureCoordinates;
out vec4 vertexColor;
out vec3 vertexLight;
out float vertexFog;

/***
 * Functions
 ***/

/* Checks if flag is set (bools are not supported by macOS Radeon cards */
bool isSet(int value) {
    return value != 0;
}

float getAttenuation(Attenuation attenuation, float distance) {
    // An all-zero attenuation would divide by zero
    return 1.0 / max(attenuation.constant + attenuation.linear * distance + attenuation.quadratic * distance * distance, 0.0001);
}

/* A particle has no normal, it gets all the light reaching its center, so smoke is lit from any side */
vec3 computeLighting(vec3 center) {
    if (!isSet(useLighting))
        return vec3(1.0);

    vec3 result = ambientLight;
    if (isSet(useDirectionalLight))
        result += directionalLight.color * directionalLight.intensity;

    for (int i = 0; i < lightCount; i++)
        if (lights[i].enabled != 0)
            result += lights[i].color * lights[i].intensity * getAttenuation(lights[i].attenuation, length(lights[i].position - center));

    return result;
}

/* Part of the way between two points that lies in the fog layer (below fog.height) */
float getFogDistance(vec3 from, vec3 to) {
    float distance = length(to - from);
    float lower = min(from.y, to.y);
    float upper = max(from.y, to.y);

    if (upper <= fog.height)
        return distance;

    if (lower >= fog.height)
        return 0.0;

    return distance * (fog.height - lower) / (upper - lower);
}

/* Amount of fog color: 0 is no fog, 1 is the fog color only */
float getFogFactor(float distance) {
    float range = max(fog.maximalDistance - fog.minimalDistance, 0.0001);
    return clamp((distance - fog.minimalDistance) / range, 0.0, 1.0);
}

float computeFog(vec3 center) {
    if (!isSet(useFog))
        return 0.0;

    return getFogFactor(getFogDistance(cameraPosition, center));
}

void main() {
    /* The whole particle is lit and fogged as its center, so it stays uniform and costs one evaluation per vertex */
    vec3 center = matrixModel[3].xyz;

    vertexColor = color;
    vertexLight = computeLighting(center);
    vertexFog = computeFog(center);
    vertexTextureCoordinates = (position + 0.5);
    gl_Position = matrixViewProjection * matrixModel * vec4(position, 0.0, 1.0);
}
