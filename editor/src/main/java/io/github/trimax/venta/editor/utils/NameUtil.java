package io.github.trimax.venta.editor.utils;

import lombok.NonNull;
import lombok.experimental.UtilityClass;

@UtilityClass
public final class NameUtil {
    public boolean isValidName(@NonNull final String name) {
        return name.matches("^[\\w-]+$");
    }
}
