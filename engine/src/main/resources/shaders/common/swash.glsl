/*
 * The swash of a shore: the water runs up the beach and back. The water of the sea and the wet sand of the beach take their
 * level from this function (with the same numbers), so the line where the sand gets wet follows the water.
 */

/* How high above its level the water is at a time: from 0 to the amplitude and back, once in a period (0 is none) */
float getSwash(float amplitude, float period, float time) {
    if (amplitude <= 0.0 || period <= 0.0)
        return 0.0;

    return amplitude * (0.5 + 0.5 * sin(6.28318531 * time / period));
}
