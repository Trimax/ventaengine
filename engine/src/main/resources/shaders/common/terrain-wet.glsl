#include "common/swash.glsl"

/*
 * The wet ground of a shore. The ground is wet under the water, which runs up the shore and back by the swash, and a bit higher
 * than that: the sand dries slowly. Both parts fade out upwards, the first over the height of the wet ground, the second
 * (damp, not wet) over half of it.
 */

/* From 0 (dry) to 1 (wet) for the height of a point of the ground: the level is the one of the water, the wave breaks the line a bit (irregularity, in the units of the world) */
float getWetness(float height, float level, float wetHeight, float amplitude, float period, float time, float irregularity) {
    float water = level + getSwash(amplitude, period, time) + irregularity;
    float wet = 1.0 - smoothstep(water, water + wetHeight, height);

    // What the water reached and left is damp for a while
    float reach = level + amplitude + irregularity;
    float damp = (1.0 - smoothstep(reach, reach + wetHeight * 0.5, height)) * 0.6;

    return max(wet, damp);
}
