package io.github.trimax.venta.editor.utils;

import javafx.scene.control.Control;
import lombok.NonNull;
import lombok.experimental.UtilityClass;

@UtilityClass
@SuppressWarnings("unchecked")
public final class ControlUtil {
    public <C extends Control> C find(@NonNull final Control control, @NonNull final String id) {
        return (C) control.getParent().getParent().lookup(id);
    }

    public <C extends Control> C getParent(@NonNull final Control control) {
        return (C) control.getParent();
    }
}
