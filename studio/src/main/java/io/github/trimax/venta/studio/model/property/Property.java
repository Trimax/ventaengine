package io.github.trimax.venta.studio.model.property;

import java.util.List;

/**
 * A line of the inspector: a name and a value, or a name and what is inside it.
 *
 * @param name     how the line is called
 * @param value    the value as a text, empty for a line that only has parts
 * @param children the parts of the value (the fields of a record, the elements of a list)
 */
public record Property(String name, String value, List<Property> children) {
    public Property {
        children = List.copyOf(children);
    }

    public Property(final String name, final String value) {
        this(name, value, List.of());
    }
}
