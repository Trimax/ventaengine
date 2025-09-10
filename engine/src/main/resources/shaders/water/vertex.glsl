#version 330 core

#define NUM_WAVES 4

/***
 * Structures
 ***/

struct Wave {
    vec2 direction;
    float amplitude;
    float wavelength;
    float steepness;
    float speed;
};

/* Vertex attributes */
layout(location = 0) in vec3 position;
layout(location = 1) in vec2 textureCoordinates;

/* Matrices */
uniform mat4 matrixViewProjection;
uniform mat4 matrixModel;

/* Waves & time */
uniform Wave waves[NUM_WAVES];
uniform float time;

/* Parameters going to fragment shader */
out vec3 vertexNormal;
out vec3 vertexPosition;
out vec2 vertexTextureCoordinates;

vec3 gerstnerWave(vec3 pos, Wave w, float t, out vec3 outNormal) {
    float k = 2.0 * 3.14159 / w.wavelength;
    float f = k * dot(w.direction, pos.xz) - w.speed * t;

    float a = w.amplitude;
    float q = w.steepness;

    pos.x += q * a * w.direction.x * cos(f);
    pos.z += q * a * w.direction.y * cos(f);
    pos.y += a * sin(f);

    outNormal = normalize(vec3(-w.direction.x * k * a * cos(f),
                          1.0 - q * k * a * sin(f),
                          -w.direction.y * k * a * cos(f)));
    return pos;
}

void main() {
    vec3 transformedPosition = position;
    vec3 transformedNormal = vec3(0, 1, 0);
    for (int i = 0; i < NUM_WAVES; i++)
        transformedPosition = gerstnerWave(transformedPosition, waves[i], time, transformedNormal);

    vertexPosition = transformedPosition;
    vertexNormal = normalize(transformedNormal);
    vertexTextureCoordinates = textureCoordinates;

    gl_Position = matrixViewProjection * matrixModel * vec4(transformedPosition, 1.0);
}