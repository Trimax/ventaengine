package io.github.trimax.venta.editor;

import io.github.trimax.venta.container.AbstractVentaApplication;
import io.github.trimax.venta.container.VentaApplication;
import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.engine.definitions.DefinitionsIcon;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import lombok.Getter;
import lombok.SneakyThrows;

@Component
public final class VentaEditor extends Application implements AbstractVentaApplication<Void> {
    @Getter
    private static Stage stage;

    @Override
    @SneakyThrows
    public void start(final Stage primaryStage) {
        final var loader = new FXMLLoader(getClass().getResource("/layouts/main.fxml"));
        loader.setControllerFactory(VentaApplication::getComponent);

        final var scene = new Scene(loader.load(), 800, 600);
        primaryStage.getIcons().add(new Image(getClass().getResourceAsStream(DefinitionsIcon.DEFAULT)));
        primaryStage.setTitle("Venta Editor");
        primaryStage.setMaximized(true);
        primaryStage.setScene(scene);
        primaryStage.show();

        stage = primaryStage;
    }

    @Override
    public void start(final String[] args, final Void argument) {
        launch(args);
    }

    public static void main(final String[] args) {
        VentaApplication.run(args, VentaEditor.class);
    }
}
