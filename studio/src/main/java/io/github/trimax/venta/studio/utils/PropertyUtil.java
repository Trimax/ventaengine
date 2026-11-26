package io.github.trimax.venta.studio.utils;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

import org.joml.Vector2fc;
import org.joml.Vector3fc;
import org.joml.Vector4fc;

import io.github.trimax.venta.studio.model.property.Property;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;

/**
 * Makes the lines of the inspector of any record of the scene file by what the record is made of, so every new field of the
 * scene file is shown without a line of code. What has no value is not shown.
 */
@UtilityClass
public final class PropertyUtil {
    private static final String VECTOR_PACKAGE = "org.joml.";

    /** The lines of a thing: its fields, a value that is not a record is one line */
    public List<Property> read(final Object thing) {
        if (thing == null)
            return List.of();

        if (!thing.getClass().isRecord())
            return List.of(new Property("Value", format(thing)));

        return fields(thing);
    }

    /** "castShadows" is "Cast shadows" */
    public String humanize(@NonNull final String name) {
        if (name.isEmpty())
            return name;

        final var text = new StringBuilder();
        for (int index = 0; index < name.length(); index++) {
            final var symbol = name.charAt(index);
            if (Character.isUpperCase(symbol) && index > 0)
                text.append(' ').append(Character.toLowerCase(symbol));
            else
                text.append(index == 0 ? Character.toUpperCase(symbol) : symbol);
        }

        return text.toString();
    }

    private List<Property> fields(final Object record) {
        final var properties = new ArrayList<Property>();

        for (final var component : record.getClass().getRecordComponents()) {
            final var value = valueOf(record, component);
            if (value != null)
                properties.add(property(humanize(component.getName()), value));
        }

        return properties;
    }

    private Property property(final String name, final Object value) {
        if (isLeaf(value))
            return new Property(name, format(value));

        if (value instanceof Collection<?> collection)
            return new Property(name, collection.size() + (collection.size() == 1 ? " item" : " items"), elements(collection));

        return new Property(name, "", fields(value));
    }

    private List<Property> elements(final Collection<?> collection) {
        final var elements = new ArrayList<Property>();

        int index = 0;
        for (final var element : collection) {
            final var name = nameOf(element, index++);
            elements.add(isLeaf(element) ? new Property(name, format(element)) : new Property(name, "", element.getClass().isRecord() ? fields(element) : List.of()));
        }

        return elements;
    }

    /** Things of a scene have names: the element is shown by it */
    private String nameOf(final Object element, final int index) {
        if (element.getClass().isRecord())
            for (final var component : element.getClass().getRecordComponents())
                if (component.getName().equals("name") && valueOf(element, component) instanceof String text)
                    return text;

        return "#" + index;
    }

    @SneakyThrows(IllegalAccessException.class)
    private Object valueOf(final Object record, final java.lang.reflect.RecordComponent component) {
        try {
            final var accessor = component.getAccessor();
            accessor.setAccessible(true);
            return accessor.invoke(record);
        } catch (final InvocationTargetException exception) {
            return null;
        }
    }

    private boolean isLeaf(final Object value) {
        return value instanceof Number || value instanceof CharSequence || value instanceof Boolean || value instanceof Enum<?>
                || value instanceof Character || value.getClass().getName().startsWith(VECTOR_PACKAGE);
    }

    private String format(final Object value) {
        if (value instanceof Float number)
            return trim(String.format(Locale.ROOT, "%.4f", number));

        if (value instanceof Double number)
            return trim(String.format(Locale.ROOT, "%.4f", number));

        if (value instanceof Vector2fc vector)
            return join(vector.x(), vector.y());

        if (value instanceof Vector3fc vector)
            return join(vector.x(), vector.y(), vector.z());

        if (value instanceof Vector4fc vector)
            return join(vector.x(), vector.y(), vector.z(), vector.w());

        return String.valueOf(value);
    }

    private String join(final float... numbers) {
        final var text = new StringBuilder();
        for (final var number : numbers)
            text.append(text.isEmpty() ? "" : ", ").append(format(number));

        return text.toString();
    }

    private String trim(final String number) {
        if (!number.contains("."))
            return number;

        final var trimmed = number.replaceAll("0+$", "");
        return trimmed.endsWith(".") ? trimmed + "0" : trimmed;
    }
}
