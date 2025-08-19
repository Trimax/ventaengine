package io.github.trimax.venta.editor;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import lombok.SneakyThrows;

public final class VentaArchiveManager extends Application {
    @Override
    @SneakyThrows
    public void start(final Stage primaryStage) {
        final var scene = new Scene(new FXMLLoader(getClass().getResource("/main.fxml")).load(), 800, 600);
        primaryStage.setTitle("Venta Resource Manager");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(final String[] args) {
        launch(args);
    }
}
