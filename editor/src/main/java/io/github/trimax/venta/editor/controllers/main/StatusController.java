package io.github.trimax.venta.editor.controllers.main;

import com.google.common.eventbus.Subscribe;
import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.editor.model.event.status.StatusSetEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

@Component
public final class StatusController {
    @FXML private Label lblStatus;

    @Subscribe
    public void onStatusSet(final StatusSetEvent event) {
        lblStatus.setText(event.status());
    }
}
