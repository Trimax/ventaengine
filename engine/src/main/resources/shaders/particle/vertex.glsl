#version 330 core

/* Vertex attributes */
layout(location = 0) in vec2 position;

/* Matrices */
uniform mat4 matrixViewProjection;  // Multiplied View & Projections matrices (built based on the window & camera parameters)
uniform mat4 matrixModel;           // Model matrix

/* Parameters going to fragment shader */
out vec2 vertexTextureCoordinates;

void main() {
    vertexTextureCoordinates = (position + 0.5);
    gl_Position = matrixViewProjection * matrixModel * vec4(position, 0.0, 1.0);
}