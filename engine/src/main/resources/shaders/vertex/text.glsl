#version 330 core

layout(location = 0) in vec2 aPos;
layout(location = 1) in vec2 textureCoordinates;

out vec2 vertexTextureCoordinates;

uniform vec2 uPos;
uniform float uScale;

void main() {
    // Coordinates in [-1;1]
    vec2 pos = aPos * uScale + uPos;

    gl_Position = vec4(pos.x, pos.y, 0.0, 1.0);
    vertexTextureCoordinates = textureCoordinates;
}