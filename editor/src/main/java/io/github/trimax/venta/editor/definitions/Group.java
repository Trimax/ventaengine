package io.github.trimax.venta.editor.definitions;

import javafx.scene.image.Image;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum Group {
    Materials("/icons/group/material.png"),
    Textures("/icons/group/texture.png"),
    Programs("/icons/group/program.png"),
    Shaders("/icons/group/shader.png"),
    Objects("/icons/group/object.png"),
    Scenes("/icons/group/scene.png"),
    Lights("/icons/group/light.png"),
    Meshes("/icons/group/mesh.png"),
    Cameras("/icons/group/camera.png"),
    Terrains("/icons/group/terrain.png"),
    Audios("/icons/group/audio.png"),
    Emitters("/icons/group/emitter.png"),
    Cubemaps("/icons/group/cube.png");

    private final Image icon;

    Group(final String iconPath) {
        this(new Image(java.util.Objects.requireNonNull(Group.class.getResourceAsStream(iconPath))));
    }
}
