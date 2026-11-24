struct Fog {
    vec3 color;
    float minimalDistance;
    float maximalDistance;
    float height;

    /* The exponential fog: its density at the base height falls with the height (a density of 0 is the fog of a layer) */
    float density;
    float falloff;
    float baseHeight;

    /* The color of the fog towards the sun */
    vec3 sunDirection;
    vec3 sunColor;
    float sunScattering;
    float sunSharpness;
};

/* Fog */
uniform Fog fog;
uniform int useFog;

/* The exponent is kept in the range of a float, a far point below or a camera far above would overflow it */
const float FOG_MAXIMAL_EXPONENT = 80.0;

/* Whether the fog is an exponential one, which gets thinner with the height, or the fog of a layer */
bool isFogExponential() {
    return fog.density > 0.0;
}

/* Part of the way between two points that lies in the fog layer (below fog.height) */
float getFogDistance(vec3 from, vec3 to) {
    float distance = length(to - from);
    float lower = min(from.y, to.y);
    float upper = max(from.y, to.y);

    if (upper <= fog.height)
        return distance;

    if (lower >= fog.height)
        return 0.0;

    return distance * (fog.height - lower) / (upper - lower);
}

/* Amount of fog color: 0 is no fog, 1 is the fog color only */
float getFogFactor(float distance) {
    float range = max(fog.maximalDistance - fog.minimalDistance, 0.0001);
    return clamp((distance - fog.minimalDistance) / range, 0.0, 1.0);
}

/*
 * The thickness of the exponential fog along the way between two points: the integral of the density, which is
 * density * exp(-falloff * (y - baseHeight)) at the height y, along the way. It has a closed form: the density at the
 * start of the way, times the length, times the mean of the fall of the density over the way.
 */
float getFogThickness(vec3 from, vec3 to) {
    vec3 way = to - from;
    float start = fog.density * exp(clamp(-fog.falloff * (from.y - fog.baseHeight), -FOG_MAXIMAL_EXPONENT, FOG_MAXIMAL_EXPONENT));
    float drop = clamp(fog.falloff * way.y, -FOG_MAXIMAL_EXPONENT, FOG_MAXIMAL_EXPONENT);

    // (1 - exp(-drop)) / drop is 1 on a level way, the series is used where the division would lose the digits
    float mean = abs(drop) < 1e-3 ? 1.0 - 0.5 * drop : (1.0 - exp(-drop)) / drop;

    return start * length(way) * mean;
}

/* The density of the exponential fog at a height: the part of the light that is lost on a unit of the way */
float getFogDensity(float height) {
    return fog.density * exp(clamp(-fog.falloff * (height - fog.baseHeight), -FOG_MAXIMAL_EXPONENT, FOG_MAXIMAL_EXPONENT));
}

/* Amount of fog color of a point seen from another one */
float getFogAmount(vec3 from, vec3 to) {
    if (isFogExponential())
        return 1.0 - exp(-getFogThickness(from, to));

    return getFogFactor(getFogDistance(from, to));
}

/* The color of the fog seen along a direction: towards the sun it takes the color of the sun */
vec3 getFogColor(vec3 direction) {
    if (fog.sunScattering <= 0.0)
        return fog.color;

    float lobe = pow(max(dot(direction, fog.sunDirection), 0.0), fog.sunSharpness);
    return mix(fog.color, fog.sunColor, clamp(fog.sunScattering * lobe, 0.0, 1.0));
}

/* The color of the fog of a point seen from another one */
vec3 getFogColor(vec3 from, vec3 to) {
    vec3 way = to - from;
    float distance = length(way);

    return distance > 1e-4 ? getFogColor(way / distance) : fog.color;
}
