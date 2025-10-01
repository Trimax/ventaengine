package io.github.trimax.venta.editor.utils;

import io.github.trimax.venta.container.VentaApplication;
import io.github.trimax.venta.editor.VentaEditor;
import io.github.trimax.venta.editor.enums.Layout;
import io.github.trimax.venta.engine.definitions.DefinitionsIcon;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Modality;
import javafx.stage.Stage;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;

import java.io.IOException;
import java.util.Objects;

@UtilityClass
public final class WindowUtil {
    @SneakyThrows
    public void create(@NonNull final Layout layout, @NonNull final Stage stage) {
        final var scene = getScene(layout);

        stage.getIcons().add(new Image(Objects.requireNonNull(WindowUtil.class.getResourceAsStream(DefinitionsIcon.DEFAULT))));
        stage.setTitle(layout.getTitle());
        stage.setMaximized(true);
        stage.setScene(scene);
        stage.show();
    }

    @SneakyThrows
    public void createModal(@NonNull final Layout layout) {
        final var scene = getScene(layout);

        final var stage = new Stage();
        stage.getIcons().add(new Image(Objects.requireNonNull(WindowUtil.class.getResourceAsStream(DefinitionsIcon.DEFAULT))));
        stage.setTitle(layout.getTitle());
        stage.setScene(scene);

        stage.initModality(Modality.WINDOW_MODAL);
        stage.initOwner(VentaEditor.getStage());
        stage.sizeToScene();

        stage.showAndWait();
    }

    private Scene getScene(@NonNull final Layout layout) throws IOException {
        final var loader = new FXMLLoader(WindowUtil.class.getResource(layout.getPath()));
        loader.setControllerFactory(VentaApplication::getComponent);

        return new Scene(loader.load(), 800, 600);
    }
}
