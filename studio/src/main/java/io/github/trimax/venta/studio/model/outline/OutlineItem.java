package io.github.trimax.venta.studio.model.outline;

/**
 * An item of the hierarchy: a thing of a scene or its settings.
 *
 * @param kind   what it is
 * @param name   how the hierarchy calls it
 * @param detail the file it is made from (the object, the light), or an empty text
 * @param source what the inspector shows: the part of the scene file that describes it, null for a category
 */
public record OutlineItem(OutlineKind kind, String name, String detail, Object source) {
    @Override
    public String toString() {
        return name;
    }
}
