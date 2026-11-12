#version 330 core

/***
 * Structures
 ***/

struct Fog {
    vec3 color;
    float minimalDistance;
    float maximalDistance;
    float height;
};

/***
 * Input variables and uniforms
 ***/

/* Vertex shader output */
in vec2 vertexTextureCoordinates;
in vec4 vertexColor;
in vec3 vertexLight;
in float vertexFog;

/* Textures */
uniform sampler2D textureDiffuse;

/* Fog */
uniform Fog fog;

/* Output color */
out vec4 outputColor;

void main() {
    vec4 color = texture(textureDiffuse, vertexTextureCoordinates) * vertexColor;

    /* The alpha is kept: a particle in the fog keeps its shape and takes the fog color */
    outputColor = vec4(mix(color.rgb * vertexLight, fog.color, vertexFog), color.a);
}
