#include "common/flags.glsl"
#include "common/lights.glsl"

/*
 * Shadows of the point lights: a few of the nearest lights that cast shadows have a cube of depths, whose six faces are
 * the layers of one texture array (a block of six for every slot, in the order +x, -x, +y, -y, +z, -z). A point is seen
 * from its light through the face it lies the most along, and the sampler compares its depth with the stored one.
 */

const int MAX_POINT_SHADOWS = 4;
const int POINT_SHADOW_FACES = 6;

/* How far a point is moved before it is compared with the map, in texels of the map at its distance from the light */
const float POINT_SHADOW_NORMAL_OFFSET = 1.5;
const float POINT_SHADOW_LIGHT_OFFSET = 1.5;

uniform sampler2DArrayShadow texturePointShadow;
uniform int usePointShadows;
uniform mat4 pointShadowMatrices[MAX_POINT_SHADOWS * POINT_SHADOW_FACES];

/* The face of the cube that a direction from the light goes through: the axis it lies the most along, and the side of it */
int getPointShadowFace(vec3 direction) {
    vec3 absolute = abs(direction);

    if (absolute.x >= absolute.y && absolute.x >= absolute.z)
        return direction.x >= 0.0 ? 0 : 1;

    if (absolute.y >= absolute.z)
        return direction.y >= 0.0 ? 2 : 3;

    return direction.z >= 0.0 ? 4 : 5;
}

/*
 * How much of the light of a point light reaches a point: 1 is all of it, 0 is none (the point is in a shadow). A light
 * without a map lights everything. The normal is the geometric normal of the surface in the world space.
 */
float getPointShadow(Light light, vec3 position, vec3 normal) {
    if (!isSet(usePointShadows) || light.shadowSlot < 0)
        return 1.0;

    vec3 toPoint = position - light.position;
    float distance = length(toPoint);
    if (distance < 1e-4)
        return 1.0;

    // A face sees 90 degrees, so a texel of the map is about 2 / resolution of the distance from the light wide
    ivec2 size = textureSize(texturePointShadow, 0).xy;
    float texel = 2.0 * distance / float(size.x);

    vec3 toLight = -toPoint / distance;
    float cosine = clamp(dot(normal, toLight), 0.0, 1.0);
    float sine = sqrt(1.0 - cosine * cosine);
    vec3 shifted = position + normal * (texel * POINT_SHADOW_NORMAL_OFFSET * sine) + toLight * (texel * POINT_SHADOW_LIGHT_OFFSET);

    int face = getPointShadowFace(shifted - light.position);
    int layer = light.shadowSlot * POINT_SHADOW_FACES + face;

    vec4 clip = pointShadowMatrices[layer] * vec4(shifted, 1.0);
    vec3 coordinates = clip.xyz / clip.w * 0.5 + 0.5;

    // Beyond the far plane of the faces (the range of the light) there is no map: the light itself is gone there
    if (coordinates.z > 1.0)
        return 1.0;

    vec2 step = 1.0 / vec2(size);
    float lit = 0.0;
    for (int x = -1; x <= 1; x++)
        for (int y = -1; y <= 1; y++)
            lit += texture(texturePointShadow, vec4(coordinates.xy + vec2(x, y) * step, float(layer), coordinates.z));

    return lit / 9.0;
}
