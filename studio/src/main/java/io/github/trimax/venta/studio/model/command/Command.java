package io.github.trimax.venta.studio.model.command;

/**
 * A change of the scene that can be taken back. A command that is done is kept by the history until it is undone, and the history
 * does it again when it is redone.
 */
public interface Command {
    /**
     * What the command does, for the menu: {@code Move Chest}.
     */
    String label();

    void execute();

    void revert();

    /**
     * What makes two commands one: commands that are done one after another with the same key within a short time (the steps of a
     * drag of a value) are undone at once. Null for a command that is never merged.
     */
    default Object mergeKey() {
        return null;
    }

    /**
     * The command that does what this one and the next one do together, when their keys are the same.
     *
     * @return the merged command, null when they can't be merged
     */
    default Command merge(final Command next) {
        return null;
    }
}
