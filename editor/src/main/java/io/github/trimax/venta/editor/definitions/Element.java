package io.github.trimax.venta.editor.definitions;

import javafx.scene.image.Image;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum Element {
    Unknown("/icons/common/unknown.png"),
    Group("/icons/common/group.png"),
    Resource("/icons/common/resource.png");

    private final Image icon;

    Element(final String iconPath) {
        this(new Image(java.util.Objects.requireNonNull(Element.class.getResourceAsStream(iconPath))));
    }
}
