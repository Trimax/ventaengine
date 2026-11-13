#version 330 core

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

/*
 * The highlight compression of Khronos PBR Neutral: colors below 0.76 stay as they are, brighter ones are compressed
 * towards white and desaturated a bit, so scenes look like they are authored and lights don't clip. The toe of the
 * original operator (it darkens the tones below 0.08 to remove the specular reflection of PBR materials) isn't used,
 * the lighting isn't physically based yet.
 */
vec3 toneMapNeutral(vec3 color) {
    const float startCompression = 0.8 - 0.04;
    const float desaturation = 0.15;

    float peak = max(color.r, max(color.g, color.b));
    if (peak < startCompression)
        return color;

    const float range = 1.0 - startCompression;
    float newPeak = 1.0 - range * range / (peak + range - startCompression);
    color *= newPeak / peak;

    float whiteness = 1.0 - 1.0 / (desaturation * (peak - newPeak) + 1.0);
    return mix(color, vec3(newPeak), whiteness);
}

/*
 * Filmic tone mapping (the ACES curve fitted by K. Narkowicz): a contrasty film look, dark tones get darker and bright
 * colors roll off smoothly. The fit expects the color scaled by 0.6, so mid tones keep their brightness.
 */
vec3 toneMapACES(vec3 color) {
    color *= 0.6;
    return clamp((color * (2.51 * color + 0.03)) / (color * (2.43 * color + 0.59) + 0.14), 0.0, 1.0);
}

vec3 toneMap(vec3 color) {
    if (toneMapping == TONE_MAPPING_ACES)
        return toneMapACES(color);

    if (toneMapping == TONE_MAPPING_NEUTRAL)
        return toneMapNeutral(color);

    return clamp(color, 0.0, 1.0);
}

/* The sRGB transfer function (IEC 61966-2-1): the window shows sRGB colors */
vec3 toSRGB(vec3 color) {
    vec3 low = color * 12.92;
    vec3 high = 1.055 * pow(color, vec3(1.0 / 2.4)) - 0.055;

    return mix(low, high, step(vec3(0.0031308), color));
}

void main() {
    vec3 color = texture(textureScene, vertexTextureCoordinates).rgb * exposure;

    outputColor = vec4(toSRGB(toneMap(max(color, vec3(0.0)))), 1.0);
}
