/*
 * The bloom: the bright parts of the HDR image are blurred over several scales and added back. These are the pieces of
 * its passes: what is bright enough to glow, and the weight that keeps a single very bright pixel from flickering.
 */

/* The brightness of a color: its strongest channel */
float bloomBrightness(vec3 color) {
    return max(color.r, max(color.g, color.b));
}

/*
 * The part of a color that glows: nothing below the threshold, the whole excess above it, and a soft quadratic knee
 * around it so the glow doesn't start with an edge. The knee is a share of the threshold.
 */
vec3 bloomPrefilter(vec3 color, float threshold, float knee) {
    float brightness = bloomBrightness(color);
    float width = threshold * knee + 1e-4;

    float soft = clamp(brightness - threshold + width, 0.0, 2.0 * width);
    soft = soft * soft / (4.0 * width);

    float contribution = max(soft, brightness - threshold) / max(brightness, 1e-4);
    return color * contribution;
}

/* The weight of a color in an average: a pixel that is very bright counts less, so one spark doesn't light the whole blur */
float bloomKarisWeight(vec3 color) {
    return 1.0 / (1.0 + dot(color, vec3(0.2126, 0.7152, 0.0722)));
}
