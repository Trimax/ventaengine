#version 330 core

/*
 * FXAA (Timothy Lottes): smooths the edges of a finished image, tone mapped and encoded to sRGB, by the contrast of its
 * brightness. A pixel without contrast around it stays as it is; on an edge the search goes along the edge in both
 * directions to find where it ends, and the pixel is moved across the edge by how far it is from the nearest end, so the
 * bilinear sample takes the color of the other side in the share of the coverage. The lines thinner than a pixel are
 * smoothed by the difference of the pixel from the average of its neighbours.
 */

/***
 * Definitions
 ***/

/* The least contrast of the brightness that is an edge, whatever the threshold: the dark parts are not worth the blur */
const float EDGE_THRESHOLD_MINIMAL = 0.0312;

/***
 * Input variables and uniforms
 ***/

/* Vertex shader output */
in vec2 vertexTextureCoordinates;

/* The image to smooth, with linear filtering */
uniform sampler2D textureScene;

/* The size of a pixel in the texture coordinates */
uniform vec2 texelSize;

/* How much the lines thinner than a pixel are smoothed, from 0 to 1 */
uniform float fxaaSubpixel;

/* The contrast of the brightness (a share of the brightest neighbour) from which a pixel is on an edge */
uniform float fxaaEdgeThreshold;

/* How many steps along an edge are made in each direction, at most */
uniform int fxaaSteps;

/* Output color */
out vec4 outputColor;

/***
 * Functions
 ***/

float getLuma(vec3 color) {
    return dot(color, vec3(0.299, 0.587, 0.114));
}

float getLumaAt(vec2 coordinates) {
    return getLuma(textureLod(textureScene, coordinates, 0.0).rgb);
}

float getLumaAt(ivec2 pixel) {
    return getLuma(texelFetch(textureScene, pixel, 0).rgb);
}

