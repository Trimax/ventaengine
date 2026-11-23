package io.github.trimax.venta.engine.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;
import java.util.function.ToDoubleFunction;

import lombok.NonNull;
import lombok.experimental.UtilityClass;

/**
 * The order in which the objects of a scene are drawn. What writes depth and has no partly transparent fragments
 * (opaque and cutout surfaces) goes first in any order, then the surfaces that mix colors go from the farthest to the
 * nearest, so every one of them is mixed with everything behind it.
 */
@UtilityClass
public final class RenderOrderUtil {
    /**
     * Orders objects for drawing.
     *
     * @param items    the objects, they are not changed
     * @param solid    whether an object has no partly transparent fragments
     * @param distance how far an object is from the camera: only the order of the values matters
     * @return a new list: the solid objects in their order, then the others from the farthest to the nearest
     */
    public <T> List<T> order(@NonNull final Collection<T> items, @NonNull final Predicate<T> solid, @NonNull final ToDoubleFunction<T> distance) {
        final var ordered = new ArrayList<T>(items.size());
        final var blended = new ArrayList<Measured<T>>();
        for (final var item : items)
            if (solid.test(item))
                ordered.add(item);
            else
                // The distance is measured once: it can be costly and the sort asks for it many times
                blended.add(new Measured<>(item, distance.applyAsDouble(item)));

        // The sort is stable: objects at the same distance keep their order
        blended.sort(Comparator.comparingDouble((Measured<T> measured) -> measured.distance).reversed());
        for (final var measured : blended)
            ordered.add(measured.item);

        return ordered;
    }

    private record Measured<T>(T item, double distance) {
    }
}
