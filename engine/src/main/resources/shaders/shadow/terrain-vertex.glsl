#version 330 core

#include "common/heightmap.glsl"

/* Vertex attributes */
layout(location = 0) in vec3 position;
layout(location = 1) in vec2 textureCoordinates;

/* Matrices: the sun looks at the scene */
uniform mat4 matrixViewProjection;
uniform mat4 matrixModel;

void main() {
    vec3 displaced = vec3(position.x, getHeight(textureCoordinates), position.z);

    gl_Position = matrixViewProjection * matrixModel * vec4(displaced, 1.0);
}
