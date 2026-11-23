#version 330 core

#include "common/camera.glsl"
#include "common/flags.glsl"
#include "common/fog.glsl"
#include "common/lights.glsl"

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

/* Parameters going to fragment shader */
out vec2 vertexTextureCoordinates;
out vec4 vertexColor;
out vec3 vertexLight;
out float vertexFog;

/***
 * Functions
 ***/

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

float computeFog(vec3 center) {
    if (!isSet(useFog))
        return 0.0;

    return getFogAmount(cameraPosition, center);
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
