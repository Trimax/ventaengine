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

/* Vertex shader output */
in vec4 vertexColor;
in vec2 vertexTextureCoordinates;
in vec3 vertexPosition;
in vec3 vertexNormal;

in vec3 vertexTangent;
in vec3 vertexBitangent;

/* Textures */
uniform sampler2D textureDiffuse;
uniform sampler2D textureHeight;
uniform sampler2D textureNormal;

/* Feature flags */
uniform bool useTextureDiffuse;
uniform bool useTextureHeight;
uniform bool useTextureNormal;
uniform bool useLighting;

/* Lighting */
uniform Light lights[MAX_LIGHTS];
uniform vec4 ambientLight;
uniform int lightCount;

/* Output color */
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

vec4 getDiffuseColor() {
    return useTextureDiffuse ? texture(textureDiffuse, vertexTextureCoordinates) : vec4(1.0);
}

float getHeight() {
    return useTextureHeight ? texture(textureHeight, vertexTextureCoordinates).r : 1.0;
}

vec3 calculateLight(vec3 normal) {
    if (!useLighting)
        return vec3(1.0);

    vec3 lighting = ambientLight.xyz * ambientLight.w;
    for (int i = 0; i < lightCount; i++)
        lighting += calculateLight(lights[i], normal, vertexPosition);

    return lighting;
}

vec3 getNormal() {
    if (!useTextureNormal)
        return vertexNormal;

    /* Normal mapping */
    vec3 normalMap = texture(textureNormal, vertexTextureCoordinates).rgb;
    normalMap = normalMap * 2.0 - 1.0;
    mat3 TBN = mat3(normalize(vertexTangent), normalize(vertexBitangent), normalize(vertexNormal));

    return normalize(TBN * normalMap);
}

void main() {
    vec4 diffuseColor = getDiffuseColor();
    vec3 lighting = calculateLight(getNormal());

    vec3 modulatedColor = diffuseColor.rgb * (0.3 + 0.7 * getHeight());
    vec3 finalColor = modulatedColor * lighting;

    vec4 baseColor = vec4(clamp(finalColor, 0.0, 1.0), diffuseColor.a);

    FragColor = vertexColor * baseColor;
}