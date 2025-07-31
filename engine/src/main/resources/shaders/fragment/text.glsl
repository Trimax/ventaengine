#version 330 core

/* Vertex shader output */
in vec2 vertexTextureCoordinates;

/* Texture */
uniform sampler2D textureDiffuse;

/* Output color */
out vec4 FragColor;

void main() {
    FragColor = vec4(0.2, 0.8, 0.2, texture(textureDiffuse, vertexTextureCoordinates).r);
}