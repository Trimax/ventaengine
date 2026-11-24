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

/*
 * The tangent space of a vertex. A vertex whose triangles have degenerate texture coordinates has no tangent (the mesh
 * loader leaves zeros), and normalizing a zero vector gives NaN that turns every triangle that uses the vertex black.
 * Such a vertex gets any tangent space around its normal: it has no texture detail to follow anyway.
 */
mat3 getTangentSpace(mat3 normalMatrix) {
    vec3 n = normalize(normalMatrix * normal);
    vec3 t = normalMatrix * tangent;
    vec3 b = normalMatrix * bitangent;

    if (dot(t, t) < 1e-12 || dot(b, b) < 1e-12) {
        vec3 axis = abs(n.y) < 0.99 ? vec3(0.0, 1.0, 0.0) : vec3(1.0, 0.0, 0.0);
        t = normalize(cross(axis, n));
        b = cross(n, t);
        return mat3(t, b, n);
    }

    return mat3(normalize(t), normalize(b), n);
}

void main() {
#ifdef INSTANCED
    mat4 model = instanceModel;
    mat3 normalMatrix = instanceNormal;
#else
    mat4 model = matrixModel;
    mat3 normalMatrix = matrixNormal;
#endif

    vec4 worldPos = model * vec4(position, 1.0);

    vertexTBN = getTangentSpace(normalMatrix);

    vertexTextureCoordinates = textureCoordinates;
    vertexCameraPosition = cameraPosition;
    vertexPosition = worldPos.xyz;
    vertexColor = color;

    gl_Position = matrixViewProjection * worldPos;
}
