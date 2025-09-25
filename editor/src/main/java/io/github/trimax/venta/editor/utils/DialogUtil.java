package io.github.trimax.venta.editor.utils;

import io.github.trimax.venta.editor.VentaEditor;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextInputDialog;
import javafx.stage.FileChooser;
import lombok.NonNull;
import lombok.experimental.UtilityClass;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

@UtilityClass
public final class DialogUtil {
    public void showInput(@NonNull final String header,
                          @NonNull final String title,
                          @NonNull final String name,
                          @NonNull final Consumer<String> action) {
        final var dialog = new TextInputDialog();
        dialog.initOwner(VentaEditor.getStage());
        dialog.setHeaderText(header);
        dialog.setContentText(name);
        dialog.setTitle(title);
        dialog.setWidth(512);

        dialog.showAndWait().ifPresent(action);
    }

    public void showConfirm(@NonNull final String header,
                            @NonNull final Runnable action) {
        final var dialog = new Alert(Alert.AlertType.CONFIRMATION, header, ButtonType.YES, ButtonType.NO);
        dialog.initOwner(VentaEditor.getStage());
        dialog.setHeaderText(null);

        dialog.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES)
                action.run();
        });
    }

    public void showInfo(@NonNull final String message) {
        final var dialog = new Alert(Alert.AlertType.INFORMATION, message, ButtonType.OK);
        dialog.initOwner(VentaEditor.getStage());
        dialog.setHeaderText(null);

        dialog.showAndWait();
    }

    public void showFileOpen(@NonNull final String message,
                             @NonNull final Consumer<File> action,
                             @NonNull final Map<String, List<String>> filters) {
        final var dialog = new FileChooser();
        dialog.setTitle(message);

        filters.forEach((name, ext) -> dialog.getExtensionFilters().add(new FileChooser.ExtensionFilter(name, ext)));
        Optional.ofNullable(dialog.showOpenDialog(VentaEditor.getStage())).ifPresent(action);
    }

    public void showFileSave(@NonNull final String message,
                             @NonNull final Consumer<File> action,
                             @NonNull final Map<String, List<String>> filters) {
        final var dialog = new FileChooser();
        dialog.setTitle(message);

        filters.forEach((name, ext) -> dialog.getExtensionFilters().add(new FileChooser.ExtensionFilter(name, ext)));
        Optional.ofNullable(dialog.showSaveDialog(VentaEditor.getStage())).ifPresent(action);
    }
}
