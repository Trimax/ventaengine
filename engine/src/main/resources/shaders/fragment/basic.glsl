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
    int castShadows;
    int enabled;
};

/* Vertex shader output */
in vec4 vertexColor;
in vec3 vertexViewDirection;
in vec2 vertexTextureCoordinates;
in vec3 vertexPosition;

in mat3 vertexTBN;

/* Textures */
uniform sampler2D textureDiffuse;
uniform sampler2D textureHeight;
uniform sampler2D textureNormal;
uniform sampler2D textureRoughness;
uniform sampler2D textureAmbientOcclusion;

/* Feature flags */
uniform int useTextureDiffuse;
uniform int useTextureHeight;
uniform int useTextureNormal;
uniform int useTextureRoughness;
uniform int useTextureAmbientOcclusion;
uniform int useLighting;

/* Material parameters */
uniform float materialShininess;
uniform vec2 materialTiling;
uniform vec2 materialOffset;
uniform vec3 materialColor;

/* Lighting */
uniform Light lights[MAX_LIGHTS];
uniform vec4 ambientLight;
uniform int lightCount;

/* Output color */
out vec4 FragColor;

/* Checks if flag is set (bools are not supported by macOS Radeon cards */
bool isSet(int value) {
    return value != 0;
}

/* Gets base color from the texture */
vec4 getDiffuseColor(vec2 textureCoordinates) {
    return isSet(useTextureDiffuse) ? texture(textureDiffuse, textureCoordinates) : vec4(1.0);
}

/* Gets ambient occlusion */
float getAmbientOcclusion(vec2 textureCoordinates) {
    return isSet(useTextureAmbientOcclusion) ? texture(textureAmbientOcclusion, textureCoordinates).r : 1.0;
}

/* Gets height */
float getHeight(vec2 textureCoordinates) {
    return isSet(useTextureHeight) ? texture(textureHeight, textureCoordinates).r : 1.0;
}

/* Gets roughness */
float getRoughness(vec2 textureCoordinates) {
    return isSet(useTextureRoughness) ? texture(textureRoughness, textureCoordinates).r : 1.0;
}

/* Translates texture coordinates according to parallax effect */
vec2 parallaxMapping(vec2 textureCoordinates) {
    if (!isSet(useTextureHeight))
        return textureCoordinates;

    return textureCoordinates + normalize(vertexViewDirection).xy * (getHeight(textureCoordinates) * 0.01);
}

/* Gets normal (either face or from normal map) */
vec3 getNormal(vec2 textureCoordinates) {
    if (!isSet(useTextureNormal))
        return vertexTBN[2];

    /* Normal mapping */
    return normalize(vertexTBN * (texture(textureNormal, textureCoordinates).rgb * 2.0 - 1.0));
}

/* Calculates effect of one light source */
vec3 calculateLight(Light light, vec3 normal, vec3 fragPos) {
    if (!isSet(light.enabled))
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

vec3 calculateLighting(vec2 textureCoordinates) {
    if (!isSet(useLighting))
        return vec3(1.0);

    vec3 normal = getNormal(textureCoordinates);

    vec3 lighting = ambientLight.xyz * ambientLight.w;
    for (int i = 0; i < lightCount; i++)
        lighting += calculateLight(lights[i], normal, vertexPosition);

    return lighting;
}

vec2 getTextureCoordinates() {
    vec2 textureCoordinates = vertexTextureCoordinates * materialTiling + materialOffset;

    return isSet(useTextureHeight) ? parallaxMapping(textureCoordinates) : textureCoordinates;
}

void main() {
    vec2 textureCoordinates = getTextureCoordinates();

    vec4 diffuseColor = getDiffuseColor(textureCoordinates) * vec4(materialColor, 1.0);
    vec3 lighting = calculateLighting(textureCoordinates) * getAmbientOcclusion(textureCoordinates) * getRoughness(textureCoordinates);

    FragColor = vertexColor * vec4(clamp(diffuseColor.rgb * lighting, 0.0, 1.0), diffuseColor.a);
}