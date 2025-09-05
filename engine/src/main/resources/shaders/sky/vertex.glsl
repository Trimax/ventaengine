#version 330 core

/* Vertex attributes */
layout(location = 0) in vec3 position;

/* Matrices */
uniform mat4 matrixProjection;
uniform mat4 matrixView;

/* Parameters going to fragment shader */
out vec3 vertexPosition;

void main() {
    mat4 viewNoTranslation = mat4(mat3(matrixView));

    vertexPosition = position;

    gl_Position = matrixProjection * viewNoTranslation * vec4(position, 1.0);
    gl_Position.z = gl_Position.w;
}