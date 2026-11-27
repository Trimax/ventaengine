package io.github.trimax.venta.studio.model.command;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Objects;
import java.util.function.LongSupplier;

import lombok.NonNull;

/**
 * The commands that were done and the ones that were undone. A new command empties what was undone, the oldest commands are forgotten
 * when there are too many. It knows whether the scene is as it was saved: the saved state is a place in the list of the commands.
 */
public final class History {
    public static final int MAXIMAL_COMMANDS = 200;

    /** Two commands of a drag are one when they are done within this time (milliseconds) */
    public static final long MERGE_WINDOW = 700;

    private final List<Command> done = new ArrayList<>();
    private final Deque<Command> undone = new ArrayDeque<>();
    private final LongSupplier clock;

    /** How many commands were done when the scene was saved, -1 when that state is lost (forgotten, or the commands were undone and others done) */
    private int saved;
    private long lastTime;

    /** Whether a drag is on: its steps are one command however long it takes */
    private boolean holding;

    public History() {
        this(System::currentTimeMillis);
    }

    public History(@NonNull final LongSupplier clock) {
        this.clock = clock;
    }

    /** Does the command and keeps it; it is merged with the one before it when they are of the same drag */
    public void perform(@NonNull final Command command) {
        final var now = clock.getAsLong();

        command.execute();
        undone.clear();

        // What is merged must not be what was saved: the saved state would be changed by a command that is not undone with the rest
        if (saved > done.size())
            saved = -1;

        final var previous = done.isEmpty() ? null : done.getLast();
        if (previous != null && saved != done.size() && command.mergeKey() != null && Objects.equals(previous.mergeKey(), command.mergeKey())
                && (holding || now - lastTime <= MERGE_WINDOW)) {
            final var merged = previous.merge(command);
            if (merged != null) {
                done.set(done.size() - 1, merged);
                lastTime = now;
                return;
            }
        }

        done.add(command);
        lastTime = now;

        if (done.size() > MAXIMAL_COMMANDS) {
            done.removeFirst();
            saved = saved > 0 ? saved - 1 : -1;
        }
    }

    /**
     * A drag begins: the commands of the same key that are done until {@link #release()} are one, even when the mouse rests.
     */
    public void hold() {
        holding = true;
        lastTime = 0;
    }

    /** The drag is over: the next command is a new one */
    public void release() {
        holding = false;
        lastTime = 0;
    }

    public boolean canUndo() {
        return !done.isEmpty();
    }

    public boolean canRedo() {
        return !undone.isEmpty();
    }

    /** What undo does, an empty text when there is nothing to undo */
    public String undoLabel() {
        return done.isEmpty() ? "" : done.getLast().label();
    }

    public String redoLabel() {
        return undone.isEmpty() ? "" : undone.peekFirst().label();
    }

    public void undo() {
        if (done.isEmpty())
            return;

        final var command = done.removeLast();
        command.revert();
        undone.push(command);
        lastTime = 0;
    }

    public void redo() {
        if (undone.isEmpty())
            return;

        final var command = undone.pop();
        command.execute();
        done.add(command);
        lastTime = 0;
    }

    /** The scene is saved as it is now */
    public void markSaved() {
        saved = done.size();
        lastTime = 0;
    }

    /** Whether the scene is not what was saved (what was never saved is changed when there is any command) */
    public boolean isDirty() {
        return saved != done.size();
    }

    /** Forgets everything: a scene is open, there is nothing to undo, and the scene is as it was read */
    public void clear() {
        done.clear();
        undone.clear();
        saved = 0;
        lastTime = 0;
    }
}
