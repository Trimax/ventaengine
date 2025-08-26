#version 330 core

/* Vertex shader output */
in vec3 vertexPosition;

/* Skybox texture */
uniform samplerCube skybox;

/* Output color */
out vec4 FragColor;

void main() {
    FragColor = texture(skybox, vertexPosition);
}