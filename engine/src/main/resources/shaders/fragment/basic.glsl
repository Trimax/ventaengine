#version 330 core

in vec4 vertexColor;
in vec2 vertexTextureCoordinates;

out vec4 FragColor;

uniform sampler2D textureDiffuse;

void main() {
    vec4 texColor = texture(textureDiffuse, vertexTextureCoordinates);
    FragColor = vertexColor * texColor;
}