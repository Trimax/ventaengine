#version 330 core
layout(location = 0) in vec3 position;

/* Matrices */
uniform mat4 matrixViewProjection;
uniform mat4 matrixModel;

out vec3 vertexColor;

void main() {
    gl_Position = matrixViewProjection * vec4(position, 1.0);
    vertexColor = position;
}