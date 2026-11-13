#version 330 core

/* Parameters going to fragment shader */
out vec2 vertexTextureCoordinates;

/* One triangle covers the screen: the vertices (-1, -1), (3, -1) and (-1, 3) need no vertex buffer */
void main() {
    vec2 position = vec2((gl_VertexID << 1) & 2, gl_VertexID & 2);

    vertexTextureCoordinates = position;
    gl_Position = vec4(position * 2.0 - 1.0, 0.0, 1.0);
}
