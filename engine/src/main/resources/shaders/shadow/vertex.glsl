#version 330 core

/* Vertex attributes */
layout(location = 0) in vec3 position;

/* Matrices: the sun looks at the scene */
uniform mat4 matrixViewProjection;
uniform mat4 matrixModel;

void main() {
    gl_Position = matrixViewProjection * matrixModel * vec4(position, 1.0);
}
