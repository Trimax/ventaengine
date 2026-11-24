/*
 * The wind sways the surfaces that follow it (the wind influence of a material): the top of a mesh leans along the wind
 * and the gusts swing it, the bottom stays where it is. It is the same function for the surface and for its shadow, so the
 * shadow sways with the crown. The scene without wind (windStrength is 0) or a material that doesn't follow it are not moved.
 */
uniform vec2 windDirection;         // The unit direction the wind blows to: x and z of the world
uniform float windStrength;         // How far (in the units of the world) the top of a surface that fully follows the wind is pushed
uniform float windSpeed;            // How fast the gusts follow each other
uniform float windInfluence;        // How much the material follows the wind, from 0 to 1
uniform vec3 meshMinimum;           // The bounding box of the mesh in its own space
uniform vec3 meshMaximum;
uniform float timeElapsed;          // Seconds since the start

/*
 * @param worldPosition the position of the vertex in the world
 * @param model         the matrix of the mesh: the box of the mesh in the world tells how high a vertex is, however the mesh is
 *                      turned or scaled, and its position is the phase (the copies of a mesh are out of step, so the trees
 *                      don't sway together)
 */
vec3 applyWind(vec3 worldPosition, mat4 model) {
    float amount = windStrength * windInfluence;
    if (amount <= 0.0)
        return worldPosition;

    // The lowest and the highest point of the box of the mesh in the world: the row of the matrix that makes the y
    vec3 row = vec3(model[0].y, model[1].y, model[2].y);
    vec3 low = row * meshMinimum;
    vec3 high = row * meshMaximum;
    float bottom = model[3].y + dot(min(low, high), vec3(1.0));
    float top = model[3].y + dot(max(low, high), vec3(1.0));

    // A stem bends the more the higher a point is: the bottom stays, the top moves the most
    float height = clamp((worldPosition.y - bottom) / max(top - bottom, 1e-4), 0.0, 1.0);
    height *= height;

    float phase = dot(model[3].xz, vec2(0.37, 0.23));
    float time = timeElapsed * windSpeed;

    // The wind leans the tree along its direction (never against it) and the gusts swing it; the leaves flutter across
    float lean = 0.65 + 0.25 * sin(time + phase) + 0.1 * sin(2.7 * time + 1.9 * phase);
    float flutter = 0.12 * sin(5.3 * time + 3.1 * phase + worldPosition.x * 1.7 + worldPosition.z * 1.3);

    vec2 across = vec2(-windDirection.y, windDirection.x);
    vec2 offset = (windDirection * lean + across * flutter) * amount * height;

    return worldPosition + vec3(offset.x, 0.0, offset.y);
}
