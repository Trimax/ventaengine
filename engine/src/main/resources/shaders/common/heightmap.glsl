/* Heightmap */
uniform sampler2D textureHeight;
uniform float factor;

/* The height of the surface in its local space: the middle of the range of the heightmap is the height 0 */
float getHeight(vec2 coordinates) {
    return (texture(textureHeight, coordinates).r - 0.5) * factor;
}
