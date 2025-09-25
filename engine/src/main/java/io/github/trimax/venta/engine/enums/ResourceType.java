package io.github.trimax.venta.engine.enums;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum ResourceType {
    AudioSource("Audio sources", "/icons/group/audio.png"),
    Billboard("Billboards", "/icons/group/billboard.png"),
    Camera("Cameras", "/icons/group/camera.png"),
    CubeMap("Cube maps", "/icons/group/cubemap.png"),
    Emitter("Emitters", "/icons/group/emitter.png"),
    GridMesh("Grid meshes", "/icons/group/grid.png"),
    Light("Lights", "/icons/group/light.png"),
    Material("Materials", "/icons/group/material.png"),
    Mesh("Meshes", "/icons/group/mesh.png"),
    Object("Objects", "/icons/group/object.png"),
    Program("Programs", "/icons/group/program.png"),
    Scene("Scenes", "/icons/group/scene.png"),
    Shader("Shaders", "/icons/group/shader.png"),
    Sprite("Sprites", "/icons/group/sprite.png"),
    Texture("Textures", "/icons/group/texture.png");

    private final String displayName;
    private final String icon;

    public static ResourceType of(final String value) {
        for (final var currentValue : values())
            if (currentValue.name().equalsIgnoreCase(value))
                return currentValue;

        return null;
    }
}
