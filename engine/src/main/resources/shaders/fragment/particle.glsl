#version 330 core

/* Vertex shader output */
in vec2 vertexTextureCoordinates;

/* Textures */
uniform sampler2D textureDiffuse;
uniform vec4 color;

/* Output color */
out vec4 outputColor;

void main() {
    outputColor = texture(textureDiffuse, vertexTextureCoordinates) * color;
}