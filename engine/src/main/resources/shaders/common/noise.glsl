/* A pseudo random value in [0, 1) of a point */
float hash(vec2 point) {
    return fract(sin(dot(point, vec2(127.1, 311.7))) * 43758.5453);
}

/* Smooth value noise in [0, 1]: the random values of the corners of the cell interpolated with a smooth curve */
float noise(vec2 point) {
    vec2 cell = floor(point);
    vec2 fraction = fract(point);
    vec2 weight = fraction * fraction * (3.0 - 2.0 * fraction);

    return mix(mix(hash(cell), hash(cell + vec2(1.0, 0.0)), weight.x),
               mix(hash(cell + vec2(0.0, 1.0)), hash(cell + vec2(1.0, 1.0)), weight.x), weight.y);
}
