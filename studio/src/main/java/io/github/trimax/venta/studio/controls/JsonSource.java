package io.github.trimax.venta.studio.controls;

import java.util.List;
import java.util.function.Consumer;

import com.google.gson.JsonObject;

/**
 * The object of a file that a form edits: where it is read from, how a change is made (as a command), and what is known of its fields.
 */
public interface JsonSource {
    /** A copy of the object as the file says now, null when it is gone */
    JsonObject read();

    /**
     * Changes the object as a command.
     *
     * @param field  what is changed: the steps of a drag of the same field are one command
     * @param change what is changed on a copy of the object
     */
    void edit(String field, Consumer<JsonObject> change);

    /** The type that the engine reads a field as, null when it is not known */
    Class<?> typeOf(List<String> path);

    /** Whether the field names a file that has a form of its own (a material), shown under the field when the user asks for it */
    default boolean hasReferenceEditor(final List<String> path) {
        return false;
    }

    /** The form of the file that a field names, null when there is none or the file can't be read */
    default SectionEditor referenceEditor(final List<String> path, final String value) {
        return null;
    }

    /** The values a text field may have, null when it may have any text */
    default List<String> optionsOf(final List<String> path) {
        return null;
    }
}
