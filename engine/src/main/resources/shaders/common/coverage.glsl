/*
 * Alpha to coverage for the surfaces with holes (the cutout materials). The hardware turns the alpha that a fragment outputs into the
 * share of the samples of the pixel that it covers, so the edge of a hole is smoothed by the multisampling like the edge of a polygon.
 * The alpha is sharpened first: the line where it crosses the cutoff stays one pixel wide at any distance (a mip level of a texture blurs
 * the alpha, and a leaf far away would get thin and then vanish).
 */

/*
 * The alpha that becomes the coverage: 0 where the surface is a hole, 1 where it is solid, and between them only across the width of
 * one pixel around the line where the alpha of the material is the cutoff.
 *
 * `width` is how much the alpha changes from a pixel to the next one (fwidth of the alpha).
 */
float getSharpenedAlpha(float alpha, float cutoff, float width) {
    return clamp((alpha - cutoff) / max(width, 0.0001) + 0.5, 0.0, 1.0);
}
