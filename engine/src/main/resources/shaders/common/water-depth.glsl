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
