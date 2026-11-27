package io.github.trimax.venta.studio.model.command;

import java.util.List;
import java.util.Objects;

import com.google.gson.JsonObject;

import lombok.NonNull;

/**
 * A section of the environment of the scene (the sun, the fog, the bloom, ...) is changed. What it writes with is given by the editor of the
 * scene, which also shows the change.
 */
public final class ReplaceSectionCommand implements Command {
    /**
     * Where a section is written: the editor of the scene.
     */
    public interface Writer {
        void write(String section, JsonObject value);
    }

    private final Writer writer;
    private final String section;
    private final JsonObject before;
    private final JsonObject after;
    private final String label;
    private final String field;

    /**
     * A change of a section.
     *
     * @param field what is changed (a name for the merge): the steps of a drag of the same field are one command
     */
    public ReplaceSectionCommand(@NonNull final Writer writer, @NonNull final String section, @NonNull final JsonObject before,
                                 @NonNull final JsonObject after, @NonNull final String label, @NonNull final String field) {
        this.writer = writer;
        this.section = section;
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
        writer.write(section, after);
    }

    @Override
    public void revert() {
        writer.write(section, before);
    }

    @Override
    public Object mergeKey() {
        return List.of(section, field);
    }

    @Override
    public Command merge(final Command next) {
        if (!(next instanceof ReplaceSectionCommand other) || !Objects.equals(mergeKey(), other.mergeKey()) || writer != other.writer)
            return null;

        return new ReplaceSectionCommand(writer, section, before, other.after, label, field);
    }
}
