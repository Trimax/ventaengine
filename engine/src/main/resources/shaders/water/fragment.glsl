#version 330 core

in vec3 vertexNormal;
in vec3 vertexPosition;
in vec2 vertexTextureCoordinates;

uniform vec3 cameraPosition;
uniform vec3 lightDirection;

out vec4 outputColor;

void main() {
    vec3 N = normalize(vertexNormal);
    vec3 L = normalize(lightDirection);
    vec3 V = normalize(cameraPosition - vertexPosition);

    float diff = max(dot(N, L), 0.0);
    vec3 R = reflect(-L, N);
    float spec = pow(max(dot(R, V), 0.0), 32.0);

    vec3 baseColor = vec3(0.0, 0.3, 0.6); // blueish water color
    vec3 color = baseColor * diff + vec3(1.0) * spec * 0.3;

    outputColor = vec4(color, 1.0);
}