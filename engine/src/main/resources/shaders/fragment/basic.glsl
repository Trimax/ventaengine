#version 330 core

const int MAX_LIGHTS = 64;
const int LIGHT_TYPE_POINT = 0;
const int LIGHT_TYPE_DIRECTIONAL = 1;
const int LIGHT_TYPE_SPOT = 2;

struct Attenuation {
    float constant;
    float linear;
    float quadratic;
};

struct Light {
    int type; // See LIGHT_TYPE constants
    vec3 position;
    vec3 direction;
    vec3 color;
    float intensity;

    Attenuation attenuation;

    bool castShadows;
    bool enabled;
};

in vec4 vertexColor;
in vec2 vertexTextureCoordinates;
in vec3 vertexPosition;
in vec3 vertexNormal;

uniform sampler2D textureDiffuse;
uniform sampler2D heightTexture;

uniform Light lights[MAX_LIGHTS];
uniform int lightCount;

out vec4 FragColor;

vec3 calculateLight(Light light, vec3 normal, vec3 fragPos) {
    if (!light.enabled)
        return vec3(0.0);

    vec3 lightDir = vec3(0.0);
    float distance = 1.0;
    float attenuation = 1.0;

    switch (light.type) {
        case LIGHT_TYPE_POINT:
            lightDir = normalize(light.position - fragPos);
            distance = length(light.position - fragPos);
            attenuation = 1.0 / (light.attenuation.constant + light.attenuation.linear * distance + light.attenuation.quadratic * distance * distance);
            break;

        case LIGHT_TYPE_DIRECTIONAL:
            lightDir = normalize(-light.direction);
            break;

        case LIGHT_TYPE_SPOT:
            lightDir = normalize(light.position - fragPos);
            distance = length(light.position - fragPos);
            attenuation = 1.0 / (light.attenuation.constant + light.attenuation.linear * distance + light.attenuation.quadratic * distance * distance);
            break;

        default:
            return vec3(0.0);
    }

    float diff = max(dot(normal, lightDir), 0.0);
    return light.color * light.intensity * diff * attenuation;
}

void main() {
    vec4 diffuseColor = texture(textureDiffuse, vertexTextureCoordinates);
    float heightValue = texture(heightTexture, vertexTextureCoordinates).r;

    vec3 normal = normalize(vertexNormal);
    vec3 lighting = vec3(0.1); // Temporary ambient lighting

    for (int i = 0; i < lightCount; i++) {
        lighting += calculateLight(lights[i], normal, vertexPosition);
    }

    vec3 modulatedColor = diffuseColor.rgb * (0.8 + 0.2 * heightValue);
    vec3 finalColor = modulatedColor * lighting;

    FragColor = vertexColor * vec4(clamp(finalColor, 0.0, 1.0), diffuseColor.a);
}