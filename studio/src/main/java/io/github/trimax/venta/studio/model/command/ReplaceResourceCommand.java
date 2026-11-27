package io.github.trimax.venta.studio.model.command;

import java.util.List;
import java.util.Objects;

import com.google.gson.JsonObject;

import lombok.NonNull;

/**
 * A file of a resource of the project (a light, ...) is changed. What it writes with is given by the editor of the scene, which also shows
 * the change.
 */
public final class ReplaceResourceCommand implements Command {
    /**
     * Where a resource is written: the editor of the scene.
     */
    public interface Writer {
        void writeResource(String path, JsonObject value);
    }

    private final Writer writer;
    private final String path;
    private final JsonObject before;
    private final JsonObject after;
    private final String label;
    private final String field;

    /**
     * A change of a resource.
     *
     * @param path  the place of the file in the project
     * @param field what is changed (a name for the merge): the steps of a drag of the same field are one command
     */
    public ReplaceResourceCommand(@NonNull final Writer writer, @NonNull final String path, @NonNull final JsonObject before,
                                  @NonNull final JsonObject after, @NonNull final String label, @NonNull final String field) {
        this.writer = writer;
        this.path = path;
        this.before = before.deepCopy();
        this.after = after.deepCopy();
        this.label = label;
        this.field = field;
    }

    @Override
    public String label() {
        return label;
    }

    @Override
    public void execute() {
        writer.writeResource(path, after);
    }

    @Override
    public void revert() {
        writer.writeResource(path, before);
    }

    @Override
    public Object mergeKey() {
        return List.of(path, field);
    }

    @Override
    public Command merge(final Command next) {
        if (!(next instanceof ReplaceResourceCommand other) || !Objects.equals(mergeKey(), other.mergeKey()) || writer != other.writer)
            return null;

        return new ReplaceResourceCommand(writer, path, before, other.after, label, field);
    }
}
