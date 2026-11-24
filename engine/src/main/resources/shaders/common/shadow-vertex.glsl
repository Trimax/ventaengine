/*
 * The vertex shader of the shadows of the objects: the sun looks at the scene. Four variants are made of this file:
 * a single object takes its model matrix from the uniform, many copies of a mesh (INSTANCED) take it from the vertex
 * attributes, and a cutout surface (ALPHA_CUTOUT) also passes its alpha to the fragment shader, which discards the holes.
 */

#include "common/wind.glsl"

/* Vertex attributes */
layout(location = 0) in vec3 position;

#ifdef ALPHA_CUTOUT
layout(location = 4) in vec2 textureCoordinates;
layout(location = 5) in vec4 color;
#endif

/* Matrices */
uniform mat4 matrixViewProjection;

#ifdef INSTANCED
layout(location = 6) in mat4 instanceModel;     // Model matrix of the copy (takes the locations 6 to 9)
#else
uniform mat4 matrixModel;
#endif

#ifdef ALPHA_CUTOUT
/* The alpha of the surface decides whether a fragment casts a shadow */
out vec2 vertexTextureCoordinates;
out vec4 vertexColor;
#endif

void main() {
#ifdef INSTANCED
    mat4 model = instanceModel;
#else
    mat4 model = matrixModel;
#endif

#ifdef ALPHA_CUTOUT
    vertexTextureCoordinates = textureCoordinates;
    vertexColor = color;
#endif

    vec3 worldPosition = applyWind((model * vec4(position, 1.0)).xyz, model);

    gl_Position = matrixViewProjection * vec4(worldPosition, 1.0);
}
