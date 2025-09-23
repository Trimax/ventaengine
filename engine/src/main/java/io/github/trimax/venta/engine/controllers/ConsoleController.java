package io.github.trimax.venta.engine.controllers;

import static org.lwjgl.glfw.GLFW.*;

import java.util.function.Consumer;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.engine.console.ConsoleCommandQueue;
import io.github.trimax.venta.engine.definitions.DefinitionsGeometry;
import io.github.trimax.venta.engine.enums.ConsoleMessageType;
import io.github.trimax.venta.engine.enums.ProgramType;
import io.github.trimax.venta.engine.helpers.GeometryHelper;
import io.github.trimax.venta.engine.layouts.ConsoleVertexLayout;
import io.github.trimax.venta.engine.model.states.ConsoleState;
import io.github.trimax.venta.engine.registries.implementation.ProgramRegistryImplementation;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class ConsoleController extends AbstractController<ConsoleState, Void> {
    private final ProgramRegistryImplementation programRegistry;
    private final GeometryHelper geometryHelper;

    @Override
    protected ConsoleState create(final Void argument) {
        log.debug("Initializing console");

        return new ConsoleState(programRegistry.get(ProgramType.Console.getProgramName()),
                geometryHelper.create("Console", ConsoleVertexLayout.class,
                        DefinitionsGeometry.Console.VERTICES, DefinitionsGeometry.Console.FACETS, null));
    }

    @Override
    protected void destroy(@NonNull final ConsoleState state) {
        log.debug("Deinitializing console");

        geometryHelper.delete(state.getGeometry());
    }

    public boolean isVisible() {
        return get().isVisible();
    }

    public void toggle() {
        get().setVisible(!get().isVisible());
    }

    public void accept(final char c) {
        get().accept(c);
    }

    public void clear() {
        get().clear();
    }

    public void handle(final int key, final Consumer<ConsoleCommandQueue.Command> commandConsumer) {
        switch (key) {
            case GLFW_KEY_ENTER:
                get().submit(commandConsumer);
                return;
            case GLFW_KEY_BACKSPACE:
                get().eraseLast();
                return;
            case GLFW_KEY_UP:
                get().navigateHistory(-1);
                return;
            case GLFW_KEY_DOWN:
                get().navigateHistory(1);
                return;
            default:
        }
    }

    public void header(final String format, final Object... arguments) {
        print(ConsoleMessageType.Header, format, arguments);
    }

    public void info(final String format, final Object... arguments) {
        print(ConsoleMessageType.Info, format, arguments);
    }

    public void warning(final String format, final Object... arguments) {
        print(ConsoleMessageType.Warning, format, arguments);
    }

    public void error(final String format, final Object... arguments) {
        print(ConsoleMessageType.Error, format, arguments);
    }

    public void debug(final String format, final Object... arguments) {
        print(ConsoleMessageType.Debug, format, arguments);
    }

    public void emptyLine() {
        print(ConsoleMessageType.Command, "");
    }

    private void print(final ConsoleMessageType type, final String format, final Object... arguments) {
        this.get().getHistory().add(new ConsoleController.ConsoleMessage(type, String.format(format, arguments)));
    }

    public record ConsoleMessage(ConsoleMessageType type, String text) {}
}
