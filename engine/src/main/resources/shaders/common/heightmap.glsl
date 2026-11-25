/* Heightmap */
uniform sampler2D textureHeight;
uniform float factor;

/* How deep the skirts of the chunks hang: they close the cracks between the chunks of different levels of detail */
uniform float skirtDepth;

/* The vertices of the skirts have the height -1 where the ones of the grid have 0 (see TerrainChunkLayout) */
float getSkirt(float flag) {
    return flag < -0.5 ? skirtDepth : 0.0;
}

/* The height of the surface in its local space: the middle of the range of the heightmap is the height 0 */
float getHeight(vec2 coordinates) {
    return (texture(textureHeight, coordinates).r - 0.5) * factor;
}
