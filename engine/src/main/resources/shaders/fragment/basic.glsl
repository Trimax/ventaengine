#version 330 core

in vec4 vertexColor;
in vec2 vertexTextureCoordinates;

out vec4 FragColor;

uniform sampler2D textureDiffuse;
uniform sampler2D heightTexture;

void main() {
    vec4 diffuseColor = texture(textureDiffuse, vertexTextureCoordinates);
    float heightValue = texture(heightTexture, vertexTextureCoordinates).r;

    vec3 colorMod = diffuseColor.rgb * (0.8 + 0.2 * heightValue);

    FragColor = vertexColor * vec4(colorMod, diffuseColor.a);
}