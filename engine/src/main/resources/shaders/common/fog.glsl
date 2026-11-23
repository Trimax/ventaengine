struct Fog {
    vec3 color;
    float minimalDistance;
    float maximalDistance;
    float height;
};

/* Fog */
uniform Fog fog;
uniform int useFog;

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

/* Amount of fog color of a point seen from another one */
float getFogAmount(vec3 from, vec3 to) {
    return getFogFactor(getFogDistance(from, to));
}
