#version 330 core

/* Vertex shader output */
in vec4 vertexColor;

/* Output color */
out vec4 outputColor;

void main() {
    outputColor = vertexColor;
}