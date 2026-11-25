const int MAX_LIGHTS = 32;

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

    /* A spot light shines in a cone: the unit direction and the cosines of the inner and the outer angle */
    int spot;
    vec3 direction;
    float cosInner;
    float cosOuter;
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

/*
 * How much of a spot light reaches a point by the angle from its direction: all of it inside the inner angle, nothing
 * beyond the outer one and a smooth fall between. A point light shines to all the directions.
 */
float getLightCone(Light light, vec3 position) {
    if (light.spot == 0)
        return 1.0;

    vec3 toPoint = position - light.position;
    float cosine = dot(toPoint, light.direction) / max(length(toPoint), 0.0001);

    // The same angles make a hard edge
    if (light.cosInner <= light.cosOuter + 1e-6)
        return cosine >= light.cosOuter ? 1.0 : 0.0;

    return smoothstep(light.cosOuter, light.cosInner, cosine);
}

/* The part of the light of a source that reaches a point: its attenuation, the fade at the end of its range and the cone */
float getLightFactor(Light light, vec3 position) {
    float distance = length(light.position - position);

    return getAttenuation(light.attenuation, distance) * getLightFade(light.range, distance) * getLightCone(light, position);
}
