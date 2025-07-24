#version 330 core

/* Vertex attributes */
layout(location = 0) in vec3 position;
layout(location = 3) in vec4 color;

/* Camera attributes */
uniform mat4 matrixViewProjection;  // Multiplied View & Projections matrices (built based on the window & camera parameters)
uniform mat4 matrixModel;           // Normal matrix computed by model matrix

out vec4 vertexColor;

void main() {
    vertexColor = color;
    gl_Position = matrixViewProjection * matrixModel * vec4(position, 1.0);
}