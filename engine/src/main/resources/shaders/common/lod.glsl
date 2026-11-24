/*
 * The cross fade between two levels of detail of an object: the level that fades in and the level that fades out are drawn
 * with the opposite dither, so every pixel belongs to one of them and the two don't blend (they would need sorting).
 * The fade of a copy comes from the vertex shader: 1 is a copy that is drawn whole, 0 to 1 is a level that fades in (this
 * share of the pixels is drawn), 0 to -1 is a level that fades out (the absolute value is the share of the pixels).
 */

/* Interleaved gradient noise: a dither of 0 to 1 that is even over a few pixels and needs no texture */
float lodDither(vec2 pixel) {
    return fract(52.9829189 * fract(dot(pixel, vec2(0.06711056, 0.00583715))));
}

/* Whether the pixel of a copy that fades is drawn, the pixel is gl_FragCoord.xy */
bool lodVisible(float fade, vec2 pixel) {
    float dither = lodDither(pixel);

    return fade >= 0.0 ? dither < fade : 1.0 - dither < -fade;
}
