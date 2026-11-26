package io.github.trimax.venta.studio.services;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.studio.logging.ConsoleAppender;
import io.github.trimax.venta.studio.model.LogLine;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * The lines of the log of the engine for the console of the studio: the last ones, the oldest are forgotten.
 */
@Component
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ConsoleLog {
    public static final int MAXIMAL_LINES = 5000;

    /** Changed on the thread of JavaFX only */
    @Getter
    private final ObservableList<LogLine> lines = FXCollections.observableArrayList();

    /** The lines of the log go here from now on, from whatever thread they are written */
    public void install() {
        ConsoleAppender.install(line -> Platform.runLater(() -> add(line)));
    }

    /** Adds a line, on the thread of JavaFX */
    public void add(final LogLine line) {
        lines.add(line);

        if (lines.size() > MAXIMAL_LINES)
            lines.remove(0, lines.size() - MAXIMAL_LINES);
    }
}
