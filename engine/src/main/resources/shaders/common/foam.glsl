/*
 * The foam of water: a lace of bubbles. The pattern is a cellular noise (every point is as far from the nearest point of a
 * scattered set as it is): along the lines between the cells (where the two nearest points are equally near) the bubbles
 * crowd, which gives the net of foam that is seen on the sea. Large patches of a soft noise make the net uneven.
 */

#include "common/noise.glsl"

/* A point of a cell of the grid: where the scattered point of the cell is, from the corner of the cell */
vec2 getFoamPoint(vec2 cell) {
    return fract(sin(vec2(dot(cell, vec2(127.1, 311.7)), dot(cell, vec2(269.5, 183.3)))) * 43758.5453);
}

/* The distances to the nearest and to the second nearest point of the scattered set: x and y */
vec2 getFoamDistances(vec2 position) {
    vec2 cell = floor(position);
    vec2 offset = fract(position);

    float nearest = 8.0;
    float second = 8.0;
    for (int y = -1; y <= 1; y++)
        for (int x = -1; x <= 1; x++) {
            vec2 neighbour = vec2(float(x), float(y));
            float distance = length(neighbour + getFoamPoint(cell + neighbour) - offset);
            if (distance < nearest) {
                second = nearest;
                nearest = distance;
            } else if (distance < second) {
                second = distance;
            }
        }

    return vec2(nearest, second);
}

/*
 * The lace of the foam at a point (the position along the water and the scale: how many cells a unit holds), from 0 (a gap) to 1
 * (a bubble): the net where the cells meet, in patches that a slow noise gives.
 */
float getFoamLace(vec2 position, float scale) {
    vec2 distances = getFoamDistances(position * scale);
    float net = 1.0 - smoothstep(0.0, 0.32, distances.y - distances.x);
    float patches = noise(position * scale * 0.35 + 7.0);

    return clamp(net * 0.75 + patches * 0.5 - 0.1, 0.0, 1.0);
}
