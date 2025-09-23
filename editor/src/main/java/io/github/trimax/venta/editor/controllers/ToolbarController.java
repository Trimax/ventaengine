package io.github.trimax.venta.editor.controllers;

import com.google.common.eventbus.Subscribe;
import io.github.trimax.venta.editor.events.EventBus;
import io.github.trimax.venta.editor.events.ItemSelectedEvent;
import javafx.fxml.FXML;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ToolbarController {
    @FXML
    public void initialize() {
        System.out.println("INIT");
        EventBus.getInstance().register(this);
    }

    @Subscribe
    public void onSelectionChanged(final ItemSelectedEvent event) {
        log.info("Item selected: {}", event.selectedItem());
    }
}
