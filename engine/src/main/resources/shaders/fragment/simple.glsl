#version 330 core

/* Vertex shader output */
in vec4 vertexColor;

out vec4 fragColor;

void main() {
    fragColor = vertexColor;
}