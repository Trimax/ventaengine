package io.github.trimax.venta.studio.model.command;

import java.util.Objects;

import com.google.gson.JsonObject;

import io.github.trimax.venta.studio.model.outline.OutlineKind;
import lombok.NonNull;

/**
 * A thing of the scene is changed: its place in the file is the same, it has other fields. What it writes with is given by the editor of
 * the scene, which also shows the change.
 */
public final class ReplaceEntryCommand implements Command {
    /**
     * Where a thing is written: the editor of the scene.
     */
    public interface Writer {
        void write(OutlineKind kind, int index, JsonObject entry);
    }

    private final Writer writer;
    private final OutlineKind kind;
    private final int index;
    private final JsonObject before;
    private final JsonObject after;
    private final String label;
    private final String field;

    /**
     * A change of a thing.
     *
     * @param field what is changed (a name for the merge): {@code position}, so the steps of a drag of the position are one command
     */
    public ReplaceEntryCommand(@NonNull final Writer writer, @NonNull final OutlineKind kind, final int index, @NonNull final JsonObject before,
                               @NonNull final JsonObject after, @NonNull final String label, @NonNull final String field) {
        this.writer = writer;
        this.kind = kind;
        this.index = index;
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
        writer.write(kind, index, after);
    }

    @Override
    public void revert() {
        writer.write(kind, index, before);
    }

    @Override
    public Object mergeKey() {
        return java.util.List.of(kind, index, field);
    }

    @Override
    public Command merge(final Command next) {
        if (!(next instanceof ReplaceEntryCommand other) || !Objects.equals(mergeKey(), other.mergeKey()) || writer != other.writer)
            return null;

        return new ReplaceEntryCommand(writer, kind, index, before, other.after, label, field);
    }
}
