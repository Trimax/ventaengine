package io.github.trimax.venta.engine.utils;

import lombok.NonNull;
import lombok.experimental.UtilityClass;
import one.util.streamex.StreamEx;

import java.util.function.ToIntFunction;

@UtilityClass
public final class EnumUtil {
    public static <E extends Enum<E>> int sum(@NonNull final Class<E> enumClass,
                                              @NonNull final ToIntFunction<E> mapper) {
        return StreamEx.of(enumClass.getEnumConstants())
                .mapToInt(mapper)
                .sum();
    }
}
