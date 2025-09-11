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

/***
 * Input variables and uniforms
 ***/

/* Vertex shader output */
in vec3 vertexNormal;
in vec3 vertexPosition;
in vec2 vertexTextureCoordinates;

/* Lighting */
uniform Light lights[MAX_LIGHTS];
uniform vec3 ambientLight;
uniform int lightCount;

uniform vec3 cameraPosition;

/* Output color */
out vec4 outputColor;

/***
 * Lighting functions
 ***/

float getAttenuation(Attenuation attenuation, float distance) {
    return 1.0 / (attenuation.constant + attenuation.linear * distance + attenuation.quadratic * distance * distance);
}

/* Computes light */
vec3 calculateLight(Light light, vec3 normal, vec3 cameraDirection) {
    if (light.enabled == 0)
        vec3(0.0);

    vec3 lightDirection = vec3(0.0);
    float attenuation = 1.0;

    switch (light.type) {
        case LIGHT_TYPE_POINT:
        case LIGHT_TYPE_SPOT:
            lightDirection = normalize(light.position - vertexPosition);
            attenuation = getAttenuation(light.attenuation, length(light.position - vertexPosition));
            break;

        case LIGHT_TYPE_DIRECTIONAL:
            lightDirection = normalize(-light.direction);
            break;

        default:
            return vec3(0.0);
    }

    float diff = max(dot(normal, lightDirection), 0.0);
    vec3 R = reflect(-lightDirection, normal);
    float spec = pow(max(dot(R, cameraDirection), 0.0), 32.0);

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