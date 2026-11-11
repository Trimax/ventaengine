package io.github.trimax.venta.engine.utils;

import lombok.NonNull;
import lombok.experimental.UtilityClass;

/** Checks the sizes of lists that are uploaded to fixed-size uniform arrays of shaders */
@UtilityClass
public final class LimitUtil {
    /** Shaders index uniform arrays by the count, so a count outside [minimal, maximal] reads out of bounds */
    public void checkCount(@NonNull final String resource, @NonNull final String items, final int count, final int minimal, final int maximal) {
        if (count < minimal || count > maximal)
            throw new IllegalArgumentException(String.format("%s: %d %s, expected from %d to %d", resource, count, items, minimal, maximal));
    }

    /** Count of items that fit into a uniform array of the given size */
    public int fit(final int count, final int maximal) {
        return Math.max(0, Math.min(count, maximal));
    }
}
