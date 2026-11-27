package io.github.trimax.venta.studio.utils;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.RecordComponent;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;

import io.github.trimax.venta.engine.model.dto.SceneDTO;
import io.github.trimax.venta.studio.model.document.SceneDocument;
import lombok.NonNull;
import lombok.experimental.UtilityClass;

/**
 * What the file of a scene may hold, by the records that the engine reads it into: the type of a field of a section of the environment,
 * so the editor knows the values of an enum and a whole number from a fraction.
 */
@UtilityClass
public final class SchemaUtil {
    /**
     * The type of a field of a section.
     *
     * @param section the name of the section in the hierarchy ({@code Fog})
     * @param path    the names of the fields from the section to the field; empty for the section itself
     * @return the type, null when the engine doesn't read such a field
     */
    public Class<?> typeOf(@NonNull final String section, @NonNull final List<String> path) {
        final var fields = SceneDocument.fieldsOf(section);
        if (fields.isEmpty())
            return null;

        // The output is not an object of the file: its fields are the fields of the scene, so the first name of the path is one of them
        final var root = fields.size() == 1 ? component(SceneDTO.class, fields.getFirst()) : SceneDTO.class;
        return typeOf(root, path);
    }

    /**
     * The type of a field of a record that the engine reads a file into.
     *
     * @param root the record ({@code LightDTO})
     * @param path the names of the fields from the record to the field; empty for the record itself
     * @return the type, null when the record has no such field
     */
    public Class<?> typeOf(final Class<?> root, @NonNull final List<String> path) {
        Type type = root;
        for (final var name : path) {
            if (type == null)
                return null;

            type = step(type, name);
        }

        return rawOf(type);
    }

    /** The names of the values of an enum, null for a type that is not one */
    public List<String> constantsOf(final Class<?> type) {
        return type == null || !type.isEnum() ? null : Arrays.stream(type.getEnumConstants()).map(constant -> ((Enum<?>) constant).name()).toList();
    }

    /** Whether the type holds only whole numbers */
    public boolean isWhole(final Class<?> type) {
        return type == int.class || type == Integer.class || type == long.class || type == Long.class || type == short.class || type == Short.class;
    }

    /** The type of a field of a record, or of an element of a list when the name is its number */
    private Type step(final Type type, final String name) {
        if (type instanceof ParameterizedType generic && List.class.isAssignableFrom(rawOf(generic)) && name.chars().allMatch(Character::isDigit))
            return generic.getActualTypeArguments()[0];

        final var raw = rawOf(type);
        if (raw == null || !raw.isRecord())
            return null;

        return Arrays.stream(raw.getRecordComponents()).filter(component -> component.getName().equals(name)).map(RecordComponent::getGenericType).findFirst().orElse(null);
    }

    /** The class of a type, null for a type that isn't known until it is used (the parameter of a generic record) */
    private Class<?> rawOf(final Type type) {
        if (type instanceof Class<?> found)
            return found;

        return type instanceof ParameterizedType generic && generic.getRawType() instanceof Class<?> found ? found : null;
    }

    private Class<?> component(final Class<?> type, final String name) {
        return rawOf(step(type, name));
    }
}
