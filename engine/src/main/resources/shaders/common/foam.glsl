/*
 * The foam of water: a lace of bubbles. It is made of a cellular noise (every point is as far from the nearest point of a scattered set
 * as it is): round holes of different sizes in a film, and the lines where the cells meet.
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

/* How much of a film of holes is film, on the average: what it is when the holes are too small to be seen */
const float FILM_AVERAGE_LARGE = 0.78;
const float FILM_AVERAGE_FINE = 0.86;

/*
 * A film with holes: 1 on the film and 0 in a hole, the edge of a hole is soft. Every cell of the grid has a hole round its point, and
 * the holes of the cells that are next to each other overlap, so the film is a web of walls of different thickness between round holes of
 * different sizes, as the foam of a wave that has run up the sand is.
 */
float getFoamFilm(vec2 position, float seed) {
    vec2 cell = floor(position);
    vec2 offset = fract(position);

    float film = 1.0;
    for (int y = -1; y <= 1; y++)
        for (int x = -1; x <= 1; x++) {
            vec2 neighbour = vec2(float(x), float(y));
            vec2 corner = cell + neighbour;

            float radius = 0.12 + 0.3 * hash(corner + seed);
            float distance = length(neighbour + getFoamPoint(corner + seed) - offset);

            film = min(film, smoothstep(radius * 0.25, radius, distance));
        }

    return film;
}

/*
 * The lace of the foam at a point (the position along the water and the scale: how many cells a unit holds), from 0 (a gap) to 1
 * (a bubble). The foam doesn't lie in rows: a slow noise bends the plane. It has holes of two sizes (the small ones are in the
 * walls of the big ones), thin filaments where the small cells meet, single bubbles, and it is thick in some patches and thin in
 * the others, so there is no pattern that repeats.
 *
 * The footprint is how much of the position a pixel covers (the caller takes it from the derivatives of the position, where it can: the
 * derivatives are not defined in a branch): the holes that are smaller than a pixel only shimmer, so they give way to the average film.
 */
float getFoamLace(vec2 position, float scale, float footprint) {
    vec2 point = position * scale;
    float cells = max(footprint, 0.0) * scale;
    vec2 bent = point + (vec2(noise(point * 0.4 + 3.1), noise(point * 0.4 + 9.7)) - 0.5) * 0.7;

    // How many cells a pixel covers says which holes are drawn, and the film that is too fine is its average
    float large = 1.0 - smoothstep(0.12, 0.35, cells * 1.15);
    float fine = 1.0 - smoothstep(0.12, 0.35, cells * 2.7);
    float tiny = 1.0 - smoothstep(0.12, 0.35, cells * 4.1);

    float film = mix(FILM_AVERAGE_LARGE, getFoamFilm(bent * 1.15, 0.0), large)
            * mix(1.0, mix(FILM_AVERAGE_FINE, getFoamFilm(bent * 2.7 + 23.0, 5.0), fine), 0.75);

    float filaments = 0.0;
    if (tiny > 0.0) {
        vec2 distances = getFoamDistances(bent * 4.1 + 41.0);
        filaments = (1.0 - smoothstep(0.0, 0.16, distances.y - distances.x)) * tiny;
    }

    float bubbles = smoothstep(0.80, 0.95, noise(point * 7.0 + 13.0)) * (1.0 - smoothstep(0.12, 0.35, cells * 7.0));
    float patches = noise(point * 0.3 + 7.0) * 0.6 + noise(point * 0.9 + 1.3) * 0.4;

    return clamp(film * (0.3 + patches * 0.9) + filaments * 0.3 * patches + bubbles * 0.25 * patches - 0.04, 0.0, 1.0);
}
