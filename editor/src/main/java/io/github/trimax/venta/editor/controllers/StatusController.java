package io.github.trimax.venta.editor.controllers;

import com.google.common.eventbus.Subscribe;
import io.github.trimax.venta.editor.events.status.StatusSetEvent;
import io.github.trimax.venta.editor.utils.EventUtil;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

public final class StatusController {
    @FXML private Label lblStatus;

    @FXML
    public void initialize() {
        EventUtil.register(this);
    }

    @Subscribe
    public void onStatusSet(final StatusSetEvent event) {
        lblStatus.setText(event.status());
    }
}
