package io.github.trimax.venta.studio.model.command;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

final class HistoryTest {
    private final AtomicLong clock = new AtomicLong(1000);
    private final List<String> log = new ArrayList<>();

    private History history;

    @BeforeEach
    void create() {
        history = new History(clock::get);
    }

    /** A command that writes what it does into the log */
    private Command command(final String name) {
        return new Command() {
            @Override
            public String label() {
                return name;
            }

            @Override
            public void execute() {
                log.add("+" + name);
            }

            @Override
            public void revert() {
                log.add("-" + name);
            }
        };
    }

    @Test
    void testCommandIsDoneWhenItIsPerformed() {
        history.perform(command("a"));

        assertEquals(List.of("+a"), log);
        assertTrue(history.canUndo());
        assertFalse(history.canRedo());
        assertEquals("a", history.undoLabel());
    }

    @Test
    void testUndoAndRedoTakeBackAndDoAgainInOrder() {
        history.perform(command("a"));
        history.perform(command("b"));

        history.undo();
        history.undo();
        history.redo();

        assertEquals(List.of("+a", "+b", "-b", "-a", "+a"), log);
        assertEquals("b", history.redoLabel());
        assertEquals("a", history.undoLabel());
    }

    @Test
    void testNothingToUndoOrRedoChangesNothing() {
        history.undo();
        history.redo();

        assertTrue(log.isEmpty());
        assertEquals("", history.undoLabel());
        assertEquals("", history.redoLabel());
    }

    @Test
    void testNewCommandForgetsWhatWasUndone() {
        history.perform(command("a"));
        history.undo();
        history.perform(command("b"));

        assertFalse(history.canRedo());
        assertEquals("b", history.undoLabel());
    }

    @Test
    void testSceneIsCleanUntilSomethingIsDone() {
        assertFalse(history.isDirty());

        history.perform(command("a"));
        assertTrue(history.isDirty());

        history.undo();
        assertFalse(history.isDirty(), "Back where it was");
    }

    @Test
    void testSavedSceneIsCleanAndStaysSoWhenGoingBackAndForth() {
        history.perform(command("a"));
        history.markSaved();
        assertFalse(history.isDirty());

        history.perform(command("b"));
        assertTrue(history.isDirty());

        history.undo();
        assertFalse(history.isDirty(), "The saved state is reached again");

        history.undo();
        assertTrue(history.isDirty(), "Before it is not the saved state");
    }

    @Test
    void testSavedStateIsLostWhenSomethingElseIsDoneAfterGoingBack() {
        history.perform(command("a"));
        history.perform(command("b"));
        history.markSaved();

        history.undo();
        history.perform(command("c"));
        history.undo();

        assertTrue(history.isDirty(), "The state with b is gone and can't be reached");
    }

    @Test
    void testStepsOfADragWithinTheWindowAreOneCommand() {
        history.perform(new MergeableStep("x", 0, 1));
        clock.addAndGet(100);
        history.perform(new MergeableStep("x", 1, 5));

        history.undo();

        assertFalse(history.canUndo(), "One undo took back the whole drag");
    }

    @Test
    void testStepsOfDifferentDragsOrFarApartInTimeAreSeparate() {
        history.perform(new MergeableStep("x", 0, 1));
        clock.addAndGet(History.MERGE_WINDOW + 1);
        history.perform(new MergeableStep("x", 1, 2));
        history.perform(new MergeableStep("y", 0, 1));

        history.undo();
        history.undo();

        assertTrue(history.canUndo(), "Three commands, two undone");
    }

    @Test
    void testHeldDragIsOneCommandHoweverLongTheMouseRests() {
        history.hold();
        history.perform(new MergeableStep("x", 0, 1));
        clock.addAndGet(History.MERGE_WINDOW * 5);
        history.perform(new MergeableStep("x", 1, 5));
        history.release();

        history.undo();

        assertFalse(history.canUndo(), "The pause didn't split the drag");
    }

    @Test
    void testDragThatIsLetGoIsNotMergedWithTheNext() {
        history.hold();
        history.perform(new MergeableStep("x", 0, 1));
        history.release();
        clock.addAndGet(10);
        history.perform(new MergeableStep("x", 1, 2));

        history.undo();

        assertTrue(history.canUndo(), "Two commands: the first was let go");
    }

    @Test
    void testSavedCommandIsNotChangedByAMerge() {
        history.perform(new MergeableStep("x", 0, 1));
        history.markSaved();
        clock.addAndGet(50);
        history.perform(new MergeableStep("x", 1, 2));

        history.undo();

        assertFalse(history.isDirty(), "The saved state came back with one undo: the step was not merged into it");
    }

    @Test
    void testOldestCommandsAreForgotten() {
        for (int index = 0; index < History.MAXIMAL_COMMANDS + 20; index++)
            history.perform(command("c" + index));

        var undone = 0;
        while (history.canUndo()) {
            history.undo();
            undone++;
        }

        assertEquals(History.MAXIMAL_COMMANDS, undone);
    }

    @Test
    void testForgettingTheSavedStateMakesTheSceneDirty() {
        history.markSaved();
        for (int index = 0; index < History.MAXIMAL_COMMANDS + 5; index++)
            history.perform(command("c" + index));

        while (history.canUndo())
            history.undo();

        assertTrue(history.isDirty(), "The saved state is older than what can be undone to");
    }

    @Test
    void testClearForgetsEverythingAndTheSceneIsClean() {
        history.perform(command("a"));
        history.clear();

        assertFalse(history.canUndo());
        assertFalse(history.canRedo());
        assertFalse(history.isDirty());
    }

    /** A step of a drag that can be merged with the next one of the same key */
    private static final class MergeableStep implements Command {
        private final String key;
        private final int from;
        private final int to;

        private MergeableStep(final String key, final int from, final int to) {
            this.key = key;
            this.from = from;
            this.to = to;
        }

        @Override
        public String label() {
            return "Step " + key;
        }

        @Override
        public void execute() {
        }

        @Override
        public void revert() {
        }

        @Override
        public Object mergeKey() {
            return key;
        }

        @Override
        public Command merge(final Command next) {
            return next instanceof MergeableStep step ? new MergeableStep(key, from, step.to) : null;
        }
    }
}
