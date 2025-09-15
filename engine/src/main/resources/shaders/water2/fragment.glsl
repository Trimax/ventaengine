#version 330 core

/***
 * Structures
 ***/

struct Surface {
    vec4 color;
    float threshold;
    float transition;
};

struct Fresnel {
    float scale;
    float power;
};

uniform Surface surface;
uniform Surface trough;
uniform Surface peak;

uniform Fresnel fresnel;

in vec3 vNormal;
in vec3 vWorldPosition;

/* Camera */
uniform vec3 cameraPosition;

uniform samplerCube textureSkybox;

/* Output color */
out vec4 outputColor;

void main() {
    // Calculate vector from camera to the vertex
    vec3 viewDirection = normalize(vWorldPosition - cameraPosition);
    vec3 reflectedDirection = reflect(viewDirection, vNormal);
    //reflectedDirection.x = -reflectedDirection.x;

    // Sample environment map to get the reflected color
    vec4 reflectionColor = texture(textureSkybox, reflectedDirection);

    // Calculate fresnel effect
    float fresnel = fresnel.scale * pow(1.0 - clamp(dot(viewDirection, vNormal), 0.0, 1.0), fresnel.power);

    // Calculate elevation-based color
    float elevation = vWorldPosition.y;

    // Calculate transition factors using smoothstep
    float peakFactor = smoothstep(peak.threshold - peak.transition, peak.threshold + peak.transition, elevation);
    float troughFactor = smoothstep(trough.threshold - trough.transition, trough.threshold + trough.transition, elevation);

    // Mix between trough and surface colors based on trough transition
    vec3 mixedColor1 = mix(trough.color.rgb, surface.color.rgb, troughFactor);

    // Mix between surface and peak colors based on peak transition
    vec3 mixedColor2 = mix(mixedColor1, peak.color.rgb, peakFactor);

    // Mix the final color with the reflection color
    vec3 finalColor = mix(mixedColor2, reflectionColor.rgb, fresnel);

    outputColor = vec4(finalColor, surface.color.a);
    outputColor = vec4(mixedColor2, 1.0);
}