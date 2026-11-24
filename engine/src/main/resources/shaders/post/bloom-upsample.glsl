#version 330 core

/*
 * Doubles a level of the bloom with a tent filter of 9 taps (the weights are 1 2 1 / 2 4 2 / 1 2 1 over 16). The result
 * is added to the level above it by the blending, so the glow of every scale ends up in the largest level.
 */

/* Vertex shader output */
in vec2 vertexTextureCoordinates;

/* The smaller level and the size of one of its texels */
uniform sampler2D textureScene;
uniform vec2 texelSize;

/* Output color */
out vec4 outputColor;

void main() {
    vec2 uv = vertexTextureCoordinates;

    vec3 sum = texture(textureScene, uv + texelSize * vec2(-1.0, 1.0)).rgb
            + texture(textureScene, uv + texelSize * vec2(0.0, 1.0)).rgb * 2.0
            + texture(textureScene, uv + texelSize * vec2(1.0, 1.0)).rgb
            + texture(textureScene, uv + texelSize * vec2(-1.0, 0.0)).rgb * 2.0
            + texture(textureScene, uv).rgb * 4.0
            + texture(textureScene, uv + texelSize * vec2(1.0, 0.0)).rgb * 2.0
            + texture(textureScene, uv + texelSize * vec2(-1.0, -1.0)).rgb
            + texture(textureScene, uv + texelSize * vec2(0.0, -1.0)).rgb * 2.0
            + texture(textureScene, uv + texelSize * vec2(1.0, -1.0)).rgb;

    outputColor = vec4(sum / 16.0, 1.0);
}
