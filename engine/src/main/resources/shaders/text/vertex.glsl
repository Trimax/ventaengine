#version 330 core

layout(location = 0) in vec2 vertexPosition;

out vec2 vertexTextureCoordinates;

uniform vec4 quadBounds;
uniform vec4 texBounds;

void main() {
    float x = mix(quadBounds.x, quadBounds.z, vertexPosition.x);
    float y = mix(quadBounds.y, quadBounds.w, vertexPosition.y);

    gl_Position = vec4(x, y, 0.0, 1.0);

    float s = mix(texBounds.x, texBounds.z, vertexPosition.x);
    float t = mix(texBounds.y, texBounds.w, vertexPosition.y);

    vertexTextureCoordinates = vec2(s, t);
}