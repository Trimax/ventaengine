package io.github.trimax.venta.editor.definitions;

import javafx.scene.image.Image;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum Element {
    Unknown("/icons/unknown.png"),
    Folder("/icons/folder.png"),
    File("/icons/file.png");

    private final Image icon;

    Element(final String iconPath) {
        this(new Image(java.util.Objects.requireNonNull(Element.class.getResourceAsStream(iconPath))));
    }
}
