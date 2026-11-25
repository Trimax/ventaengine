/*
 * An equirectangular panorama: the whole sphere of the sky in a picture twice as wide as it is high. The longitude goes along it and
 * the latitude down it. The center of the picture looks along -z and the right of it goes to +x, so the picture is read from the
 * inside as it is seen (it is not mirrored); the top of the picture is up (+y).
 */

const float PANORAMA_PI = 3.14159265359;

/*
 * The angle of a direction round the vertical axis, from -pi to pi, 0 for -z and pi / 2 for +x. The two argument atan is not used:
 * it is not defined where the second argument is zero, which is where the middle of the faces of a cube is.
 */
float getPanoramaLongitude(float x, float z) {
    float forward = -z;
    if (abs(forward) < 1e-6)
        return x >= 0.0 ? 0.5 * PANORAMA_PI : -0.5 * PANORAMA_PI;

    float angle = atan(x / forward);
    if (forward < 0.0)
        angle += x >= 0.0 ? PANORAMA_PI : -PANORAMA_PI;

    return angle;
}

/* Where a direction is in the panorama: from 0 to 1 along it and down it */
vec2 getPanoramaCoordinates(vec3 direction) {
    vec3 normalized = normalize(direction);

    return vec2(getPanoramaLongitude(normalized.x, normalized.z) / (2.0 * PANORAMA_PI) + 0.5,
            0.5 - asin(clamp(normalized.y, -1.0, 1.0)) / PANORAMA_PI);
}
