#version 330 core

/* Vertex attributes */
layout(location = 0) in vec3 position;
layout(location = 1) in vec2 textureCoordinates;

/* Matrices */
uniform mat4 matrixViewProjection;
uniform mat4 matrixModel;
uniform mat3 matrixNormal;

/* Camera */
uniform vec3 cameraPosition;

/* Heightmap */
uniform sampler2D textureElevation;
uniform float factor;

/* Parameters going to fragment shader */
out mat3 vertexTBN;
out vec3 vertexPosition;
out vec2 vertexTextureCoordinates;
out vec3 vertexViewDirectionLocalSpace;
out vec3 vertexViewDirectionWorldSpace;

void main() {
    /* Sample center height for displacement */
    float height = texture(textureElevation, textureCoordinates).r;

    /* Displace vertex along Y */
    vec3 displacedPosition = position;
    displacedPosition.y = (height - 0.5) * factor;

    /* Compute world-space position */
    vec4 worldPos = matrixModel * vec4(displacedPosition, 1.0);
    vertexPosition = worldPos.xyz;

    /* Pass through texture coordinates */
    vertexTextureCoordinates = textureCoordinates;

    /* Sample heightmap at 4 adjacent texels for normal computation */
    vec2 texelSize = 1.0 / textureSize(textureElevation, 0);
    vec2 uvL = clamp(textureCoordinates - vec2(texelSize.x, 0.0), 0.0, 1.0);
    vec2 uvR = clamp(textureCoordinates + vec2(texelSize.x, 0.0), 0.0, 1.0);
    vec2 uvD = clamp(textureCoordinates - vec2(0.0, texelSize.y), 0.0, 1.0);
    vec2 uvU = clamp(textureCoordinates + vec2(0.0, texelSize.y), 0.0, 1.0);

    float hL = texture(textureElevation, uvL).r;
    float hR = texture(textureElevation, uvR).r;
    float hD = texture(textureElevation, uvD).r;
    float hU = texture(textureElevation, uvU).r;

    /* Compute normal via central finite differences */
    vec3 normal = normalize(vec3((hL - hR) * factor, 2.0, (hD - hU) * factor));

    /* Construct TBN basis transformed by normal matrix */
    vec3 T = normalize(matrixNormal * vec3(1.0, (hR - hL) * factor * 0.5, 0.0));
    vec3 N = normalize(matrixNormal * normal);
    vec3 B = normalize(cross(N, T));
    vertexTBN = mat3(T, B, N);

    /* Compute view directions */
    vertexViewDirectionWorldSpace = normalize(cameraPosition - worldPos.xyz);
    vertexViewDirectionLocalSpace = normalize(transpose(vertexTBN) * vertexViewDirectionWorldSpace);

    gl_Position = matrixViewProjection * matrixModel * vec4(displacedPosition, 1.0);
}
