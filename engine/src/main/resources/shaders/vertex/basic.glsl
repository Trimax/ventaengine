#version 330 core

/* Vertex attributes */
layout(location = 0) in vec3 position;
layout(location = 1) in vec3 normal;
layout(location = 2) in vec2 textureCoordinates;
layout(location = 3) in vec4 color;

/* Object attributes */
uniform vec3 translation; // Object's position in world coordinates
uniform vec3 rotation;    // Object's orientation, Euler angles (radians): pitch (x), yaw (y), roll (z)
uniform vec3 scale;       // Object's scale

out vec4 vertexColor;

mat4 createRotationMatrix(vec3 angles) {
    float cx = cos(angles.x);
    float sx = sin(angles.x);
    float cy = cos(angles.y);
    float sy = sin(angles.y);
    float cz = cos(angles.z);
    float sz = sin(angles.z);

    mat4 Rx = mat4(
    1, 0, 0, 0,
    0, cx, -sx, 0,
    0, sx, cx, 0,
    0, 0, 0, 1
    );

    mat4 Ry = mat4(
    cy, 0, sy, 0,
    0, 1, 0, 0,
    -sy, 0, cy, 0,
    0, 0, 0, 1
    );

    mat4 Rz = mat4(
    cz, -sz, 0, 0,
    sz, cz, 0, 0,
    0, 0, 1, 0,
    0, 0, 0, 1
    );

    return Rz * Ry * Rx;
}

mat4 createTranslationMatrix(vec3 t) {
    return mat4(
    1.0, 0.0, 0.0, 0.0,
    0.0, 1.0, 0.0, 0.0,
    0.0, 0.0, 1.0, 0.0,
    t.x, t.y, t.z, 1.0
    );
}

mat4 createScaleMatrix(vec3 s) {
    return mat4(
    s.x, 0.0, 0.0, 0.0,
    0.0, s.y, 0.0, 0.0,
    0.0, 0.0, s.z, 0.0,
    0.0, 0.0, 0.0, 1.0
    );
}

void main() {
    mat4 S = createScaleMatrix(scale);
    mat4 R = createRotationMatrix(rotation);
    mat4 T = createTranslationMatrix(translation);

    mat4 model = T * R * S;

    gl_Position = model * vec4(position, 1.0);
    vertexColor = color;
}
