#version 330 core

/* Vertex attributes */
layout(location = 0) in vec2 position;
layout(location = 1) in vec4 color;
layout(location = 2) in mat4 matrixModel;

/* Matrices */
uniform mat4 matrixViewProjection;  // Multiplied View & Projections matrices (built based on the window & camera parameters)

/* Parameters going to fragment shader */
out vec2 vertexTextureCoordinates;
out vec4 vertexColor;

void main() {
    vertexColor = color;
    vertexTextureCoordinates = (position + 0.5);
    gl_Position = matrixViewProjection * matrixModel * vec4(position, 0.0, 1.0);
}