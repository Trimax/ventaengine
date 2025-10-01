package io.github.trimax.venta.editor;

import io.github.trimax.venta.container.AbstractVentaApplication;
import io.github.trimax.venta.container.VentaApplication;
import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.editor.enums.Layout;
import io.github.trimax.venta.editor.utils.WindowUtil;
import javafx.application.Application;
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
        WindowUtil.create(Layout.Main, primaryStage);

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
