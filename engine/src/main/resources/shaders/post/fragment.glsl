#version 330 core

#include "common/color.glsl"

/***
 * Definitions
 ***/

/* Tone mapping operators, see the ToneMapping enum */
const int TONE_MAPPING_NONE = 0;
const int TONE_MAPPING_NEUTRAL = 1;
const int TONE_MAPPING_ACES = 2;

/***
 * Input variables and uniforms
 ***/

/* Vertex shader output */
in vec2 vertexTextureCoordinates;

/* The scene in linear HDR colors */
uniform sampler2D textureScene;

/* Multiplies the light before the tone mapping, 1 keeps it */
uniform float exposure;

/* The tone mapping operator */
uniform int toneMapping;

/* Output color */
out vec4 outputColor;

/***
 * Tone mapping
 ***/

vec3 toneMap(vec3 color) {
    if (toneMapping == TONE_MAPPING_ACES)
        return toneMapACES(color);

    if (toneMapping == TONE_MAPPING_NEUTRAL)
        return toneMapNeutral(color);

    return clamp(color, 0.0, 1.0);
}

void main() {
    vec3 color = texture(textureScene, vertexTextureCoordinates).rgb * exposure;

    outputColor = vec4(toSRGB(toneMap(max(color, vec3(0.0)))), 1.0);
}
