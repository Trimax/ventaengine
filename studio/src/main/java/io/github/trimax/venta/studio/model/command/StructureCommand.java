package io.github.trimax.venta.studio.model.command;

import com.google.gson.JsonObject;

import io.github.trimax.venta.studio.model.outline.OutlineKind;
import lombok.NonNull;

/**
 * A thing is put in the scene or taken out of it. What undoes an insertion is the removal of the thing from the same place and the
 * other way round, so one command does both.
 */
public final class StructureCommand implements Command {
    /**
     * Where the things are put and taken from: the editor of the scene.
     */
    public interface Store {
        void insert(OutlineKind kind, int index, JsonObject entry);

        void remove(OutlineKind kind, int index);
    }

    private final Store store;
    private final OutlineKind kind;
    private final int index;
    private final JsonObject entry;
    private final boolean inserting;
    private final String label;

    private StructureCommand(final Store store, final OutlineKind kind, final int index, final JsonObject entry, final boolean inserting, final String label) {
        this.store = store;
        this.kind = kind;
        this.index = index;
        this.entry = entry.deepCopy();
        this.inserting = inserting;
        this.label = label;
    }

    /** The thing is put in the place: the things after it move one place on */
    public static StructureCommand insertion(@NonNull final Store store, @NonNull final OutlineKind kind, final int index, @NonNull final JsonObject entry, @NonNull final String label) {
        return new StructureCommand(store, kind, index, entry, true, label);
    }

    /** The thing that is in the place is taken out; the thing is kept for the undo */
    public static StructureCommand removal(@NonNull final Store store, @NonNull final OutlineKind kind, final int index, @NonNull final JsonObject entry, @NonNull final String label) {
        return new StructureCommand(store, kind, index, entry, false, label);
    }

    @Override
    public String label() {
        return label;
    }

    @Override
    public void execute() {
        apply(inserting);
    }

    @Override
    public void revert() {
        apply(!inserting);
    }

    private void apply(final boolean insert) {
        if (insert)
            store.insert(kind, index, entry);
        else
            store.remove(kind, index);
    }
}
