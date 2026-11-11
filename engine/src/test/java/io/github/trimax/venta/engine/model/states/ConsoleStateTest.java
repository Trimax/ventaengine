package io.github.trimax.venta.engine.model.states;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.github.trimax.venta.engine.console.ConsoleCommandQueue;
import io.github.trimax.venta.engine.controllers.ConsoleController;
import io.github.trimax.venta.engine.definitions.DefinitionsConsole;
import io.github.trimax.venta.engine.enums.ConsoleMessageType;

final class ConsoleStateTest {
    private static final String PROMPT = DefinitionsConsole.CONSOLE_WELCOME_SYMBOL;

    private final List<ConsoleCommandQueue.Command> submitted = new ArrayList<>();
    private ConsoleState state;

    @BeforeEach
    void createState() {
        state = new ConsoleState(null, null);
    }

    @Test
    void testTypingAndErasing() {
        type("ab");
        state.eraseLast();
        state.eraseLast();
        state.eraseLast();

        assertEquals(PROMPT, state.getBuffer(), "The prompt must not be erased");
    }

    @Test
    void testSubmitSendsCommandAndClearsInput() {
        type("scene list");
        state.submit(submitted::add);

        assertEquals(List.of("scene list"), values(), "Command must be submitted");
        assertEquals(PROMPT, state.getBuffer(), "Input must be cleared");
    }

    @Test
    void testBlankInputAndCommentsAreNotSubmitted() {
        type("   ");
        state.submit(submitted::add);
        type("# comment");
        state.submit(submitted::add);

        assertTrue(submitted.isEmpty(), "Blank input and comments must not be submitted");
        assertTrue(state.getCommands().isEmpty(), "They must not be stored in the history");
    }

    @Test
    void testHistoryNavigation() {
        submit("first");
        submit("second");

        state.navigateHistory(-1);
        assertEquals(PROMPT + "second", state.getBuffer(), "Up shows the last command");

        state.navigateHistory(-1);
        assertEquals(PROMPT + "first", state.getBuffer(), "Up again shows the previous command");

        state.navigateHistory(-1);
        assertEquals(PROMPT + "first", state.getBuffer(), "History stops at the first command");

        state.navigateHistory(1);
        state.navigateHistory(1);
        assertEquals(PROMPT, state.getBuffer(), "Down after the last command clears the input");
    }

    @Test
    void testHistoryWorksAfterClearingMessages() {
        submit("scene list");
        state.getHistory().add(new ConsoleController.ConsoleMessage(ConsoleMessageType.Info, "output"));
        state.clear();

        state.navigateHistory(-1);

        assertEquals(PROMPT + "scene list", state.getBuffer(), "Commands must be available after the messages are cleared");
    }

    @Test
    void testNavigationWithoutCommandsDoesNothing() {
        state.navigateHistory(-1);

        assertEquals(PROMPT, state.getBuffer(), "Input must not change");
    }

    private void submit(final String command) {
        type(command);
        state.submit(submitted::add);
    }

    private void type(final String text) {
        text.chars().forEach(c -> state.accept((char) c));
    }

    private List<String> values() {
        return submitted.stream().map(ConsoleCommandQueue.Command::value).toList();
    }
}
