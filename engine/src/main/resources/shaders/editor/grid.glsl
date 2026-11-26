#version 330 core

/*
 * The grid of the ground that an editor shows: lines every meter and heavier ones every ten, the axes x (red) and z (blue) through
 * the origin. Every pixel finds where its view ray meets the plane y = 0, so the grid has no edge. It is drawn over the finished
 * image: what the scene has in front of the ground hides it.
 */
in vec2 vertexTextureCoordinates;

uniform mat4 inverseViewProjection;
uniform vec3 cameraPosition;
uniform float fadeDistance;
uniform sampler2D textureDepth;

out vec4 outputColor;

const vec3 GRID_COLOR = vec3(0.62, 0.66, 0.72);
const vec3 AXIS_X_COLOR = vec3(0.90, 0.32, 0.30);
const vec3 AXIS_Z_COLOR = vec3(0.30, 0.52, 0.95);

vec3 unproject(vec2 ndc, float z) {
    vec4 point = inverseViewProjection * vec4(ndc, z, 1.0);
    return point.xyz / point.w;
}

/* How much of a pixel the line of a grid with cells of this size covers; lines closer than a few pixels fade out, they would only shimmer */
float getLine(vec2 coordinate, float cell) {
    vec2 scaled = coordinate / cell;
    vec2 width = max(fwidth(scaled), vec2(1e-6));
    vec2 distance = abs(fract(scaled - 0.5) - 0.5) / width;

    float line = 1.0 - clamp(min(distance.x, distance.y), 0.0, 1.0);
    return line * (1.0 - smoothstep(0.15, 0.45, max(width.x, width.y)));
}

void main() {
    vec2 ndc = vertexTextureCoordinates * 2.0 - 1.0;
    vec3 near = unproject(ndc, -1.0);
    vec3 far = unproject(ndc, 1.0);

    float slope = far.y - near.y;
    float along = -near.y / (abs(slope) < 1e-9 ? 1e-9 : slope);
    if (along <= 0.0 || along >= 1.0)
        discard;

    vec3 world = near + along * (far - near);

    // The ground is hidden where something of the scene is nearer than it
    float sceneDepth = texelFetch(textureDepth, ivec2(gl_FragCoord.xy), 0).r;
    float groundDepth = length(world - cameraPosition);
    vec3 sceneNear = unproject(ndc, sceneDepth * 2.0 - 1.0);
    if (sceneDepth < 1.0 && length(sceneNear - cameraPosition) < groundDepth - 0.01)
        discard;

    vec2 width = max(fwidth(world.xz), vec2(1e-6)) * 1.5;
    float axisX = 1.0 - clamp(abs(world.z) / width.y, 0.0, 1.0);
    float axisZ = 1.0 - clamp(abs(world.x) / width.x, 0.0, 1.0);

    float fine = getLine(world.xz, 1.0) * 0.22;
    float coarse = getLine(world.xz, 10.0) * 0.45;
    float fade = 1.0 - smoothstep(fadeDistance * 0.4, fadeDistance, groundDepth);

    float grid = max(fine, coarse);
    float axis = max(axisX, axisZ);
    vec3 color = axisX > axisZ ? AXIS_X_COLOR : AXIS_Z_COLOR;

    float alpha = max(grid, axis * 0.85) * fade;
    if (alpha < 0.002)
        discard;

    outputColor = vec4(axis > grid ? color : GRID_COLOR, alpha);
}
