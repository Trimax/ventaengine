#version 330 core

/* Vertex attributes */
layout(location = 0) in vec3 position;
layout(location = 1) in vec3 normal;
layout(location = 2) in vec2 textureCoordinates;
layout(location = 3) in vec4 color;

/* Camera attributes */
uniform mat4 matrixViewProjection;  // Multiplied View & Projections matrices (built based on the window & camera parameters)
uniform mat3 matrixNormal;          // Multiplied P * R * S
uniform mat4 matrixModel;           // Normal matrix computed by model matrix

out vec4 vertexColor;
out vec2 vertexTextureCoordinates;
out vec3 vertexPosition;
out vec3 vertexNormal;

void main() {
    vec4 worldPos = matrixModel * vec4(position, 1.0);
    vertexPosition = worldPos.xyz;
    vertexNormal = normalize(matrixNormal * normal);

    gl_Position = matrixViewProjection * worldPos;

    vertexColor = color;
    vertexTextureCoordinates = textureCoordinates;
}
