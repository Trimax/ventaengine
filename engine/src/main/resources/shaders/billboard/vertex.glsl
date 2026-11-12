#version 330 core

/***
 * Definitions
 ***/

/* Common */
const int MAX_FRAMES = 64;

/***
 * Structures
 ***/

struct Fog {
    vec3 color;
    float minimalDistance;
    float maximalDistance;
    float height;
};

/* Vertex attributes */
layout(location = 0) in vec2 position;

/* Matrices */
uniform mat4 matrixViewProjection;  // Multiplied View & Projections matrices (built based on the window & camera parameters)
uniform mat4 matrixModel;           // Normal matrix computed by model matrix

uniform vec4 color;

/* Frames */
uniform vec4 frames[MAX_FRAMES];
uniform int frameIndex;

/* Fog */
uniform Fog fog;
uniform int useFog;

/* Camera */
uniform vec3 cameraPosition;

/* Parameters going to fragment shader */
out vec2 vertexTextureCoordinates;
out vec4 vertexColor;
out float vertexFog;

/* Checks if flag is set (bools are not supported by macOS Radeon cards */
bool isSet(int value) {
    return value != 0;
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

void main() {
    /* The whole billboard is fogged as its center, so it stays uniform */
    vertexFog = isSet(useFog) ? getFogFactor(getFogDistance(cameraPosition, matrixModel[3].xyz)) : 0.0;

    vec2 baseUV = position + 0.5; // Position is in [-0.5; 0.5] range (see GeometryDefinitions.PARTICLE_VERTICES)
    vec4 subTextureCoordinates = frames[frameIndex];

    vertexColor = color;
    vertexTextureCoordinates = subTextureCoordinates.xy + baseUV * subTextureCoordinates.zw;
    vertexTextureCoordinates.y = 1.0 - vertexTextureCoordinates.y;

    gl_Position = matrixViewProjection * matrixModel * vec4(position, 0.0, 1.0);
}