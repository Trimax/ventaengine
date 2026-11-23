#version 330 core

/* Vertex attributes */
layout(location = 0) in vec3 position;
layout(location = 4) in vec2 textureCoordinates;
layout(location = 5) in vec4 color;

/* Matrices: the sun looks at the scene */
uniform mat4 matrixViewProjection;
uniform mat4 matrixModel;

/* The alpha of the surface decides whether a fragment casts a shadow */
out vec2 vertexTextureCoordinates;
out vec4 vertexColor;

void main() {
    vertexTextureCoordinates = textureCoordinates;
    vertexColor = color;

    gl_Position = matrixViewProjection * matrixModel * vec4(position, 1.0);
}
