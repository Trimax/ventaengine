package io.github.trimax.venta.studio.model.outline;

/**
 * An item of the hierarchy: a thing of a scene or its settings.
 *
 * @param kind   what it is
 * @param name   how the hierarchy calls it
 * @param detail the file it is made from (the object, the light), or an empty text
 * @param source what the inspector shows: the part of the scene file that describes it, null for a category
 * @param index  the place of the thing among the things of its kind in the file (from 0), -1 for what is not a thing: the scene, a
 *               category, a setting
 */
public record OutlineItem(OutlineKind kind, String name, String detail, Object source, int index) {
    /** What is not a thing of the scene has no place among them */
    public static final int NOT_A_THING = -1;

    public OutlineItem(final OutlineKind kind, final String name, final String detail, final Object source) {
        this(kind, name, detail, source, NOT_A_THING);
    }

    /** Whether the item is a thing of the scene: it has a place in the file and can be edited */
    public boolean isThing() {
        return index != NOT_A_THING;
    }

    @Override
    public String toString() {
        return name;
    }
}
