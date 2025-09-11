#version 330 core

#define MAX_WAVES 16

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

/* Waves */
uniform Wave waves[MAX_WAVES];
uniform int waveCount;

/* Time */
uniform float timeElapsed;

/* Parameters going to fragment shader */
out vec3 vertexNormal;
out vec3 vertexPosition;
out vec2 vertexTextureCoordinates;

vec3 gerstnerWave(vec3 pos, Wave wave, out vec3 outNormal) {
    float k = 2.0 * 3.14159 / wave.l;
    float f = k * dot(wave.direction, pos.xz) - wave.speed * timeElapsed;

    float a = wave.amplitude;
    float q = wave.steepness;

    pos.x += q * a * wave.direction.x * cos(f);
    pos.z += q * a * wave.direction.y * cos(f);
    pos.y += a * sin(f);

    outNormal = normalize(vec3(-wave.direction.x * k * a * cos(f),
                          1.0 - q * k * a * sin(f),
                          -wave.direction.y * k * a * cos(f)));
    return pos;
}

void main() {
    vec3 transformedPosition = position;
    vec3 transformedNormal = vec3(0, 1, 0);
    for (int i = 0; i < waveCount; i++)
        transformedPosition = gerstnerWave(transformedPosition, waves[i], transformedNormal);

    vertexNormal = transformedNormal;
    vertexPosition = transformedPosition;
    vertexTextureCoordinates = textureCoordinates;

    gl_Position = matrixViewProjection * matrixModel * vec4(transformedPosition, 1.0);
}