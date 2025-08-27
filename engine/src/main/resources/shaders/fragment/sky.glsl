#version 330 core

struct Fog {
    vec3 color;
    float minimalDistance;
    float maximalDistance;
};

/* Vertex shader output */
in vec3 vertexPosition;

/* Skybox texture */
uniform samplerCube skybox;

/* Fog */
uniform Fog fog;

/* Feature flags */
uniform int useFog;

/* Output color */
out vec4 FragColor;

/* Checks if flag is set (bools are not supported by macOS Radeon cards */
bool isSet(int value) {
    return value != 0;
}

void main() {
    FragColor = isSet(useFog) ? vec4(fog.color, 1.0) : texture(skybox, vertexPosition);
}