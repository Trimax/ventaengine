package io.github.trimax.venta.engine.model.common.reflection;

import java.util.function.Predicate;

/**
 * The few nearest of many things, kept in a frame without allocating: the candidates are offered one by one with their
 * distances, and the selection keeps the nearest ones in the order of the distance.
 *
 * @param <T> what is selected
 */
public final class NearestSelection<T> {
    private final Object[] items;
    private final float[] distances;
    private int count;

    public NearestSelection(final int capacity) {
        if (capacity < 1)
            throw new IllegalArgumentException("The selection must keep at least one thing, got " + capacity);

        items = new Object[capacity];
        distances = new float[capacity];
    }

    /** Forgets what was offered */
    public void clear() {
        java.util.Arrays.fill(items, 0, count, null);
        count = 0;
    }

    /** Offers a candidate: it is kept when it is nearer than the farthest of the kept ones, or there is room */
    public void offer(final T item, final float distance) {
        var index = count;
        if (count == items.length) {
            if (distance >= distances[count - 1])
                return;

            index = count - 1;
        } else {
            count++;
        }

        // The kept ones are sorted: the candidate goes to its place and the farther ones move
        while (index > 0 && distances[index - 1] > distance) {
            items[index] = items[index - 1];
            distances[index] = distances[index - 1];
            index--;
        }

        items[index] = item;
        distances[index] = distance;
    }

    public int size() {
        return count;
    }

    @SuppressWarnings("unchecked")
    public T get(final int index) {
        if (index < 0 || index >= count)
            throw new IndexOutOfBoundsException(index);

        return (T) items[index];
    }

    /** Whether the thing is selected: by the identity */
    public boolean contains(final Object item) {
        for (int index = 0; index < count; index++)
            if (items[index] == item)
                return true;

        return false;
    }

    /** The test of the identity as a predicate */
    public Predicate<T> asPredicate() {
        return this::contains;
    }
}