void main() {
    vec2 uv = vertexTextureCoordinates;
    ivec2 pixel = ivec2(gl_FragCoord.xy);
    ivec2 last = textureSize(textureScene, 0) - 1;

    vec3 colorCenter = texelFetch(textureScene, pixel, 0).rgb;
    float lumaM = getLuma(colorCenter);
    float lumaN = getLumaAt(clamp(pixel + ivec2(0, 1), ivec2(0), last));
    float lumaS = getLumaAt(clamp(pixel + ivec2(0, -1), ivec2(0), last));
    float lumaW = getLumaAt(clamp(pixel + ivec2(-1, 0), ivec2(0), last));
    float lumaE = getLumaAt(clamp(pixel + ivec2(1, 0), ivec2(0), last));

    float lumaMax = max(lumaM, max(max(lumaN, lumaS), max(lumaW, lumaE)));
    float lumaMin = min(lumaM, min(min(lumaN, lumaS), min(lumaW, lumaE)));
    float lumaRange = lumaMax - lumaMin;

    // No contrast, no edge
    if (lumaRange < max(EDGE_THRESHOLD_MINIMAL, lumaMax * fxaaEdgeThreshold)) {
        outputColor = vec4(colorCenter, 1.0);
        return;
    }

    float lumaNW = getLumaAt(clamp(pixel + ivec2(-1, 1), ivec2(0), last));
    float lumaNE = getLumaAt(clamp(pixel + ivec2(1, 1), ivec2(0), last));
    float lumaSW = getLumaAt(clamp(pixel + ivec2(-1, -1), ivec2(0), last));
    float lumaSE = getLumaAt(clamp(pixel + ivec2(1, -1), ivec2(0), last));

    // The edge is horizontal when the brightness changes more up and down than left and right: the second differences are taken across it
    float edgeHorizontal = abs(lumaNW + lumaSW - 2.0 * lumaW) + 2.0 * abs(lumaN + lumaS - 2.0 * lumaM) + abs(lumaNE + lumaSE - 2.0 * lumaE);
    float edgeVertical = abs(lumaNW + lumaNE - 2.0 * lumaN) + 2.0 * abs(lumaW + lumaE - 2.0 * lumaM) + abs(lumaSW + lumaSE - 2.0 * lumaS);
    bool isHorizontal = edgeHorizontal >= edgeVertical;

    // The side of the edge with the steepest change of the brightness is the one the edge is searched from
    float luma1 = isHorizontal ? lumaS : lumaW;
    float luma2 = isHorizontal ? lumaN : lumaE;
    float gradient1 = luma1 - lumaM;
    float gradient2 = luma2 - lumaM;
    bool isSteepest1 = abs(gradient1) >= abs(gradient2);
    float gradientScaled = 0.25 * max(abs(gradient1), abs(gradient2));

    float stepLength = isHorizontal ? texelSize.y : texelSize.x;
    float lumaAverageLocal;
    if (isSteepest1) {
        stepLength = -stepLength;
        lumaAverageLocal = 0.5 * (luma1 + lumaM);
    } else {
        lumaAverageLocal = 0.5 * (luma2 + lumaM);
    }

    // The search goes along the edge from the middle of the pixel to the edge between the pixels
    vec2 current = uv;
    if (isHorizontal)
        current.y += stepLength * 0.5;
    else
        current.x += stepLength * 0.5;

    vec2 offset = isHorizontal ? vec2(texelSize.x, 0.0) : vec2(0.0, texelSize.y);
    vec2 uv1 = current - offset;
    vec2 uv2 = current + offset;

    float lumaEnd1 = getLumaAt(uv1) - lumaAverageLocal;
    float lumaEnd2 = getLumaAt(uv2) - lumaAverageLocal;
    bool reached1 = abs(lumaEnd1) >= gradientScaled;
    bool reached2 = abs(lumaEnd2) >= gradientScaled;

    // The steps grow after the first ones: a long edge is found with fewer samples
    for (int i = 1; i < fxaaSteps && !(reached1 && reached2); i++) {
        float stride = 1.0 + 0.5 * float(max(i - 4, 0));

        if (!reached1)
            uv1 -= offset * stride;
        if (!reached2)
            uv2 += offset * stride;

        if (!reached1) {
            lumaEnd1 = getLumaAt(uv1) - lumaAverageLocal;
            reached1 = abs(lumaEnd1) >= gradientScaled;
        }
        if (!reached2) {
            lumaEnd2 = getLumaAt(uv2) - lumaAverageLocal;
            reached2 = abs(lumaEnd2) >= gradientScaled;
        }
    }

    float distance1 = isHorizontal ? uv.x - uv1.x : uv.y - uv1.y;
    float distance2 = isHorizontal ? uv2.x - uv.x : uv2.y - uv.y;
    bool isNearer1 = distance1 < distance2;
    float distanceNearest = min(distance1, distance2);
    float edgeLength = distance1 + distance2;

    // The pixel is moved across the edge only where the end of the edge changes the brightness the way this pixel does
    bool isCenterSmaller = lumaM < lumaAverageLocal;
    bool isVariationRight = ((isNearer1 ? lumaEnd1 : lumaEnd2) < 0.0) != isCenterSmaller;
    float offsetFinal = isVariationRight ? 0.5 - distanceNearest / edgeLength : 0.0;

    // A line thinner than a pixel: the pixel differs from the average of its neighbours, and moves by that
    float lumaAverage = (1.0 / 12.0) * (2.0 * (lumaN + lumaS + lumaW + lumaE) + lumaNW + lumaNE + lumaSW + lumaSE);
    float subpixel = clamp(abs(lumaAverage - lumaM) / lumaRange, 0.0, 1.0);
    subpixel = (-2.0 * subpixel + 3.0) * subpixel * subpixel;
    offsetFinal = max(offsetFinal, subpixel * subpixel * fxaaSubpixel);

    vec2 coordinates = uv;
    if (isHorizontal)
        coordinates.y += offsetFinal * stepLength;
    else
        coordinates.x += offsetFinal * stepLength;

    outputColor = vec4(textureLod(textureScene, coordinates, 0.0).rgb, 1.0);
}
