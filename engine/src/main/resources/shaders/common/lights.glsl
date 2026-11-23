const int MAX_LIGHTS = 64;

struct Attenuation {
    float constant;
    float linear;
    float quadratic;
};

struct Light {
    vec3 position;
    vec3 color;
    float intensity;
    Attenuation attenuation;
    int castShadows;
    int enabled;
};

struct DirectionalLight {
    vec3 color;
    vec3 direction;
    float intensity;
};

/* Lighting */
uniform DirectionalLight directionalLight;
uniform int useDirectionalLight;
uniform Light lights[MAX_LIGHTS];
uniform vec3 ambientLight;
uniform int lightCount;

float getAttenuation(Attenuation attenuation, float distance) {
    // An all-zero attenuation would divide by zero
    return 1.0 / max(attenuation.constant + attenuation.linear * distance + attenuation.quadratic * distance * distance, 0.0001);
}
