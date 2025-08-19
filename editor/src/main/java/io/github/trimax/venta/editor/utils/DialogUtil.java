package io.github.trimax.venta.editor.utils;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextInputDialog;
import lombok.NonNull;
import lombok.experimental.UtilityClass;

import java.util.function.Consumer;

@UtilityClass
public final class DialogUtil {
    public void showInput(@NonNull final String header,
                          @NonNull final String title,
                          @NonNull final String name,
                          @NonNull final Consumer<String> action) {
        final var dialog = new TextInputDialog();
        dialog.setHeaderText(header);
        dialog.setContentText(name);
        dialog.setTitle(title);
        dialog.setWidth(512);

        dialog.showAndWait().ifPresent(action);
    }

    public void showConfirm(@NonNull final String header,
                            @NonNull final Runnable action) {
        final var dialog = new Alert(Alert.AlertType.CONFIRMATION, header, ButtonType.YES, ButtonType.NO);
        dialog.setHeaderText(null);

        dialog.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES)
                action.run();
        });
    }
}
