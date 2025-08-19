package io.github.trimax.venta.editor.definitions;

import javafx.scene.image.Image;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Objects;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Icons {
    public static final Image UNKNOWN = new Image(Objects.requireNonNull(Icons.class.getResourceAsStream("/icons/unknown.png")));
    public static final Image MATERIAL = new Image(Objects.requireNonNull(Icons.class.getResourceAsStream("/icons/material.png")));
    public static final Image TEXTURE = new Image(Objects.requireNonNull(Icons.class.getResourceAsStream("/icons/texture.png")));
    public static final Image OBJECT = new Image(Objects.requireNonNull(Icons.class.getResourceAsStream("/icons/object.png")));
    public static final Image SHADER = new Image(Objects.requireNonNull(Icons.class.getResourceAsStream("/icons/shader.png")));
    public static final Image FOLDER = new Image(Objects.requireNonNull(Icons.class.getResourceAsStream("/icons/folder.png")));
    public static final Image GROUP = new Image(Objects.requireNonNull(Icons.class.getResourceAsStream("/icons/group.png")));
    public static final Image SCENE = new Image(Objects.requireNonNull(Icons.class.getResourceAsStream("/icons/scene.png")));
    public static final Image LIGHT = new Image(Objects.requireNonNull(Icons.class.getResourceAsStream("/icons/light.png")));
    public static final Image MESH = new Image(Objects.requireNonNull(Icons.class.getResourceAsStream("/icons/mesh.png")));
}
