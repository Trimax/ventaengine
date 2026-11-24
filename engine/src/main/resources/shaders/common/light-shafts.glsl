/*
 * The shafts of the light of the sun: the light that is scattered in the exponential fog towards the camera. A view ray is
 * marched in steps, and a step adds the light of the sun that reaches its point (the shadow maps tell how much does)
 * scattered in the fog there, weakened by the fog between the point and the camera. In a fog of a constant density that
 * is not shadowed the sum of the steps is 1 - exp(-the thickness of the fog) however many steps there are. Where something shadows the sun, the
 * ray adds nothing, so the gaps between the trees and the towers are bright and the shadows are dark: shafts.
 */

#include "common/fog.glsl"
#include "common/lod.glsl"
#include "common/shadow.glsl"

/* The depth of the scene, one value for a pixel of the image, and the inverse of its view and projection matrices */
uniform sampler2D textureDepth;
uniform mat4 matrixViewProjectionInverse;

uniform int lightShaftsSteps;
uniform float lightShaftsAnisotropy;

/* The part of the light that fog scatters towards a direction: the cosine is taken between the ray and the direction to the sun */
float getShaftsPhase(float cosine, float anisotropy) {
    float square = anisotropy * anisotropy;

    // The phase function of Henyey and Greenstein, scaled so that the scattering in all directions equally is 1
    return (1.0 - square) / pow(max(1.0 + square - 2.0 * anisotropy * cosine, 1e-4), 1.5);
}

/* The point of the world a pixel sees, the pixel is in [0, 1] and the depth is the value of the depth buffer */
vec3 getShaftsPosition(vec2 pixel, float depth) {
    vec4 position = matrixViewProjectionInverse * vec4(vec3(pixel, depth) * 2.0 - 1.0, 1.0);
    return position.xyz / position.w;
}

/*
 * How far a view ray is marched: to the point the pixel sees, but not farther than the shadows reach (beyond them
 * everything is lit, so it would only make a haze that the fog already has). The sky has no point: its rays go as far
 * as the shadows.
 */
float getShaftsLength(vec2 pixel, float depth, out vec3 direction) {
    vec3 way = getShaftsPosition(pixel, depth) - cameraPosition;
    float span = max(length(way), 1e-4);

    direction = way / span;
    return depth >= 1.0 ? shadowDistance : min(span, shadowDistance);
}

/* How much a sample of the shafts of a lower resolution counts for a pixel: the more alike their distances, the more */
float getShaftsWeight(float rayLength, float sampled) {
    return 1.0 / (abs(rayLength - sampled) / max(max(rayLength, sampled), 1.0) + 0.02);
}

/*
 * The light of the sun scattered towards the camera along a ray.
 *
 * @param direction the unit direction of the ray
 * @param rayLength how far the ray is marched
 * @param jitter    where in the first step the march starts, from 0 to 1: it differs between the pixels, so the steps make noise and not bands
 */
vec3 getLightShafts(vec3 direction, float rayLength, float jitter) {
    float stepLength = rayLength / float(lightShaftsSteps);

    float thickness = 0.0;
    float scattered = 0.0;
    for (int step = 0; step < lightShaftsSteps; step++) {
        vec3 point = cameraPosition + direction * ((float(step) + jitter) * stepLength);
        float extinction = getFogDensity(point.y) * stepLength;

        // The fog of a step scatters the part of the light that it doesn't let through (so a step of a thick fog can't scatter more
        // than there is), and the light that arrives from the camera's side of it is weakened by the fog before the step
        scattered += getSunVisibility(point) * (1.0 - exp(-extinction)) * exp(-thickness);
        thickness += extinction;
    }

    float cosine = dot(direction, normalize(-directionalLight.direction));
    return directionalLight.color * directionalLight.intensity * scattered * getShaftsPhase(cosine, lightShaftsAnisotropy);
}
