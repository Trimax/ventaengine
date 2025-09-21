#version 330 core

in vec2 vertexTextureCoordinates;
in vec4 vertexColor;

uniform sampler2D textureDiffuse;

out vec4 outputColor;

void main() {
    outputColor = texture(textureDiffuse, vertexTextureCoordinates) * vertexColor;
}