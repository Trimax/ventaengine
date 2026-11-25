/*
 * How much of the waves of the water is left at a distance from the camera. Far away the waves are smaller than a pixel and the
 * grid of the water is coarse, so they fade out into a flat sea, which looks the same and doesn't shimmer.
 */

/* 1 near the camera, 0 at the distance of the fade, and a smooth fall between (from a third of it) (0 is no fade) */
float getWaveFade(float distance, float fadeDistance) {
    if (fadeDistance <= 0.0)
        return 1.0;

    return 1.0 - smoothstep(fadeDistance * 0.35, fadeDistance, distance);
}
