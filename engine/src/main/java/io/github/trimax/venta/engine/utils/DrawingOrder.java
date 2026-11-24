package io.github.trimax.venta.engine.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;
import java.util.function.ToDoubleFunction;

import lombok.NonNull;

/**
 * The order in which the objects of a scene are drawn. What writes depth and has no partly transparent fragments
 * (opaque and cutout surfaces) goes first in any order, then the surfaces that mix colors go from the farthest to the
 * nearest, so every one of them is mixed with everything behind it.
 *
 * <p>It is made for every frame, so one instance keeps its lists and arrays and reuses them: ordering allocates
 * nothing once they have grown to the size of the scene. The result is valid until the next call.
 */
public final class DrawingOrder<T> {
    private static final int INITIAL_CAPACITY = 64;

    private final List<T> ordered = new ArrayList<>();

    private Object[] items = new Object[INITIAL_CAPACITY];
    private Object[] itemsBuffer = new Object[INITIAL_CAPACITY];
    private double[] distances = new double[INITIAL_CAPACITY];
    private double[] distancesBuffer = new double[INITIAL_CAPACITY];

    /**
     * Orders objects for drawing.
     *
     * @param source   the objects, they are not changed
     * @param solid    whether an object has no partly transparent fragments
     * @param distance how far an object is from the camera: only the order of the values matters, it is asked once per object
     * @return the solid objects in their order, then the others from the farthest to the nearest; the list is reused by
     *     the next call and must not be changed
     */
    @SuppressWarnings("unchecked")
    public List<T> order(@NonNull final Collection<T> source, @NonNull final Predicate<T> solid, @NonNull final ToDoubleFunction<T> distance) {
        ordered.clear();

        var count = 0;
        for (final var item : source)
            if (solid.test(item))
                ordered.add(item);
            else {
                grow(count + 1);
                items[count] = item;
                distances[count] = distance.applyAsDouble(item);
                count++;
            }

        sort(count);

        for (int i = 0; i < count; i++) {
            ordered.add((T) items[i]);

            // The scene may delete an object: it must not be kept alive by the arrays
            items[i] = null;
            itemsBuffer[i] = null;
        }

        return ordered;
    }

    private void grow(final int capacity) {
        if (capacity <= items.length)
            return;

        final var size = Math.max(capacity, items.length * 2);
        items = Arrays.copyOf(items, size);
        itemsBuffer = new Object[size];
        distances = Arrays.copyOf(distances, size);
        distancesBuffer = new double[size];
    }

    /** Merge sort from the farthest to the nearest: it is stable (objects at the same distance keep their order) */
    private void sort(final int count) {
        for (int width = 1; width < count; width *= 2) {
            for (int from = 0; from < count; from += 2 * width)
                merge(from, Math.min(from + width, count), Math.min(from + 2 * width, count));

            final var swappedItems = items;
            items = itemsBuffer;
            itemsBuffer = swappedItems;

            final var swappedDistances = distances;
            distances = distancesBuffer;
            distancesBuffer = swappedDistances;
        }
    }

    /** Merges the sorted runs [from, middle) and [middle, to) of the arrays into the same range of the buffers */
    private void merge(final int from, final int middle, final int to) {
        var left = from;
        var right = middle;
        for (int target = from; target < to; target++) {
            // The right run goes first only when it is strictly farther: ties keep the order
            final var takeRight = right < to && (left >= middle || distances[right] > distances[left]);

            itemsBuffer[target] = takeRight ? items[right] : items[left];
            distancesBuffer[target] = takeRight ? distances[right] : distances[left];

            if (takeRight)
                right++;
            else
                left++;
        }
    }
}
