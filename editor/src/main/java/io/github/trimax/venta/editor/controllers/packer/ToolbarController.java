package io.github.trimax.venta.editor.controllers.packer;

import io.github.trimax.venta.container.annotations.Component;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class ToolbarController {
    @FXML
    public void onAtlasBuild(final ActionEvent ignored) {

    }

    @FXML
    public void onAtlasSave(final ActionEvent ignored) {

    }
}
