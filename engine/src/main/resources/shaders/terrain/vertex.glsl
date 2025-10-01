#version 330 core

/* Vertex attributes */
layout(location = 0) in vec3 position;
layout(location = 1) in vec2 textureCoordinates;

/* Matrices */
uniform mat4 matrixViewProjection;
uniform mat4 matrixModel;

/* Time */
uniform float timeElapsed;

/* Heightmap */
uniform sampler2D textureHeight;
uniform float factor;

/* Parameters going to fragment shader */
out vec3 vertexNormal;
out vec3 vertexPosition;
out float vertexTimeElapsed;
out vec2 vertexTextureCoordinates;

void main() {
    float height = texture(textureHeight, textureCoordinates).r;

    vertexNormal = vec3(0, 1, 0);
    vertexPosition = position;
    vertexTimeElapsed = timeElapsed;
    vertexPosition.y = (height - 0.5) * factor;
    vertexTextureCoordinates = textureCoordinates;

    gl_Position = matrixViewProjection * matrixModel * vec4(vertexPosition, 1.0);
}