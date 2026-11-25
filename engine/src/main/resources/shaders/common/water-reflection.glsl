/*
 * The reflection of the scene in water: the scene is drawn as the mirror image of the camera sees it, into a texture, and a
 * point of the water finds what it shows by the projection of the mirror image.
 */

/* How far from the edge of the image (a share of it) the reflection fades into the sky: there is nothing beyond the image */
const float REFLECTION_EDGE = 0.05;

/*
 * Where in the image of the reflection a point of the water is, and how much of the reflection is there: the xy are the
 * coordinates in the image (moved by the ripples: the surface tilts the mirror) and z is the weight, 1 inside of the image,
 * fading to 0 at its edge and behind the mirror image of the camera.
 */
vec3 getReflectionCoordinates(mat4 viewProjection, vec3 position, vec3 normal, float distortion) {
    vec4 clip = viewProjection * vec4(position, 1.0);
    if (clip.w <= 0.0)
        return vec3(0.0);

    vec2 coordinates = clip.xy / clip.w * 0.5 + 0.5 + normal.xz * distortion;
    vec2 edge = min(coordinates, 1.0 - coordinates);
    float weight = smoothstep(0.0, REFLECTION_EDGE, min(edge.x, edge.y));

    return vec3(clamp(coordinates, 0.0, 1.0), weight);
}
