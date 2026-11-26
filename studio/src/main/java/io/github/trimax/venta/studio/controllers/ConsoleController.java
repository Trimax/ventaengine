package io.github.trimax.venta.studio.controllers;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.studio.model.LogLine;
import io.github.trimax.venta.studio.services.ConsoleLog;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

/**
 * The log of the engine, from the level that is chosen up.
 */
@Component
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class ConsoleController {
    private final ConsoleLog log;

    @FXML private ChoiceBox<String> level;
    @FXML private ListView<LogLine> lines;

    @FXML
    public void initialize() {
        level.setItems(FXCollections.observableArrayList("DEBUG", "INFO", "WARN", "ERROR"));
        level.setValue("INFO");

        final var visible = new FilteredList<>(log.getLines());
        visible.predicateProperty().bind(javafx.beans.binding.Bindings.createObjectBinding(() -> line -> line.isAtLeast(level.getValue()), level.valueProperty()));
        lines.setItems(visible);
        lines.setCellFactory(view -> new ListCell<>() {
            @Override
            protected void updateItem(final LogLine line, final boolean empty) {
                super.updateItem(line, empty);

                setText(empty || line == null ? null : line.toString());
                getStyleClass().removeAll("warning", "error");
                if (!empty && line != null && line.level().equals("WARN"))
                    getStyleClass().add("warning");
                else if (!empty && line != null && (line.level().equals("ERROR") || line.level().equals("FATAL")))
                    getStyleClass().add("error");
            }
        });

        // The newest line is in view while the user doesn't look at the old ones
        visible.addListener((ListChangeListener<LogLine>) change -> {
            if (!visible.isEmpty())
                lines.scrollTo(visible.size() - 1);
        });
    }

    @FXML
    public void onClear(final ActionEvent event) {
        log.getLines().clear();
    }
}
