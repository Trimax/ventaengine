#version 330 core

#include "common/swash.glsl"
#include "common/water-waves.glsl"

#define MAX_WAVES 16

#define PI 3.14159

/***
 * Structures
 ***/

struct Wave {
    vec2 direction;
    float amplitude;
    float l;
    float steepness;
    float speed;
};

/* Vertex attributes */
layout(location = 0) in vec3 position;
layout(location = 1) in vec2 textureCoordinates;

/* Matrices */
uniform mat4 matrixViewProjection;
uniform mat4 matrixModel;
uniform mat3 matrixNormal;

/* Waves */
uniform Wave waves[MAX_WAVES];
uniform int waveCount;

/* Time */
uniform float timeElapsed;

/* The waves fade out at this distance from the camera (0 keeps them), and the water runs up the shore and back by the swash */
uniform vec3 cameraPosition;
uniform float waveFadeDistance;
uniform float swashAmplitude;
uniform float swashPeriod;

/* Parameters going to fragment shader (world space) */
out vec3 vertexNormal;
out vec3 vertexPosition;
out float vertexHeight;
out float vertexTimeElapsed;
out vec2 vertexTextureCoordinates;

/*
 * Sum of Gerstner waves: every wave is evaluated at the original grid position,
 * the displacements and the partial derivatives of the surface are accumulated.
 */
void main() {
    vec3 displaced = position;
    vec3 normal = vec3(0.0, 1.0, 0.0);

    // The waves are as big as the distance to the camera lets them be
    vec3 waterPosition = (matrixModel * vec4(position, 1.0)).xyz;
    float fade = getWaveFade(distance(waterPosition.xz, cameraPosition.xz), waveFadeDistance);

    for (int i = 0; i < waveCount; i++) {
        Wave wave = waves[i];

        float k = 2.0 * PI / wave.l;
        float f = k * dot(wave.direction, position.xz) - wave.speed * timeElapsed;
        float a = wave.amplitude * fade;
        float q = wave.steepness;

        displaced.x += q * a * wave.direction.x * cos(f);
        displaced.z += q * a * wave.direction.y * cos(f);
        displaced.y += a * sin(f);

        normal.x -= wave.direction.x * k * a * cos(f);
        normal.z -= wave.direction.y * k * a * cos(f);
        normal.y -= q * k * a * sin(f);
    }

    vertexHeight = displaced.y - position.y;

    // The water runs up the shore and back: all of it rises and falls, the colors by the height of the waves are not changed by it
    displaced.y += getSwash(swashAmplitude, swashPeriod, timeElapsed);

    vec4 worldPosition = matrixModel * vec4(displaced, 1.0);

    vertexPosition = worldPosition.xyz;
    vertexNormal = normalize(matrixNormal * normalize(normal));
    vertexTimeElapsed = timeElapsed;
    vertexTextureCoordinates = textureCoordinates;

    gl_Position = matrixViewProjection * worldPosition;
}
