#version 330 core

/* Vertex attributes */
layout(location = 0) in vec2 vertexPosition;

/* */
uniform vec4 boundsPosition;
uniform vec4 boundsTextureCoordinates;

/* Parameters going to fragment shader */
out vec2 vertexTextureCoordinates;

void main() {
    float s = mix(boundsTextureCoordinates.x, boundsTextureCoordinates.z, vertexPosition.x);
    float t = mix(boundsTextureCoordinates.y, boundsTextureCoordinates.w, vertexPosition.y);
    vertexTextureCoordinates = vec2(s, t);

    float x = mix(boundsPosition.x, boundsPosition.z, vertexPosition.x);
    float y = mix(boundsPosition.y, boundsPosition.w, vertexPosition.y);
    gl_Position = vec4(x, y, 0.0, 1.0);
}