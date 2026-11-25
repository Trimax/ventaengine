/*
 * The functions that make water look deep: the colors fade with how much water the view goes through, the shore is white
 * where the water is thin, and the bottom seen through the ripples is shifted by them.
 */

/*
 * How much of the light from under the water comes through a thickness of it: all of it for no water and almost none (5%)
 * at the depth distance, falling off exponentially (Beer-Lambert), so the shallows show the bottom and the deeps hide it.
 */
float getWaterTransmittance(float thickness, float depthDistance) {
    return exp(-3.0 * max(thickness, 0.0) / max(depthDistance, 0.0001));
}

/*
 * The foam where the water is thin: the shore and what stands in the water. It is the whitest at the very edge and gone at the
 * width. A pattern (from 0 to 1, moving ripples of a noise) breaks it into patches so it is not a smooth band.
 */
float getShoreFoam(float thickness, float width, float intensity, float pattern) {
    if (width <= 0.0)
        return 0.0;

    float edge = 1.0 - smoothstep(0.0, width, thickness);

    return clamp(edge * mix(0.45, 1.0, pattern) * intensity, 0.0, 1.0);
}

/*
 * Where on the screen the bottom is seen through a ripple: the surface tilts the view, so the point is shifted along the tilt
 * (the horizontal part of the normal). Where the water is thin the shift fades out: there the shore and the objects in the water
 * would be dragged over the water.
 */
vec2 getRefractedCoordinates(vec2 coordinates, vec3 normal, float strength, float thickness, float depthDistance) {
    float fade = clamp(thickness / max(depthDistance * 0.1, 0.0001), 0.0, 1.0);

    return coordinates + normal.xz * strength * fade;
}

/*
 * Lines of foam that run to the shore from the deep, one after another: the waves that come in. They are where the water is thin
 * enough to break (from twice the width of the foam of the shore up to ten widths of it), and move to the shore with time.
 */
float getFoamBands(float thickness, float width, float intensity, float time) {
    if (width <= 0.0 || intensity <= 0.0)
        return 0.0;

    float phase = thickness / width * 0.45 + time * 0.12;
    float lines = smoothstep(0.62, 0.95, 0.5 + 0.5 * sin(6.28318531 * phase));
    float zone = smoothstep(width * 0.4, width * 1.2, thickness) * (1.0 - smoothstep(width * 3.0, width * 10.0, thickness));

    return clamp(lines * zone * intensity, 0.0, 1.0);
}

/*
 * How opaque the water is where it is thin: it fades out over a few centimeters of depth, so the edge of the water on the shore is
 * soft and doesn't show the triangles of its grid, which cut the ground in straight lines. The depth is the height of the water over
 * the ground under it (not the length of the view through it).
 */
float getShoreFade(float depth, float edge) {
    if (edge <= 0.0)
        return 1.0;

    return smoothstep(0.0, edge, depth);
}
