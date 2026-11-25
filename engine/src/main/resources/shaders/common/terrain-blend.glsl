/*
 * The functions that blend the materials of a terrain by the steepness of the ground and sample a material from three
 * sides (triplanar): the texture of the ground is laid by the position on the map, so it stretches on a cliff, and a
 * triplanar one is laid by the world along each of the axes and the three are mixed by the direction of the surface.
 */

/* How sharply the planes of a triplanar texture are told apart: the greater, the narrower the bands where they mix */
const float TRIPLANAR_SHARPNESS = 6.0;

/* The angle between the ground and the horizon in degrees: 0 on a plane, 90 on a wall (a ceiling counts as a wall) */
float getSlopeAngle(vec3 normal) {
    return degrees(acos(clamp(normal.y, 0.0, 1.0)));
}

/*
 * How much a material that covers the ground between two angles is there at the angle: full between them and fading to
 * nothing over the blend outside of them. A material that starts at 0 or ends at 90 has no edge there.
 */
float getSlopeWeight(float angle, float minimum, float maximum, float blend) {
    float lower = blend > 0.0 ? smoothstep(minimum - blend, minimum, angle) : step(minimum, angle);
    float upper = blend > 0.0 ? 1.0 - smoothstep(maximum, maximum + blend, angle) : step(angle, maximum);

    return lower * upper;
}

/* How much of the texture of every plane (x, y, z) a surface takes: by the direction of its normal, the three add up to 1 */
vec3 getTriplanarWeights(vec3 normal) {
    vec3 weights = pow(abs(normal), vec3(TRIPLANAR_SHARPNESS));

    return weights / max(weights.x + weights.y + weights.z, 0.00001);
}

/*
 * The coordinates of a point in the texture of a plane. The plane x is seen along x (its coordinates are z and y), the
 * plane y along y (x and z), the plane z along z (x and y); every one is laid so that the top of the picture points up
 * and the picture is not mirrored (see getTriplanarNormal). The size is how far the texture goes before it repeats,
 * for a tiling of 1.
 */
vec2 getTriplanarCoordinates(int plane, vec3 position, vec3 normal, vec2 size) {
    if (plane == 0)
        return vec2(-sign(normal.x) * position.z / size.x, -position.y / size.x);

    if (plane == 1)
        return vec2(position.x / size.x, sign(normal.y) * position.z / size.y);

    return vec2(sign(normal.z) * position.x / size.x, -position.y / size.x);
}

/*
 * The normal in the world of a normal map laid on a plane: the red channel goes along the direction of the u of the
 * picture, the green one along its up and the blue one out of the surface (the same as the normal maps of the surfaces
 * use). The sides are the ones getTriplanarCoordinates lays the picture by.
 */
vec3 getTriplanarNormal(int plane, vec3 normal, vec3 mapped) {
    if (plane == 0)
        return mapped.x * vec3(0.0, 0.0, -sign(normal.x)) + mapped.y * vec3(0.0, 1.0, 0.0) + mapped.z * vec3(sign(normal.x), 0.0, 0.0);

    if (plane == 1)
        return mapped.x * vec3(1.0, 0.0, 0.0) + mapped.y * vec3(0.0, 0.0, -sign(normal.y)) + mapped.z * vec3(0.0, sign(normal.y), 0.0);

    return mapped.x * vec3(sign(normal.z), 0.0, 0.0) + mapped.y * vec3(0.0, 1.0, 0.0) + mapped.z * vec3(0.0, 0.0, sign(normal.z));
}
