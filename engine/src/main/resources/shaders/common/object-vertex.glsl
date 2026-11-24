/*
 * The vertex shader of the objects, two variants are made of this file: default/vertex.glsl draws one object with
 * the matrices of its uniforms, default/vertex-instanced.glsl (it defines INSTANCED) draws many copies of a mesh in one
 * call and takes the matrices of every copy from the vertex attributes.
 */
#include "common/camera.glsl"

/* Vertex attributes */
layout(location = 0) in vec3 position;
layout(location = 1) in vec3 normal;
layout(location = 2) in vec3 tangent;
layout(location = 3) in vec3 bitangent;
layout(location = 4) in vec2 textureCoordinates;
layout(location = 5) in vec4 color;

/* Matrices */
uniform mat4 matrixViewProjection;  // Multiplied View & Projections matrices (built based on the window & camera parameters)

#ifdef INSTANCED
layout(location = 6) in mat4 instanceModel;     // Model matrix of the copy (takes the locations 6 to 9)
layout(location = 10) in mat3 instanceNormal;   // Normal matrix of the copy (takes the locations 10 to 12)
#else
uniform mat3 matrixNormal;          // Multiplied P * R * S
uniform mat4 matrixModel;           // Normal matrix computed by model matrix
#endif

/* Parameters going to fragment shader */
out mat3 vertexTBN;
out vec4 vertexColor;
out vec3 vertexPosition;
out vec3 vertexCameraPosition;
out vec2 vertexTextureCoordinates;

void main() {
#ifdef INSTANCED
    mat4 model = instanceModel;
    mat3 normalMatrix = instanceNormal;
#else
    mat4 model = matrixModel;
    mat3 normalMatrix = matrixNormal;
#endif

    vec4 worldPos = model * vec4(position, 1.0);

    vertexTBN = mat3(normalize(normalMatrix * tangent),
                     normalize(normalMatrix * bitangent),
                     normalize(normalMatrix * normal));

    vertexTextureCoordinates = textureCoordinates;
    vertexCameraPosition = cameraPosition;
    vertexPosition = worldPos.xyz;
    vertexColor = color;

    gl_Position = matrixViewProjection * worldPos;
}
