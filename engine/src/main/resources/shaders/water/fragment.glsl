#version 330 core

/***
 * Definitions
 ***/

/* Light specific */
const int MAX_LIGHTS = 64;
const int LIGHT_TYPE_POINT = 0;
const int LIGHT_TYPE_DIRECTIONAL = 1;
const int LIGHT_TYPE_SPOT = 2;

/***
 * Structures
 ***/

struct Attenuation {
    float constant;
    float linear;
    float quadratic;
};

struct Light {
    int type;
    vec3 position;
    vec3 direction;
    vec3 color;
    float intensity;
    Attenuation attenuation;
    int castShadows;
    int enabled;
};

in vec3 vertexNormal;
in vec3 vertexPosition;
in vec2 vertexTextureCoordinates;


/* Lighting */
uniform Light lights[MAX_LIGHTS];
uniform vec3 ambientLight;
uniform int lightCount;

uniform vec3 cameraPosition;

out vec4 outputColor;

vec3 computeLighting(vec3 N, vec3 V, vec3 baseColor) {
    vec3 result = ambientLight * baseColor;

    for (int i = 0; i < lightCount; i++) {
        if (lights[i].enabled == 0)
            continue;

        vec3 L;
        if (lights[i].type == LIGHT_TYPE_DIRECTIONAL) {
            L = normalize(-lights[i].direction);
        } else {
            L = normalize(lights[i].position - vertexPosition);
        }

        float distance = length(lights[i].position - vertexPosition);
        float attenuation = 1.0;

        if (lights[i].type != 0) { // apply attenuation for point/spot
                                   attenuation = 1.0 / (lights[i].attenuation.constant
                                   + lights[i].attenuation.linear * distance
                                   + lights[i].attenuation.quadratic * distance * distance);
        }

        float diff = max(dot(N, L), 0.0);
        vec3 R = reflect(-L, N);
        float spec = pow(max(dot(R, V), 0.0), 32.0);

        result += (diff + spec * 0.3) * lights[i].color * lights[i].intensity * attenuation;
    }

    return result;
}

void main() {
    vec3 N = normalize(vertexNormal);
    vec3 V = normalize(cameraPosition - vertexPosition);
    vec3 baseColor = vec3(0.0, 0.3, 0.6);

    vec3 color = computeLighting(N, V, baseColor);

    outputColor = vec4(color, 1.0);
    //outputColor = vec4(1.0);
}