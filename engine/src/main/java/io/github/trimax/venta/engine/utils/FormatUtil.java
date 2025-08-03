package io.github.trimax.venta.engine.utils;

import lombok.NonNull;
import lombok.experimental.UtilityClass;

@UtilityClass
public final class FormatUtil {
    public String indent(@NonNull final String s) {
        return "  " + s;
    }
}
