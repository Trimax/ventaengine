#version 330 core

/*
 * The color of a line: what the scene has in front of it dims it, so the lines of a frame behind a wall are seen faintly.
 */
in vec4 vertexColor;

uniform sampler2D textureDepth;

// How strong a line is when the scene is in front of it: the handles of the gizmo are always seen, so they say 1
uniform float hiddenAlpha = 0.28;

out vec4 outputColor;

const float DEPTH_BIAS = 0.00015;

void main() {
    float sceneDepth = texelFetch(textureDepth, ivec2(gl_FragCoord.xy), 0).r;
    float hidden = gl_FragCoord.z > sceneDepth + DEPTH_BIAS ? 1.0 : 0.0;

    outputColor = vec4(vertexColor.rgb, vertexColor.a * mix(1.0, hiddenAlpha, hidden));
}
