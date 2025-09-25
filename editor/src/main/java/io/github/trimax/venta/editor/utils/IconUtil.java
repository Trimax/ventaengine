package io.github.trimax.venta.editor.utils;

import io.github.trimax.venta.engine.enums.GroupType;
import io.github.trimax.venta.engine.enums.ResourceType;
import javafx.scene.image.Image;
import lombok.NonNull;
import lombok.experimental.UtilityClass;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@UtilityClass
public final class IconUtil {
    private final Map<String, Image> cache = new HashMap<>();

    public Image getResourceImage(@NonNull final GroupType type) {
        if (!cache.containsKey(type.getIconPath()))
            cache.put(type.getIconPath(), getImage(type.getIconPath()));

        return cache.get(type.getIconPath());
    }

    public Image getTypeImage(@NonNull final ResourceType type) {
        if (!cache.containsKey(type.getIconPath()))
            cache.put(type.getIconPath(), getImage(type.getIconPath()));

        return cache.get(type.getIconPath());
    }

    private Image getImage(@NonNull final String iconPath) {
        return new Image(Objects.requireNonNull(IconUtil.class.getResourceAsStream(iconPath)));
    }
}
