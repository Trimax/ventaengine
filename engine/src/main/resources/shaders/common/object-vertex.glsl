/*
 * The vertex shader of the objects, three variants are made of this file: default/vertex.glsl draws one object with
 * the matrices of its uniforms, default/vertex-instanced.glsl (it defines INSTANCED) draws many copies of a mesh in one
 * call and takes the matrices of every copy from the vertex attributes, and default/vertex-instanced-facing.glsl (it
 * defines FACING as well) draws a flat card that is turned to the camera around the vertical axis: the last level of
 * detail of an object, a picture of it. The card is a mesh in the plane of x and y, its size is the scale of the matrix.
 */

#if defined(FACING) && !defined(INSTANCED)
#error A card that is turned to the camera is drawn with instancing
#endif
#include "common/camera.glsl"
#include "common/wind.glsl"

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
layout(location = 13) in float instanceFade;    // How far the copy is drawn while a level of detail fades, see common/lod.glsl
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

#ifdef INSTANCED
flat out float vertexFade;
#endif

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

#ifdef FACING
    // The card stands upright and looks at the camera: right, up and front are its axes
    vec3 center = model[3].xyz;
    vec3 toCamera = vec3(cameraPosition.x - center.x, 0.0, cameraPosition.z - center.z);
    vec3 front = dot(toCamera, toCamera) > 1e-8 ? normalize(toCamera) : vec3(0.0, 0.0, 1.0);
    vec3 right = vec3(front.z, 0.0, -front.x);
    vec3 up = vec3(0.0, 1.0, 0.0);
    vec3 size = vec3(length(model[0].xyz), length(model[1].xyz), length(model[2].xyz));

    worldPos.xyz = center + right * (position.x * size.x) + up * (position.y * size.y) + front * (position.z * size.z);
    vertexTBN = mat3(right, up, front);
#else
    vertexTBN = getTangentSpace(normalMatrix);
#endif

    worldPos.xyz = applyWind(worldPos.xyz, model);

    vertexTextureCoordinates = textureCoordinates;
    vertexCameraPosition = cameraPosition;
    vertexPosition = worldPos.xyz;
    vertexColor = color;

#ifdef INSTANCED
    vertexFade = instanceFade;
#endif

    gl_Position = matrixViewProjection * worldPos;
}
