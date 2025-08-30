package io.github.trimax.venta.editor.definitions;

import javafx.scene.image.Image;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum Group {
    Materials("/icons/material.png"),
    Textures("/icons/texture.png"),
    Programs("/icons/program.png"),
    Shaders("/icons/shader.png"),
    Objects("/icons/object.png"),
    Scenes("/icons/scene.png"),
    Lights("/icons/light.png"),
    Meshes("/icons/mesh.png"),
    Cubemaps("/icons/cube.png");

    private final Image icon;

    Group(final String iconPath) {
        this(new Image(java.util.Objects.requireNonNull(Group.class.getResourceAsStream(iconPath))));
    }
}
