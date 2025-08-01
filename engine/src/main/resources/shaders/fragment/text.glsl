#version 330 core

/* Vertex shader output */
in vec2 vertexTextureCoordinates;

/* Texture */
uniform sampler2D textureDiffuse;

/* Text color */
uniform vec3 color;

/* Output color */
out vec4 FragColor;

void main() {
    FragColor = vec4(color, texture(textureDiffuse, vertexTextureCoordinates).r);
}