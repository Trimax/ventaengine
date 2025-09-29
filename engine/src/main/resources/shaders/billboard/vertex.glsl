#version 330 core

/***
 * Definitions
 ***/

/* Common */
const int MAX_FRAMES = 128;

/* Vertex attributes */
layout(location = 0) in vec2 position;

/* Matrices */
uniform mat4 matrixViewProjection;  // Multiplied View & Projections matrices (built based on the window & camera parameters)
uniform mat4 matrixModel;           // Normal matrix computed by model matrix

uniform vec4 color;

/* Frames */
uniform vec4 frames[MAX_FRAMES];
uniform int frameIndex;

/* Parameters going to fragment shader */
out vec2 vertexTextureCoordinates;
out vec4 vertexColor;

void main() {
    vec2 baseUV = position + 0.5; // Position is in [-0.5; 0.5] range (see GeometryDefinitions.PARTICLE_VERTICES)
    vec4 subTextureCoordinates = frames[frameIndex];

    vertexColor = color;
    vertexTextureCoordinates = subTextureCoordinates.xy + baseUV * subTextureCoordinates.zw;
    vertexTextureCoordinates.y = 1.0 - vertexTextureCoordinates.y;

    gl_Position = matrixViewProjection * matrixModel * vec4(position, 0.0, 1.0);
}