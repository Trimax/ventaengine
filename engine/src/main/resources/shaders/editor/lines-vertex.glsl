#version 330 core

/*
 * Lines of a constant width in pixels (the core profile has no wide lines): every segment is a quad of two triangles that is
 * widened in the screen space. The segments are in the uniforms, so one draw makes up to MAX_SEGMENTS of them; the part of a
 * segment behind the camera is cut off before it is projected.
 */
#define MAX_SEGMENTS 96

uniform mat4 viewMatrix;
uniform mat4 projectionMatrix;
uniform vec2 viewportSize;
uniform float thickness;
uniform float nearPlane;
uniform vec3 segmentStart[MAX_SEGMENTS];
uniform vec3 segmentEnd[MAX_SEGMENTS];
uniform vec4 segmentColor[MAX_SEGMENTS];

out vec4 vertexColor;

/* The corners of the two triangles of a quad: which end of the segment, and which side of it */
const int CORNER_END[6] = int[](0, 1, 0, 0, 1, 1);
const float CORNER_SIDE[6] = float[](-1.0, -1.0, 1.0, 1.0, -1.0, 1.0);

void main() {
    int segment = gl_VertexID / 6;
    int corner = gl_VertexID % 6;

    vec3 start = (viewMatrix * vec4(segmentStart[segment], 1.0)).xyz;
    vec3 end = (viewMatrix * vec4(segmentEnd[segment], 1.0)).xyz;

    // Behind the near plane the projection is not defined: the segment ends where it crosses the plane
    float limit = -nearPlane;
    if (start.z > limit && end.z > limit) {
        gl_Position = vec4(2.0, 2.0, 2.0, 1.0);
        vertexColor = vec4(0.0);
        return;
    }

    if (start.z > limit)
        start = mix(start, end, (limit - start.z) / (end.z - start.z));
    else if (end.z > limit)
        end = mix(end, start, (limit - end.z) / (start.z - end.z));

    vec4 clipStart = projectionMatrix * vec4(start, 1.0);
    vec4 clipEnd = projectionMatrix * vec4(end, 1.0);
    vec2 pixelStart = clipStart.xy / clipStart.w * 0.5 * viewportSize;
    vec2 pixelEnd = clipEnd.xy / clipEnd.w * 0.5 * viewportSize;

    vec2 direction = pixelEnd - pixelStart;
    direction = length(direction) < 1e-4 ? vec2(1.0, 0.0) : normalize(direction);
    vec2 normal = vec2(-direction.y, direction.x);

    vec4 clip = CORNER_END[corner] == 0 ? clipStart : clipEnd;
    vec2 offset = normal * CORNER_SIDE[corner] * thickness * 0.5 / (0.5 * viewportSize);

    gl_Position = vec4(clip.xy + offset * clip.w, clip.z, clip.w);
    vertexColor = segmentColor[segment];
}
