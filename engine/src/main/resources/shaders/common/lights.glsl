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
    float range;
    int shadowSlot;
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

/*
 * How much of a light is left at a distance because of its range: all of it near the light, nothing at the range, and a
 * smooth fall between (the fourth power of the share of the range, squared, so there is no edge where the light ends).
 * A light without a range reaches as far as its attenuation does.
 */
float getLightFade(float range, float distance) {
    if (range <= 0.0)
        return 1.0;

    float share = clamp(distance / range, 0.0, 1.0);
    float fade = clamp(1.0 - share * share * share * share, 0.0, 1.0);

    return fade * fade;
}

float getAttenuation(Attenuation attenuation, float distance) {
    // An all-zero attenuation would divide by zero
    return 1.0 / max(attenuation.constant + attenuation.linear * distance + attenuation.quadratic * distance * distance, 0.0001);
}

/* The part of the light of a source that reaches a point at a distance: its attenuation and the fade at the end of its range */
float getLightFactor(Light light, float distance) {
    return getAttenuation(light.attenuation, distance) * getLightFade(light.range, distance);
}
