package io.github.trimax.venta.engine.enums;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum GroupType {
    AudioSource("Audio sources", "soundsources", "/icons/group/audio.png"),
    Billboard("Billboards", "billboards", "/icons/group/billboard.png"),
    Camera("Cameras", "cameras", "/icons/group/camera.png"),
    CubeMap("Cube maps", "cubemaps", "/icons/group/cube.png"),
    Emitter("Emitters", "emitters", "/icons/group/emitter.png"),
    Font("Fonts", "fonts", "/icons/group/shader.png"),
    GridMesh("Grid meshes", "gridmeshes", "/icons/group/grid.png"),
    Light("Lights", "lights", "/icons/group/light.png"),
    Material("Materials", "materials", "/icons/group/material.png"),
    Mesh("Meshes", "meshes", "/icons/group/mesh.png"),
    Object("Objects", "objects", "/icons/group/object.png"),
    Program("Programs", "programs", "/icons/group/program.png"),
    Scene("Scenes", "scenes", "/icons/group/scene.png"),
    Shader("Shaders", "shaders", "/icons/group/shader.png"),
    Sound("Sounds", "sounds", "/icons/group/audio.png"),
    Sprite("Sprites", "sprites", "/icons/group/sprite.png"),
    TerrainSurface("Terrain surfaces", "surfaces/terrain", "/icons/group/grid.png"),
    Texture("Textures", "textures", "/icons/group/texture.png"),
    TextureArray("Texture arrays", "texturearrays", "/icons/group/texture.png"),
    WaterSurface("Water surfaces", "surfaces/water", "/icons/group/grid.png");

    private final String displayName;

    /** Root folder of the group inside an archive. Must match the paths the engine loads resources from */
    private final String archiveFolder;
    private final String iconPath;

    public static GroupType of(final String value) {
        for (final var currentValue : values())
            if (currentValue.name().equalsIgnoreCase(value))
                return currentValue;

        return null;
    }
}
