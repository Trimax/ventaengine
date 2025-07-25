#version 330 core

/* Vertex attributes */
layout(location = 0) in vec3 position;
layout(location = 1) in vec3 normal;
layout(location = 2) in vec3 tangent;
layout(location = 3) in vec3 bitangent;
layout(location = 4) in vec2 textureCoordinates;
layout(location = 5) in vec4 color;

/* Camera attributes */
uniform vec3 cameraPosition;

/* Matrices */
uniform mat4 matrixViewProjection;  // Multiplied View & Projections matrices (built based on the window & camera parameters)
uniform mat3 matrixNormal;          // Multiplied P * R * S
uniform mat4 matrixModel;           // Normal matrix computed by model matrix

out mat3 vertexTBN;
out vec4 vertexColor;
out vec3 vertexPosition;
out vec3 vertexViewDirection;
out vec2 vertexTextureCoordinates;

void main() {
    vec4 worldPos = matrixModel * vec4(position, 1.0);

    vertexTBN = mat3(normalize(matrixNormal * tangent), normalize(matrixNormal * bitangent), normalize(matrixNormal * normal));
    vertexViewDirection = normalize(vertexTBN * normalize(cameraPosition - worldPos.xyz));
    vertexTextureCoordinates = textureCoordinates;
    vertexPosition = worldPos.xyz;
    vertexColor = color;

    gl_Position = matrixViewProjection * worldPos;
}
