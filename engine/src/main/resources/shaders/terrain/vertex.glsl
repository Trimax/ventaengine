#version 330 core

/* Vertex attributes */
layout(location = 0) in vec3 position;
layout(location = 1) in vec2 textureCoordinates;

/* Matrices */
uniform mat4 matrixViewProjection;
uniform mat4 matrixModel;
uniform mat3 matrixNormal;

/* Time */
uniform float timeElapsed;

/* Heightmap */
uniform sampler2D textureHeight;
uniform float factor;
uniform vec2 gridSize;

/* Parameters going to fragment shader (world space) */
out vec3 vertexNormal;
out vec3 vertexTangent;
out vec3 vertexBitangent;
out vec3 vertexPosition;
out float vertexHeight;
out float vertexTimeElapsed;
out vec2 vertexTextureCoordinates;

float getHeight(vec2 coordinates) {
    return (texture(textureHeight, coordinates).r - 0.5) * factor;
}

void main() {
    float height = getHeight(textureCoordinates);

    /* Slopes of the height field from the neighbour texels: u goes along x, v goes along z */
    vec2 texel = 1.0 / vec2(textureSize(textureHeight, 0));
    vec2 size = max(gridSize, vec2(0.0001));
    float slopeX = (getHeight(textureCoordinates + vec2(texel.x, 0.0)) - getHeight(textureCoordinates - vec2(texel.x, 0.0))) / (2.0 * texel.x * size.x);
    float slopeZ = (getHeight(textureCoordinates + vec2(0.0, texel.y)) - getHeight(textureCoordinates - vec2(0.0, texel.y))) / (2.0 * texel.y * size.y);

    vec3 localPosition = vec3(position.x, height, position.z);
    vec4 worldPosition = matrixModel * vec4(localPosition, 1.0);

    vertexPosition = worldPosition.xyz;
    vertexNormal = normalize(matrixNormal * vec3(-slopeX, 1.0, -slopeZ));
    vertexTangent = normalize(mat3(matrixModel) * vec3(1.0, slopeX, 0.0));
    vertexBitangent = normalize(mat3(matrixModel) * vec3(0.0, slopeZ, 1.0));
    vertexHeight = height;
    vertexTimeElapsed = timeElapsed;
    vertexTextureCoordinates = textureCoordinates;

    gl_Position = matrixViewProjection * worldPosition;
}
