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


vec3 calculateLight(Light light, vec3 N, vec3 V) {
    if (light.enabled == 0)
        vec3(0.0);

    vec3 L;
    if (light.type == LIGHT_TYPE_DIRECTIONAL) {
        L = normalize(-light.direction);
    } else {
        L = normalize(light.position - vertexPosition);
    }

    float distance = length(light.position - vertexPosition);
    float attenuation = 1.0;

    if (light.type != 0) {
        attenuation = 1.0 / (light.attenuation.constant
        + light.attenuation.linear * distance
        + light.attenuation.quadratic * distance * distance);
    }

    float diff = max(dot(N, L), 0.0);
    vec3 R = reflect(-L, N);
    float spec = pow(max(dot(R, V), 0.0), 32.0);

    return (diff + spec * 0.3) * light.color * light.intensity * attenuation;
}

vec3 computeLighting(vec3 baseColor, vec3 N, vec3 cameraDirection) {
    vec3 result = ambientLight * baseColor;

    for (int i = 0; i < lightCount; i++)
        result += calculateLight(lights[i], N, cameraDirection);

    return result;
}

void main() {
    vec3 cameraDirection = normalize(cameraPosition - vertexPosition);
    vec3 baseColor = vec3(0.0, 0.3, 0.6);

    vec3 color = computeLighting(baseColor, normalize(vertexNormal), cameraDirection);

    outputColor = vec4(color, 1.0);
}