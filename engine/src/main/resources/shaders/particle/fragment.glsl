#version 330 core

/* Vertex shader output */
in vec2 vertexTextureCoordinates;
in vec4 vertexColor;

/* Textures */
uniform sampler2D textureDiffuse;

/* Output color */
out vec4 outputColor;

void main() {
    outputColor = texture(textureDiffuse, vertexTextureCoordinates) * vertexColor;
}